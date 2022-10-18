module ru.gb.perov.gbjavafxchat {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.apache.logging.log4j;
    requires org.slf4j;

    exports ru.gb.perov.gbjavafxchat.client;
    opens ru.gb.perov.gbjavafxchat.client to javafx.fxml;
}