package com.example.client.controllers;
import com.example.client.ServerConnection;
import com.example.client.ServerResponseListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController implements ServerResponseListener {

    private ServerConnection serverConnection;
    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    TextField usernameTextField;
    @FXML
    TextField passwordTextField;
    @FXML
    Label errorLabel;

    public LoginController(){}



    @FXML
    private void login(javafx.event.ActionEvent event) {
        //extracting credentials from TextFields
        String username = usernameTextField.getText();
        String password = passwordTextField.getText();

        serverConnection.sendRequest("login " + username + " " + password);

//        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
//        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
//        scene = new Scene(root);
//        stage.setScene(scene);
//        stage.show();
    }

    @FXML
    private void switchToRegister(MouseEvent event) throws IOException{
        Parent root = FXMLLoader.load(getClass().getResource("/RegisterScene.fxml"));
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }
    public void switchToLobby(){
        //TODO
    }

    public void setServerConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
        this.serverConnection.addListener(this);
    }

    @Override
    public void onServerResponse(String response) {
        if (response.startsWith("login")) {
            if(response.equals("login failed username_not_registered")){
                errorLabel.setText("Username not registere");
            }
            if(response.equals("login failed wrong_password")){
                errorLabel.setText("Wrong Password");
            }
            if(response.equals("login ok")){
                switchToLobby();
                errorLabel.setText("");
            }
        }
    }
}
