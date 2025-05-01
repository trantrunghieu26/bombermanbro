module com.example.bomberman {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires com.almasb.fxgl.all;
    requires java.desktop;
    requires javafx.media;

    opens com.example.bomberman to javafx.fxml;
    exports com.example.bomberman;
}