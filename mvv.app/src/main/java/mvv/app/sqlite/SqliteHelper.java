package mvv.app.sqlite;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import mvv.app.exception.HandleError;
import mvv.app.utils.CollectionUtils;
import mvv.app.utils.StringUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Manh Vu
 */
public class SqliteHelper {
    private static final Logger log = LogManager.getLogger(SqliteHelper.class);

    private static Connection connection;
    private static Statement statement;

    /**
     * @param string
     */
    public SqliteHelper(String dbPath) {
        try {
            connect(dbPath);
        } catch (ClassNotFoundException | SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    /* run on application start */
    private Connection connect(String dbPath) throws ClassNotFoundException, SQLException {
        if (connection == null) {
            // load the sqlite-JDBC driver using the current class loader
            Class.forName("org.sqlite.JDBC");

            // create a database connection
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            connection.setAutoCommit(false);
        }

        return connection;
    }

    public Statement getStatement() throws SQLException {
        if (statement == null) {
            statement = connection.createStatement();
            statement.setQueryTimeout(10);
        }

        return statement;
    }

    public <T> void resetIndex(Class<T> clazz) throws SQLException {
        StringBuilder sb = new StringBuilder(20);
        sb.append("update sqlite_sequence set seq = 0 where name = ").append(boundBy(getTableName(clazz), "\""));

        String qr = sb.toString();
        log.trace(qr);

        connection.createStatement().executeUpdate(qr);
        connection.commit();
    }

    public <T> void deleteAll(Class<T> clazz) throws SQLException {
        StringBuilder sb = new StringBuilder(20);
        sb.append("delete from ").append(getTableName(clazz));

        String qr = sb.toString();
        log.trace(qr);

        connection.createStatement().executeUpdate(qr);
        connection.commit();
    }

    public <T> T insert(T t) throws SQLException {
        Map<Object, Type> fieldValueHolder = extractValue(t);

        PreparedStatement ps = connection.prepareStatement(buildInsert(t.getClass()), new String[] { getIdField(t.getClass()) });
        ps.closeOnCompletion();

        int i = 1;
        for (Entry<Object, Type> entry : fieldValueHolder.entrySet()) {
            ps.setObject(i++, entry.getKey());
        }

        synchronized (this) {
            ps.executeUpdate();
            connection.commit();
        }

        return t;
    }

    public <T> void batchInsert(List<T> list) throws SQLException {
        if (CollectionUtils.isEmpty(list)) return;

        T t = list.get(0);
        PreparedStatement ps = connection.prepareStatement(buildInsert(t.getClass()), new String[] { getIdField(t.getClass()) });
        ps.closeOnCompletion();

        for (int i = 0; i < list.size(); i++) {
            T tt = list.get(i);
            Map<Object, Type> fieldValueHolder = extractValue(tt);

            int c = 1;
            for (Entry<Object, Type> entry : fieldValueHolder.entrySet()) {
                ps.setObject(c++, entry.getKey());
            }

            ps.addBatch();
        }

        synchronized (this) {
            ps.executeBatch();
            connection.commit();
        }
    }

    private <T> String buildInsert(Class<T> clazz) {
        String tableName = getTableName(clazz);

        StringBuilder sb = new StringBuilder(40);
        sb.append("insert into ");
        sb.append(tableName).append(" values (");

        Field[] fields = clazz.getFields();
        for (int i = 0; i < fields.length; i++) {
            sb.append(" ?,");
        }

        sb.delete(sb.length() - 1, sb.length());
        sb.append(')');

        String qr = sb.toString();
        log.trace(qr);

        return qr;
    }

    private <T> Map<Object, Type> extractValue(T t) {
        Field[] fields = t.getClass().getFields();
        Map<Object, Type> fieldValueHolder = new LinkedHashMap<>(fields.length);
        for (Field field : fields) {
            try {
                fieldValueHolder.put(field.get(t), field.getGenericType());
            } catch (IllegalArgumentException | IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
        }

        return fieldValueHolder;
    }

    /**
     * @author Manh Vu
     */
    private String boundBy(String s, String textOrChar) {
        if (StringUtils.isEmpty(s)) {
            throw new HandleError("param is invalid: " + s);
        }

        if (StringUtils.isEmpty(textOrChar)) {
            throw new HandleError("param is invalid: " + textOrChar);
        }

        StringBuilder sb = new StringBuilder(s.length() + textOrChar.length());
        sb.append(textOrChar).append(s).append(textOrChar);

        return sb.toString();
    }

    private <T> String getTableName(Class<T> clazz) {
        Entity entity = clazz.getAnnotation(Entity.class);

        if (entity == null || StringUtils.isEmpty(entity.value())) {
            throw new HandleError(clazz.getCanonicalName() + " is not an valid entity");
        }

        return entity.value();
    }

    private <T> String getIdField(Class<T> clazz) {
        String fieldName = null;

        Field[] fields = clazz.getFields();
        for (Field field : fields) {
            Id id = field.getAnnotation(Id.class);
            if (id != null) {
                fieldName = field.getName();
                break;
            }
        }

        if (StringUtils.isEmpty(fieldName)) throw new HandleError(clazz.getCanonicalName() + " missing id");

        return null;
    }

    /**
     *
     * @author Manh Vu
     */
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     *
     * @author Manh Vu
     */
    public void rollback() {
        try {
            connection.rollback();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }
    }
}
