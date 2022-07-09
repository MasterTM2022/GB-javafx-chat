package ru.gb.perov.gbjavafxchat.client;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Optional;

public class ChatClient {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final ChatController controller;

    public ChatClient(ChatController controller) {
        this.controller = controller;
    }

    public void openConnection() throws IOException {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                waitAuth();
                readMessages();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeConnection();
                System.exit(0);
            }
        }).start();
    }

    private void waitAuth() throws IOException {
        while (true) {
            final String message = in.readUTF();
            if (message.startsWith("/authok")) { //   /authok nick1
                final String[] split = message.split("\\p{Blank}+");
                final String nick = split[1];
                controller.setAuth(true);
                controller.addMessage("Успешная авторизация под ником " + nick);
                break;
            } else {
                controller.setAuth(false);
            }
        }
    }

    private void closeConnection() {
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
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void readMessages() throws IOException {
        while (true) {
            final String message = in.readUTF();
            if ("/end".equalsIgnoreCase(message)) {
                controller.setAuth(false);
                break;
            }
                controller.addMessage(message);
        }
    }

    public void sendMessage(String message) {
        try {
            out.writeUTF(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void showWrongLogin() {
//        final Alert alert = new Alert(Alert.AlertType.ERROR,
//                "Неверный логин и/или пароль \n" +
//                        "либо такой пользователь незарегистрирован!",
//                new ButtonType("Попробовать снова", ButtonBar.ButtonData.OK_DONE),
//                new ButtonType("Выйти", ButtonBar.ButtonData.CANCEL_CLOSE)
//        );
//        alert.setTitle("Ошибка логина/пароля!");
//        final Optional<ButtonType> answer = alert.showAndWait();
//        final Boolean inExit = answer.map(select -> select.getButtonData().isCancelButton()).orElse(false);
//        if (inExit) {
//            System.exit(0);
//        }
//    }
}
