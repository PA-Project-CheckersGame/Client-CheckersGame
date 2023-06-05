package com.example.client;

import com.example.client.controllers.LoginController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private static ServerConnection serverConnection;

    @Override
    public void start(Stage primaryStage) throws IOException {
        // creez o conexiune la server
        serverConnection = new ServerConnection("127.0.0.1", 8069);

        // incarc fxml-ul pentru loginScene
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginScene.fxml"));
        Parent root = loader.load();

        // obtin controller-ul si ii dau conexiunea la server
        LoginController controller = loader.getController();
        controller.setServerConnection(serverConnection);

        primaryStage.setTitle("The Checkers Game");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}