package hibernate.traps.transactional_tests.util;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TestDatabaseUtil {

    private final static List<String> TABLE_NAMES = new ArrayList<>();

    public static void resetDatabase(DataSource dataSource) {
        try {
            Connection c = dataSource.getConnection();
            c.setAutoCommit(false);
            Statement s = c.createStatement();
            s.execute("SET REFERENTIAL_INTEGRITY FALSE");
            for (String tabie : getTableNames(s)) {
                s.executeUpdate("TRUNCATE TABLE " + tabie);
            }
            s.execute("SET REFERENTIAL_INTEGRITY TRUE");
            s.close();
            c.commit();
            c.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private static List<String> getTableNames(Statement s) throws SQLException {
        if (TABLE_NAMES.isEmpty()) {
            ResultSet rs = s.executeQuery("SELECT TABLE_SCHEMA||'.'||TABLE_NAME FROM INFORMATION_SCHEMA.TABLES where TABLE_NAME like 'APP_%'");
            while (rs.next()) {
                TABLE_NAMES.add(rs.getString(1));
            }
            rs.close();
        }
        System.out.println(TABLE_NAMES);
        return TABLE_NAMES;
    }
}
