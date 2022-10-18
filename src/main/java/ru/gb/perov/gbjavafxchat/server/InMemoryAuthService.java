package ru.gb.perov.gbjavafxchat.server;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static ru.gb.perov.gbjavafxchat.server.JdbcApp.*;

public class InMemoryAuthService implements AuthService {

    private static class UserData {
        private String nick;
        private final String login;
        private final String password;

        public UserData(int id, String nick, String login, String password) {
            this.nick = nick;
            this.login = login;
            this.password = password;
        }

        public String getNick() {
            return nick;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }
    }

    private List<UserData> users;

    public InMemoryAuthService() {
        try {
            connect();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Users;");
            users = new ArrayList<>();
            while (rs.next()) {
                users.add(new UserData(rs.getInt("ID"), rs.getString("Nick"), rs.getString("Login"), rs.getString("Password")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
    }
    @Override
    public String getNickByLoginAndPassword(String login, String password) {
        String returnString = null;
        try {
            connect();
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM Users \n" +
                    "WHERE Login = '" + login + "' AND\n" +
                    "Password = '" + password + "';");
            if (resultSet.next()) {
                returnString = resultSet.getString("Nick");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            disconnect();
        }
        return returnString;
    }

    @Override
    public void close() throws IOException {
        System.out.println("Сервис аутентификации остановлен");
    }
}
