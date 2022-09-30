package ru.gb.perov.gbjavafxchat.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

import static ru.gb.perov.gbjavafxchat.Command.*;

public class ChatClientApp extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatClientApp.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("Chat Client");
        stage.setScene(scene);
        stage.show();
        ChatController controller = fxmlLoader.getController();
        stage.setOnCloseRequest(event -> {
            controller.sendHistory();
            controller.getClient().sendMessage(END);
        });
    }

    public static void main(String[] args) {
        launch();
    }
}