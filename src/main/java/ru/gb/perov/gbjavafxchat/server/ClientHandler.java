package ru.gb.perov.gbjavafxchat.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

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
                    authentificate();
                    readMessage();
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

    private void authentificate() { // контракт "формата сообщений": /auth login1 pass1
        while (true) {
            try {
                final String message = in.readUTF();
                if (message.startsWith("/auth")) {
                    String[] split = message.split("\\p{Blank}+");
                    final String login = split[1];
                    final String password = split[2];
                    String nick = authService.getNickByLoginAndPassword(login, password);
                    if (nick != null) {
                        if (server.isNickBusy(nick)) {
                            sendMessage("Пользователь уже авторизован");
                            continue;
                        }
                        sendMessage("/authok " + nick);
                        this.nick = nick;
                        server.broadcast("Пользователь " + nick + " зашёл в чат.");
                        server.subscribe(this);
                        break;
                    }
//                     else {                                               // в конструкции с видимостью/невидимостью блоков
//                        sendMessage("Неверные логин и пароль");           // данная секция
//                    }                                                     // неактуальна ???
                    else {
                        sendMessage("/authNOTok");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeConnection() {
        sendMessage("/end");

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

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readMessage() {
        while (true) {
            try {
                String message = in.readUTF();
                if ("/end".equalsIgnoreCase(message)) {
                    break;
                } else if (message.startsWith("/w ")) {
                    String[] split = message.split("\\p{Blank}+");
                    final String nickTo = split[1];
                    message = this.getNick() + " -> "+ nickTo + ": " + message.replace("/w " + nickTo + " ", "");
                    if (!server.singlePost(message, nickTo)) {
                        server.singlePost(nickTo + " не залогинен в чат!", this.getNick());
                    } else {
                        server.singlePost(message, this.getNick());
                    }
                } else {
                    server.broadcast(nick + ": " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
