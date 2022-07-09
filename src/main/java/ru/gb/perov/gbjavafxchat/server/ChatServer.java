package ru.gb.perov.gbjavafxchat.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ChatServer {

    private final List<ClientHandler> clients;

    public ChatServer() {
        this.clients = new ArrayList<>();
    }

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8189);
             AuthService authService = new InMemoryAuthService()) {
            while (true) {
                System.out.println("Ожидаю подключения...");
                final Socket socket = serverSocket.accept();
                new ClientHandler(socket, this, authService);
                System.out.println("Клиент подключен.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.sendMessage(message);
        }
    }

    public boolean singlePost(String message, String nickTo) {
        int i = 0;
        ClientHandler client;
        while (clients.size() > i) {
            client = clients.get(i++);
            if (client.getNick().equals(nickTo)) {
                client.sendMessage(message);
                return true;
            }
        }
        return false;
    }

    public void subscribe(ClientHandler client) {
        clients.add(client);
    }

    public boolean isNickBusy(String nick) {
        for (ClientHandler client :
                clients) {
            if (nick.equals(client.getNick())) {
                return true;
            }
        }
        return false;
    }

    public void unsubscibe(ClientHandler client) {
        clients.remove(client);
    }
}