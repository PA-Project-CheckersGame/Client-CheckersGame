package com.example.client.controllers;

import com.example.client.ServerConnection;
import com.example.client.ServerResponseListener;
import com.example.client.UserSession;
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

public class RegisterController implements ServerResponseListener {
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
    TextField retypePasswordTextField;
    @FXML
    Label errorLabel;

    public RegisterController() {}

    @FXML
    private void register(javafx.event.ActionEvent event){
        if(usernameTextField.getText().isEmpty()){
            errorLabel.setText("Username can't be empty");
        }else if(passwordTextField.getText().isEmpty()){
            errorLabel.setText("Password can't be empty");
        } else{
            String username = usernameTextField.getText();
            String password = passwordTextField.getText();
            String retypePassword = retypePasswordTextField.getText();

            if(password.equals(retypePassword)){
                userSession = UserSession.getInstance(username, password);
                serverConnection.sendRequest("register " + username + " " + password);
            } else {
                errorLabel.setText("Passwords don't match!");
                passwordTextField.clear();
                retypePasswordTextField.clear();
            }
        }
    }
    @FXML
    public void switchToLogin(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginScene.fxml"));
        Parent root = loader.load(); // folosim aici obiectul 'loader'
        stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        // obtin noul controller si ii setez conexiunea la server
        LoginController controller = loader.getController(); // 'loader' este acum cunoscut
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
        System.out.println("Am primit de la server: " + response);
        if (response.startsWith("register")){
            if (response.equals("register failed username_already_exists")){
                userSession.clearUserSession();
                errorLabel.setText("Username already exists!");
            }
            if(response.equals("register ok")){
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
