module ru.gb.perov.gbjavafxchat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports ru.gb.perov.gbjavafxchat.client;
    opens ru.gb.perov.gbjavafxchat.client to javafx.fxml;
}