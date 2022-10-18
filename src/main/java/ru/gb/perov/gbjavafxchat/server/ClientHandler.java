package ru.gb.perov.gbjavafxchat.server;

import ru.gb.perov.gbjavafxchat.Command;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.SQLException;

import static ru.gb.perov.gbjavafxchat.Command.*;
import static ru.gb.perov.gbjavafxchat.server.JdbcApp.*;

public class ClientHandler {
    private Socket socket;
    private ChatServer server;
    private DataInputStream in;
    private DataOutputStream out;
    private String nick;
    private AuthService authService;


    public ClientHandler(Socket socket, ChatServer server, AuthService authService) {
        try {
            this.socket = socket;
            this.server = server;
            this.authService = authService;
            this.in = new DataInputStream(socket.getInputStream());
            this.out = new DataOutputStream(socket.getOutputStream());
            new Thread(() -> {
                try {
                    if (authentificate()) {
                        readMessage();
                    }
                } finally {
                    closeConnection();
                }
            }
            ).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNick() {
        return nick;
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    private boolean authentificate() { // контракт "формата сообщений": /auth login1 pass1
        while (true) {
            try {
                final String message = in.readUTF();
                Command command = Command.getCommand(message);
                if (command == END) {
                    break;
                }
                if (command == AUTH) {
                    String[] params = command.parse(message);
                    String login = params[0];
                    String password = params[1];
                    String nick = authService.getNickByLoginAndPassword(login, password);
                    if (nick != null) {
                        if (server.isNickBusy(nick)) {
                            sendMessage(Command.ERROR, "Пользователь уже авторизован");
                            continue;
                        }
                        sendMessage(Command.AUTHOK, nick);
                        this.nick = nick;
                        server.broadcast(MESSAGE, "Пользователь " + nick + " зашёл в чат.");
                        server.subscribe(this);
                        return true;
                    } else {
                        sendMessage(ERROR, "Неверный логин и/или пароль");
                        continue;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }

    private void closeConnection() {
        sendMessage(END);
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (socket != null) {
            server.unsubscibe(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessage() {
        while (true) {
            try {
                final String message = in.readUTF();
                final Command command = getCommand(message);
                String[] params = command.parse(message);
                if (command == END) {
                    break;
                }
                if (command == PRIVATE_MESSAGE) {
                    String nickTo = params[0];
                    String singleMessage = params[1];
                    server.singlePost(singleMessage, nickTo, this);
                    continue;
                }
                if (command == CHANGE_NICK) {
                    String newNick = params[0];
                    try {
                        connect();
                        stmt.executeUpdate("UPDATE Users SET Nick = '" + newNick + "' WHERE Nick = '" + nick + "';");
                        server.broadcast(nick + " сменил Nickname на новый - " + newNick);
                        server.changeNick(nick, newNick);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    } finally {
                        disconnect();
                    }
                    continue;
                }
                server.broadcast(nick + ": " + params[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
