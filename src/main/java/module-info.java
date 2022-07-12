module ru.gb.perov.gbjavafxchat {
    requires javafx.controls;
    requires javafx.fxml;

    exports ru.gb.perov.gbjavafxchat.client;
    opens ru.gb.perov.gbjavafxchat.client to javafx.fxml;
}