module ru.gb.perov.gbjavafxchat {
    requires javafx.controls;
    requires javafx.fxml;


    opens ru.gb.perov.gbjavafxchat to javafx.fxml;
    exports ru.gb.perov.gbjavafxchat;
}