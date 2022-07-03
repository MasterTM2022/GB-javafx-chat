package ru.gb.perov.gbjavafxchat.client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class ChatController {
    @FXML
    private TextArea messageArea;
    @FXML
    private TextField messageField;


    public void clickSendButton(ActionEvent actionEvent) {
        final String message = messageField.getText();
        if (!message.isBlank()) {
            messageArea.appendText(message + "\n\n");
        }
        messageField.clear();
        messageField.requestFocus();
    }
}