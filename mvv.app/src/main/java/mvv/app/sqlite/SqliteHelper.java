package mvv.app.sqlite;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import mvv.app.exception.HandleError;
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

    private volatile int waitForCommit;

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

    public <T> T insert(T t) throws SQLException {
        String tableName = getTableName(t);
        String idField = getIdField(t);

        StringBuilder sb = new StringBuilder("insert into ");
        sb.append(tableName).append(" values (");

        Field[] fields = t.getClass().getFields();
        Map<Object, Type> fielValueHolder = new LinkedHashMap<>(fields.length);
        for (Field field : fields) {
            try {
                fielValueHolder.put(field.get(t), field.getGenericType());
                sb.append(" ?,");
            } catch (IllegalArgumentException | IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
        }

        sb.delete(sb.length() - 1, sb.length());
        sb.append(')');

        PreparedStatement ps = connection.prepareStatement(sb.toString(), new String[] { idField });
        int i = 1;
        for (Entry<Object, Type> entry : fielValueHolder.entrySet()) {
            ps.setObject(i++, entry.getKey());
        }

        ps.executeUpdate();
        waitForCommit++;

        return t;
    }

    public <T> String getTableName(T t) {
        Entity entity = t.getClass().getAnnotation(Entity.class);

        if (entity == null || StringUtils.isEmpty(entity.value())) {
            throw new HandleError(t.getClass().getCanonicalName() + " is not an valid entity");
        }

        return entity.value();
    }

    public <T> String getIdField(T t) {
        String fieldName = null;

        Field[] fields = t.getClass().getFields();
        for (Field field : fields) {
            Id id = field.getAnnotation(Id.class);
            if (id != null) {
                fieldName = field.getName();
                break;
            }
        }

        if (StringUtils.isEmpty(fieldName)) throw new HandleError(t.getClass().getCanonicalName() + " missing id");

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
    public synchronized void commit() {
        try {
            if(waitForCommit > 0)
                connection.commit();

            waitForCommit = 0;
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
