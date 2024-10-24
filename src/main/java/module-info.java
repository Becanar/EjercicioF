module com.example.ejerciciof {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.ejerciciof to javafx.fxml;
    exports com.example.ejerciciof.model;
    opens com.example.ejerciciof.model to javafx.fxml;
    exports com.example.ejerciciof.app;
    opens com.example.ejerciciof.app to javafx.fxml;
}