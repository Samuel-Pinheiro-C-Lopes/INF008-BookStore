package br.edu.ifba.inf008.shell;

import br.edu.ifba.inf008.interfaces.IDatabaseController;

import java.sql.*;

public class DatabaseController implements IDatabaseController {
    private final String url = "jdbc:mariadb://localhost:3306/bookstore";
    private final String user = "root";
    private final String password = "root";
    private static IDatabaseController instance = null;

    public static IDatabaseController getInstance() {
        return instance;
    }

    public static void init(
        String url,
        String user,
        String password
    ) {
        instance = new DatabaseController(url, user, password);
    }

    protected DatabaseController(
        String url,
        String user,
        String password
    ) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public Object executeScalarQuery(final String query) {
        Object scalarReturn = null;

        try (
            Connection conn = DriverManager.getConnection(url, user, password),
            Statement stmt = conn.createStatement(),
            ResultSet rs = stmt.executeQuery(query)
        ) {
            if (rs.next())
                scalarReturn = rs.getObject(1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return scalarReturn;
    }

    @Override
    public List<Map<String, Object>> executeEntityQuery(final String query) {
        final List<Map<String, Object>> resultRows = new ArrayList<>();
        HashMap<String, Object> row = null;
        String columnName;
        int i;

        try (
            Connection conn = DriverManager.getConnection(url, user, password),
            Statement stmt = conn.createStatement(),
            ResultSet rs = stmt.executeQuery(query)
        ) {
            ResultSetMetaData rsmeta = rs.getMetaData();
            int columnsCount = rsmeta.getColumnCount();

            while (rs.next()) {
                row = new HashMap<>();

                for (i = 1; i <= columnsCount; i++) {
                    columnName = rsmeta.getColumnLabel(i);
                    row.put(columnName, rs.getObject(i));
                }

                resultRows.add(row);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return resultRows;
    }

    @Override
    public int executeNonQuery(final String query) {
        int returnValue = -1;

        try (
            Connection conn = DriverManager.getConnection(url, user, password),
            Statement stmt = conn.createStatement()
        ) {
            returnValue = stmt.executeUpdate(query);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnValue;
    }
}
