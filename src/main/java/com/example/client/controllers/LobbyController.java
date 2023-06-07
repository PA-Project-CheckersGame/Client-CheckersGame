package com.example.client.controllers;

import com.example.client.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static java.lang.Integer.parseInt;

public class LobbyController implements ServerResponseListener {
    private ServerConnection serverConnection;
    UserSession userSession = UserSession.getInstance();
    private Timeline timeline;

    List<Player> playersList = new ArrayList<>();
    List<Game> gamesList = new ArrayList<>();

    private Stage stage;
    private Scene scene;
    private Parent root;

    @FXML
    private TableView<Player> playersTableView;
    @FXML
    private TableColumn<Player, String> usernameColumn;
    @FXML
    private TableColumn<Player, String> statusColumn;

    @FXML
    private TableView<Game> gamesTableView;
    @FXML
    private TableColumn<Game, String> idColumn;
    @FXML
    private TableColumn<Game, String> player1Column;
    @FXML
    private TableColumn<Game, String> player2Column;
    @FXML
    Button createGameButton;
    @FXML
    Button deleteGameButton;
    @FXML
    Button joinGameButton;
    @FXML
    Button leaveGameButton;
    @FXML
    Button startGameButton;
    @FXML
    Button logoutButton;
    @FXML
    Button exitButton;

    public void initialize() {
        // Setează proprietatea pentru fiecare coloană a tabelului de jucători
        usernameColumn.setCellValueFactory(new PropertyValueFactory<Player, String>("name"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<Player, String>("status"));

        // Setează proprietatea pentru fiecare coloană a tabelului de jocuri
        idColumn.setCellValueFactory(new PropertyValueFactory<Game, String>("id"));
        player1Column.setCellValueFactory(new PropertyValueFactory<Game, String>("player1"));
        player2Column.setCellValueFactory(new PropertyValueFactory<Game, String>("player2"));

        // 1. Adaugăm un ascultător la selecția din TableView
        gamesTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            // 2. Verificăm dacă jucătorul 2 este gol
            if (newSelection != null && newSelection.getPlayer2().isEmpty()) {
                // Dacă este, activăm butonul joinGame
                joinGameButton.setDisable(false);
            } else {
                // Altfel, îl dezactivăm
                joinGameButton.setDisable(true);
            }
        });

// 3. Adăugăm un handler pentru evenimentul de click al butonului joinGame
        joinGameButton.setOnAction(event -> {
            // Aflăm care este jocul selectat
            Game selectedGame = gamesTableView.getSelectionModel().getSelectedItem();
            // Trimitem mesajul la server
            serverConnection.sendRequest("join_game " + selectedGame.getId() + " " + userSession.getUsername());
        });
    }

    public void setServerConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
        this.serverConnection.addListener(this);
        startSendingUpdateRequests();
    }

    @FXML
    public void switchToLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginScene.fxml"));
        Parent root = loader.load(); // folosim aici obiectul 'loader'
        stage = (Stage)logoutButton.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        // obtin noul controller si ii setez conexiunea la server
        LoginController controller = loader.getController(); // 'loader' este acum cunoscut
        controller.setServerConnection(serverConnection);
    }
    @FXML
    private void createGame(ActionEvent actionEvent) {
        serverConnection.sendRequest("create_game " + userSession.getUsername());
    }
    @FXML
    private void deleteGame(ActionEvent actionEvent){
        serverConnection.sendRequest("delete_game " + userSession.getGameId());
    }

    @FXML
    private void joinGame(ActionEvent actionEvent){
        Game selectedGame = gamesTableView.getSelectionModel().getSelectedItem();
        serverConnection.sendRequest("join_game " + selectedGame.getId() + " " + userSession.getUsername());
        if(userSession.getGameId() != 0){
            serverConnection.sendRequest("delete_game " + userSession.getGameId());
        }
    }
    @FXML
    private void leaveGame(ActionEvent actionEvent){
        serverConnection.sendRequest("leave_game " + userSession.getGameId());
    }
    @FXML
    private void logout(ActionEvent actionEvent) {
        serverConnection.sendRequest("logout " + userSession.getUsername());
        userSession.clearUserSession();
        stopSendingUpdateRequests();
        try {
            switchToLogin();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    @FXML
    private void exit(ActionEvent actionEvent) {
        serverConnection.sendRequest("logout " + userSession.getUsername());
        userSession.clearUserSession();
        stopSendingUpdateRequests();
        Stage stage = (Stage) exitButton.getScene().getWindow();
        stage.close();
    }

    @Override
    public void onServerResponse(String response) {
        if (response.startsWith("players_list")) {
            processPlayerListResponse(response);
        }
        if (response.startsWith("games_list")) {
            processGameListResponse(response);
        }

        String[] responseWords = response.split(" ");

        if(responseWords[0].equals("game_created")){
            userSession.setGameId(parseInt(responseWords[1]));
            createGameButton.setDisable(true);
            deleteGameButton.setDisable(false);
        }
        if(responseWords[0].equals("game_deleted")){
            userSession.setGameId(0);
            createGameButton.setDisable(false);
            deleteGameButton.setDisable(true);
        }
        if(responseWords[0].equals("game_joined")){
            userSession.setGameId(Integer.parseInt(responseWords[1]));
            leaveGameButton.setDisable(false);
            createGameButton.setDisable(true);
        }
        if(responseWords[0].equals("game_left")){
            userSession.setGameId(0);
            leaveGameButton.setDisable(true);
            createGameButton.setDisable(false);
        }
    }
    private void startSendingUpdateRequests(){
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(10), event -> {
                    serverConnection.sendRequest("get_players_list");
                    serverConnection.sendRequest("get_games_list");
                })
        );

        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    public void stopSendingUpdateRequests() {
        if (timeline != null) {
            timeline.stop();
        }
    }

    public void processPlayerListResponse(String response) {
        ArrayList<Player> playerList = new ArrayList<>();
        String[] words = response.split(" ");
        for (int i = 1; i < words.length; i += 2) {
            String name = words[i];
            String status = words[i + 1];
            playerList.add(new Player(name, status));
        }
        ObservableList<Player> observablePlayerList = FXCollections.observableArrayList(playerList);
        playersTableView.setItems(observablePlayerList);
    }
    public void processGameListResponse(String response) {
        ArrayList<Game> gameList = new ArrayList<>();
        String[] words = response.split(" ");
        for (int i = 1; i < words.length; i += 3) {
            String id = words[i];
            String player1 = words[i + 1];
            String player2 = words[i + 2];
            if(player2.equals("-")){
                gameList.add(new Game(id, player1, ""));
            } else {
                gameList.add(new Game(id, player1, player2));
            }
        }
        if(userSession.getGameId() != 0 && !gamesList.isEmpty()) {
            for (Game game : gamesList) {
                if (Integer.parseInt(game.getId()) == userSession.getGameId()&& game.getPlayer1().equals(userSession.getUsername()) && !game.getPlayer2().equals("")) {
                    startGameButton.setDisable(false);
                    break;
                }
                else{
                    startGameButton.setDisable(true);
                }
            }
        }


        ObservableList<Game> observableGameList = FXCollections.observableArrayList(gameList);
        gamesTableView.setItems(observableGameList);
    }
}
