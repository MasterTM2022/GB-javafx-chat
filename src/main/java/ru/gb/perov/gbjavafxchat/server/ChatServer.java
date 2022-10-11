package ru.gb.perov.gbjavafxchat.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.gb.perov.gbjavafxchat.Command;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.gb.perov.gbjavafxchat.Command.*;

public class ChatServer {

    private final Map<String, ClientHandler> clients;

    public ChatServer() {
        this.clients = new HashMap<>();
    }

    public static final Logger LOGGER = LoggerFactory.getLogger(ChatServer.class);

    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(8190);
            AuthService authService = new InMemoryAuthService()) {

            LOGGER.info("String: {}.", "Сервер запущен");

            while (true) {
                LOGGER.info("String: {}.", "Ожидаю подключения...");
                final Socket socket = serverSocket.accept();
                new ClientHandler(socket, this, authService);
                LOGGER.info("String: {}.", "Клиент подключен");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcast(String message) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(MESSAGE, message);
        }
    }

    public void singlePost(String message, String nickTo, ClientHandler clientFrom) {
        ClientHandler clientTo = clients.get(nickTo);
        if (clientTo == null) {
            clientFrom.sendMessage(ERROR, nickTo + " не залогинен в чат!");
            LOGGER.error("String: {}.", "Попытка отправить сообщение незалогиненному пользователю " + nickTo);
        } else {
            clientTo.sendMessage(MESSAGE, clientFrom.getNick() + " -> " + nickTo + ": " + message);
            if (!nickTo.equals(clientFrom.getNick())) {
                clientFrom.sendMessage(MESSAGE, clientFrom.getNick() + " -> " + nickTo + ": " + message);
            }
            LOGGER.info("String: {}.", "Пользователь " + clientFrom.getNick() + " отправил сообщение для пользователя " + nickTo);
        }
    }

    public void aprooveChangeNick(ClientHandler client) {
        client.sendMessage(APROOVE_CHANGE_NICK);
    }


    public void historyBack(String message, ClientHandler clientFrom) {
//        ClientHandler clientTo = clients.get(nickTo);
//        if (clientTo == null) {
//            clientFrom.sendMessage(ERROR, nickTo + " не залогинен в чат!");
//        } else {
//            clientTo.sendMessage(MESSAGE, clientFrom.getNick() + " -> " + nickTo + ": " + message);
        clientFrom.sendMessage(MESSAGE, clientFrom.getNick() + "'s history -> " + clientFrom.getNick() + ": \n\n" + message);

    }

    public void subscribe(ClientHandler client) {
        clients.put(client.getNick(), client);
        broadcastClientsList();
    }

    public void changeNick(String nick, String newNick) {
        this.clients.get(nick).setNick(newNick);
        this.clients.put(newNick, this.clients.get(nick));
        this.clients.remove(nick);
        broadcastClientsList();
        LOGGER.info("String: {}.", "Пользователь " + nick + " сменил ник на " + newNick);
    }

    public void broadcastClientsList() {
        String nicks = clients.values().stream()
                .map(ClientHandler::getNick)
                .collect(Collectors.joining(" "));
        broadcast(MESSAGE, "В чате активны пользователи:\n" + nicks);
        broadcast(CLIENTS, nicks);
    }

    public void broadcast(Command command, String message) {
        for (ClientHandler client : clients.values()) {
            client.sendMessage(command, message);
        }
    }

    public boolean isNickBusy(String nick) {
        return clients.get(nick) != null;
    }

    public void unsubscibe(ClientHandler client) {
        clients.remove(client.getNick());
        broadcastClientsList();
        LOGGER.warn("String: {}.", "Клиент " + client.getNick() + " отключился");
    }

    public void refuseChangeNick(ClientHandler client) {
        client.sendMessage(REFUSE_CHANGE_NICK);

    }
}