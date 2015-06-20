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

    /* run on application start */
    public Connection connect() throws ClassNotFoundException, SQLException {
        if (connection != null) return connection;

        // load the sqlite-JDBC driver using the current class loader
        Class.forName("org.sqlite.JDBC");

        Connection connection = null;
        // create a database connection
        connection = DriverManager.getConnection("jdbc:sqlite:mvvapp.db3");
        return connection;
    }

    public Statement getStatement() throws SQLException {
        if (statement != null) return statement;

        Statement statement = connection.createStatement();
        statement.setQueryTimeout(10);

        return statement;
    }

    public <T> T insert(T t) {
        String tableName = getTableName(t);
        String idField = getIdField(t);

        StringBuilder sb = new StringBuilder("insert into ");
        sb.append(tableName).append(" values (");

        Field[] fields = t.getClass().getFields();
        Map<Object, Type> fielValueHolder = new LinkedHashMap<>(fields.length);
        for (Field field : fields) {
            sb.append(" ?,");

            try {
                fielValueHolder.put(field.get(t), field.getGenericType());
            } catch (IllegalArgumentException | IllegalAccessException e) {
                log.error(e.getMessage(), e);
            }
        }

        sb.delete(sb.length() - 1, sb.length());
        sb.append(')');

        try {
            PreparedStatement ps = connection.prepareStatement(sb.toString(), new String[] {idField});
            int i = 0;
            for(Entry<Object, Type> entry : fielValueHolder.entrySet()) {
                ps.setObject(i++, entry.getKey());
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }

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
}
