module com.example.bomberman {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires com.almasb.fxgl.all;

    opens com.example.bomberman to javafx.fxml;
    exports com.example.bomberman;
    exports com.example.bomberman.map;
    opens com.example.bomberman.map to javafx.fxml;
}