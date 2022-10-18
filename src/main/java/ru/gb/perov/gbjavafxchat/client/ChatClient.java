package ru.gb.perov.gbjavafxchat.client;

import javafx.application.Platform;
import ru.gb.perov.gbjavafxchat.Command;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import static ru.gb.perov.gbjavafxchat.Command.*;

public class ChatClient {

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final ChatController controller;
    private volatile static boolean flagAuth = false;
    private final int PAUSE_TO_SLEEP_SEC = 20;
    private final int FPS = 2;

    public ChatClient(ChatController controller) {
        this.controller = controller;
    }

    public void openConnection() throws IOException {
        socket = new Socket("localhost", 8189);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        new Thread(() -> {
            try {
                if (waitAuth()) {
                    readMessages();
                }
            } catch (IOException e) {
                System.out.println("Клиент был закрыт по тайм-ауту...");
//                e.printStackTrace();
            } finally {
                closeConnection();
                System.exit(0);
            }
        }).start();
    }

    private boolean waitAuth() throws IOException {
        new Thread(() -> {
            int timer = PAUSE_TO_SLEEP_SEC * FPS;
            while (timer >= 0) {
                try {
                    Thread.sleep(1000/FPS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                double var = 1.0 * timer / PAUSE_TO_SLEEP_SEC / FPS;
                controller.setProgress(var);
                timer -= 1;
            }

            if (!flagAuth) {
                try {
                    Platform.runLater(() -> controller.showError("Слишком долго вспоминатете параметры входа...\nСейчас клиент будет закрыт"));
                    Thread.sleep(3_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                sendMessage(END);
                System.exit(0);
            }
        }).start();

        while (true) {
            final String message = in.readUTF();
            final Command command = Command.getCommand(message);
            final String[] params = command.parse(message);
            if (command == AUTHOK) { //   /authok nick1
                flagAuth = true;
                final String nick = params[0];
                controller.setNickOnForm(nick);
                controller.setAuth(true);
                controller.addMessage("Успешная авторизация под ником " + nick);
                return true;
            }

            if (command == ERROR) {
                Platform.runLater(() -> controller.showError(params[0]));
                continue;
            }
            controller.setAuth(false);
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
            final Command command = Command.getCommand(message);
            if (END == command) {
                controller.setAuth(false);
                break;
            }
            String[] params = command.parse(message);
            if (ERROR == command) {
                String messageError = params[0];
                Platform.runLater(() -> controller.showError(messageError));
                continue;
            }
            if (MESSAGE == command) {
                controller.addMessage(params[0]);
                continue;
            }
            if (CLIENTS == command || CHANGE_NICK == command) {
                Platform.runLater(() -> controller.updateClientList(params));
                continue;
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

    public void sendMessage(Command command, String... params) {
        sendMessage(command.collectMessage(params));
    }
}