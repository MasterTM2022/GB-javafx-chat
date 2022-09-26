package ru.gb.perov.gbjavafxchat.server;

import java.sql.SQLException;

import static ru.gb.perov.gbjavafxchat.server.JdbcApp.*;

public class BeforeStart {

    private static final long START_NUMBER_USERS = 10;

    public static void prepareBase() {
            try {
                connect();
                stmt.executeUpdate("DELETE FROM Users");
                for (int i=1; i <= START_NUMBER_USERS; i++)
                stmt.executeUpdate("INSERT INTO Users (Nick, Login,Password) VALUES " +
                        "('nick"+i+"', 'login"+i+"','pass"+i+"');");
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                disconnect();
            }
        }
    }