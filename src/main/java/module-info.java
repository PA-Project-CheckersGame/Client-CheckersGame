module com.example.hellofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    opens com.example.client to javafx.fxml;
    exports com.example.client;
    exports com.example.client.controllers;
    opens com.example.client.controllers to javafx.fxml;
    exports com.example.client.sessions;
    opens com.example.client.sessions to javafx.fxml;
    exports com.example.client.supportClasses;
    opens com.example.client.supportClasses to javafx.fxml;
    exports com.example.client.serverConnection;
    opens com.example.client.serverConnection to javafx.fxml;
}