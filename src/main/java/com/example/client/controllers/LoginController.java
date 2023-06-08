package com.example.client.controllers;
import com.example.client.serverConnection.ServerConnection;
import com.example.client.serverConnection.ServerResponseListener;
import com.example.client.sessions.UserSession;
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

    private UserSession userSession;
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
        if(usernameTextField.getText().isEmpty()){
            errorLabel.setText("Username can't be empty");
        }else if(passwordTextField.getText().isEmpty()){
            errorLabel.setText("Password can't be empty");
        }else {
            String username = usernameTextField.getText();
            String password = passwordTextField.getText();

            userSession = UserSession.getInstance(username, password);
            serverConnection.sendRequest("login " + username + " " + password);
        }
    }

    @FXML
    private void switchToRegister(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/RegisterScene.fxml"));
        Parent root = loader.load(); // folosim aici obiectul 'loader'
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        // obtin noul controller si ii setez conexiunea la server
        RegisterController controller = loader.getController(); // 'loader' este acum cunoscut
        controller.setServerConnection(serverConnection);
    }

    @FXML
    private void switchToLobby() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/LobbyScene.fxml"));
        Parent root = loader.load(); // folosim aici obiectul 'loader'
        stage = (Stage)usernameTextField.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        // obtin noul controller si ii setez conexiunea la server
        LobbyController controller = loader.getController(); // 'loader' este acum cunoscut
        controller.setServerConnection(serverConnection);
    }



    public void setServerConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
        this.serverConnection.addListener(this);
    }

    @Override
    public void onServerResponse(String response) {
        if (response.startsWith("login")) {
            if(response.equals("login failed username_not_registered")){
                userSession.clearUserSession();
                errorLabel.setText("Username not registered!");
            }
            if(response.equals("login failed wrong_password")){
                userSession.clearUserSession();
                errorLabel.setText("Wrong password!");
            }
            if(response.equals("login failed already_online")){
                userSession.clearUserSession();
                errorLabel.setText("User already online!");
            }
            if(response.equals("login ok")){
                try {
                    switchToLobby();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                errorLabel.setText("");
            }
        }
    }
}
