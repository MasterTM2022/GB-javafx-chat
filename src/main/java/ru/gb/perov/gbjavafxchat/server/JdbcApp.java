package ru.gb.perov.gbjavafxchat.server;

import java.sql.*;

public class JdbcApp {
    private static Connection connection;
    static Statement stmt;
    public static void connect() throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:GB-javafx-chat\\src\\main\\resources\\ru\\gb\\perov\\gbjavafxchat\\server\\ChatUsersBase.db");
        stmt = connection.createStatement();
    }

    public static void disconnect() {
        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
