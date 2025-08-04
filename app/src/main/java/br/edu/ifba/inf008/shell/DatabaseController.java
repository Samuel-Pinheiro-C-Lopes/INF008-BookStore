package br.edu.ifba.inf008.shell;

import br.edu.ifba.inf008.interfaces.controller.IDatabaseController;

import java.sql.*;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseController implements IDatabaseController {
    private static DatabaseController instance = null;
    private final String url;
    private final String user;
    private final String password;

    public static boolean init(
        String url,
        String user,
        String password
    ) {
        instance = new DatabaseController(url, user, password);
    }

    public DatabaseController getInstance() {
        return instance;
    }

    private DatabaseController(
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
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();
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
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();
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
            return null;
        }

        return resultRows;
    }

    @Override
    public int executeNonQuery(final String query) {
        int returnValue = -1;

        try (
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement()
        ) {
            returnValue = stmt.executeUpdate(query);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return returnValue;
    }

    private static boolean checkPreInit() {
        if (instance != null)
            return false;
        return true;
    }
}
