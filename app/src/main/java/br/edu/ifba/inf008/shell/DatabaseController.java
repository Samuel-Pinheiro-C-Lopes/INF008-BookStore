package br.edu.ifba.inf008.shell;

import br.edu.ifba.inf008.interfaces.controller.IDatabaseController;

import java.sql.*;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseController implements IDatabaseController {
    private static volatile DatabaseController instance = null;
    private final String url;
    private final String user;
    private final String password;

    /*
    public static boolean init(
        String url,
        String user,
        String password
    ) {
        if (DatabaseController.checkPreInit() == false) return false;

        instance = new DatabaseController(url, user, password);
        return true;
    }
    */

    public static DatabaseController getInstance(
        String url,
        String user,
        String password
    ) {
        synchronized (DatabaseController.class) {
            if (instance == null)
                 instance = new DatabaseController(url, user, password);
        }

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
    public Object executeScalarQuery(final String query, final List<Object> parameters) {
        Object scalarReturn = null;

        try (
            Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query)
        ) {
            for (int i = 0; i < parameters.size(); i++)
                stmt.setObject(i + 1, parameters.get(i));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    scalarReturn = rs.getObject(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return scalarReturn;
    }

    @Override
    public List<Map<String, Object>> executeEntityQuery(final String query, final List<Object> parameters) {
        final List<Map<String, Object>> resultRows = new ArrayList<>();
        HashMap<String, Object> row = null;
        String columnName;
        int i;

        try (
            Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query);
        ) {

            for (i = 0; i < parameters.size(); i++)
                stmt.setObject(i + 1, parameters.get(i));

            try (ResultSet rs = stmt.executeQuery()) {
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
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return resultRows;
    }

    @Override
    public int executeNonQuery(final String query, final List<Object> parameters) {
        int returnValue = -1;

        try (
            Connection conn = DriverManager.getConnection(url, user, password);
            PreparedStatement stmt = conn.prepareStatement(query)
        ) {
            for (int i = 0; i < parameters.size(); i++)
                stmt.setObject(i + 1, parameters.get(i));

            returnValue = stmt.executeUpdate();
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
