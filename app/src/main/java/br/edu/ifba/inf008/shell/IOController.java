package br.edu.ifba.inf008.shell;

import br.edu.ifba.inf008.interfaces.IIOController;

import java.sql.*;

class IOController implements IIOController
{
/*
    public boolean select() {

    }

    public boolean insert(String table) {

    }

    public boolean update() {

    }

    public boolean delete() {

    }
*/

    @Override
    public void test() {
        String url = "jdbc:mariadb://localhost:3306/bookstore";
        String user = "root";
        String password = "root";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connection made");

            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery("SELECT * FROM users");

            while (rs.next())
                System.out.printf("User: %s (%s)%n", rs.getString("name"), rs.getString("email"));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
