package ru.gb.perov.gbjavafxchat.server;

public class ServerLauncher {

    public static void main(String[] args) {
        BeforeStart.prepareBase();

        new ChatServer().run();

    }
}