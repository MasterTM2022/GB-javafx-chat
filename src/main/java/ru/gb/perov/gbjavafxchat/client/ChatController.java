package ru.gb.perov.gbjavafxchat.client;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.util.Optional;

import static ru.gb.perov.gbjavafxchat.Command.*;

public class ChatController {
    @FXML
    private volatile ProgressBar progressBar;
    @FXML
    private ListView<String> clientList;
    @FXML
    private HBox authBox;
    @FXML
    private TextField loginField;
    @FXML
    private PasswordField password;
    @FXML
    private HBox messageBox;
    @FXML
    private TextArea messageArea;
    @FXML
    private TextField messageField;
    @FXML
    private Label inChatAs;
    private final ChatClient client;
    private String selectedNick;
    @FXML
    private TextField newNick;

    final static int HISTORY_SIZE = 5;

    public void setProgress(double persentage) {
        progressBar.setProgress(persentage);
    }


    public ChatController() {
        this.client = new ChatClient(this);
        while (true) {
            try {
                client.openConnection();
                break;
            } catch (IOException e) {
                showNotification();
            }
        }
    }

    private void showNotification() {
        final Alert alert = new Alert(Alert.AlertType.ERROR,
                "Не могу подключиться к серверу \n" +
                        "проверьте, что серыер подключен и запущен",
                new ButtonType("Попробовать снова", ButtonBar.ButtonData.OK_DONE),
                new ButtonType("Выйти", ButtonBar.ButtonData.CANCEL_CLOSE)
        );
        alert.setTitle("Ошибка подключения!");
        final Optional<ButtonType> answer = alert.showAndWait();
        final Boolean inExit = answer.map(select -> select.getButtonData().isCancelButton()).orElse(false);
        if (inExit) {
            System.exit(0);
        }
    }

    public void clickSendButton() {
        final String message = messageField.getText();
        if (selectedNick != null) {
            client.sendMessage(PRIVATE_MESSAGE, selectedNick, message);
            selectedNick = null;
        } else if (!message.isBlank()) {
            client.sendMessage(MESSAGE, message);
        }
        messageField.clear();
        messageField.requestFocus();
    }

    public void addMessage(String message) {
        messageArea.appendText(message + "\n\n");
    }

    public void setAuth(boolean success) {
        authBox.setVisible(!success);
        messageBox.setVisible(success);
        loginField.clear();
        password.clear();
        messageArea.setText("");
        Platform.runLater(() -> messageField.requestFocus());
    }

    public void signInBtnClick() {
        client.sendMessage(AUTH, loginField.getText(), password.getText());
    }

    public void sendHistory() {
        String[] fullHistory = messageArea.getText().split("\n\n");
//        Arrays.stream(fullHistory).forEach(System.out::println);

        StringBuilder history = new StringBuilder();
        for (int i = fullHistory.length - 1; i >= fullHistory.length - Math.min(HISTORY_SIZE, fullHistory.length); i--) {
            history.append(fullHistory[i]).append("\n\n");
        }
        client.sendMessage(SEND_HISTORY, history.substring(0, history.length() - "\n\n".length()));
    }

    public void showError(String errorMessage) {
        final Alert alert = new Alert(Alert.AlertType.ERROR, errorMessage,
                new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
        alert.setTitle("Error!");
        alert.showAndWait();
    }

    public void selectClient(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            String selectedNick = clientList.getSelectionModel().getSelectedItem();
            if (selectedNick != null && !selectedNick.isEmpty()) {
                this.selectedNick = selectedNick;
            }
        }
    }

    public void updateClientList(String[] clients) {
        clientList.getItems().clear();
        clientList.getItems().addAll(clients);
    }

    public void logOutClick() {
        sendHistory();
        client.sendMessage(END);
    }

    public ChatClient getClient() {
        return client;
    }

    public void ChangeNickClick() {
        if (!newNick.getText().replaceAll(TOKEN_DELIMETR, "").equals("")) {
            final String newNick = this.newNick.getText().replace(' ', '_');
            client.sendMessage(CHANGE_NICK, newNick);
        } else {
            newNick.clear();
        }
    }

    public void setNewNickOnForm() {
        String buf = newNick.getText();
        Platform.runLater(() -> inChatAs.setText("Login as: " + buf));
        System.out.println(inChatAs.getText());
        newNick.clear();
    }

    public void setOldNickOnForm() {
        newNick.clear();
    }

    public void setNickOnForm(String nick) {
        Platform.runLater(() -> inChatAs.setText("Login as: " + nick));
    }
}