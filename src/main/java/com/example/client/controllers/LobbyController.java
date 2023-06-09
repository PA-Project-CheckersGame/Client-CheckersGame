package com.example.client.controllers;

import com.example.client.serverConnection.ServerConnection;
import com.example.client.serverConnection.ServerResponseListener;
import com.example.client.sessions.GameSession;
import com.example.client.sessions.UserSession;
import com.example.client.supportClasses.Game;
import com.example.client.supportClasses.Player;
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

import static java.lang.Integer.parseInt;

public class LobbyController implements ServerResponseListener {
    private ServerConnection serverConnection;
    UserSession userSession = UserSession.getInstance();
    private Timeline lobbyTimeline;

    private Stage stage;
    private Scene scene;

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
        usernameColumn.setCellValueFactory(new PropertyValueFactory<Player, String>("name"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<Player, String>("status"));

        idColumn.setCellValueFactory(new PropertyValueFactory<Game, String>("id"));
        player1Column.setCellValueFactory(new PropertyValueFactory<Game, String>("player1"));
        player2Column.setCellValueFactory(new PropertyValueFactory<Game, String>("player2"));

        gamesTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && newSelection.getPlayer2().isEmpty() && userSession.getGameId()==0) {
                joinGameButton.setDisable(false);
            } else {
                joinGameButton.setDisable(true);
            }
        });

        joinGameButton.setOnAction(event -> {
            Game selectedGame = gamesTableView.getSelectionModel().getSelectedItem();
            serverConnection.sendRequest("join_game " + selectedGame.getId() + " " + userSession.getUsername());
        });
    }

    public void setServerConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
        this.serverConnection.addListener(this);
        startSendingLobbyUpdateRequests();
    }

    @FXML
    public void switchToLogin() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/LoginScene.fxml"));
        Parent root = loader.load();
        stage = (Stage)logoutButton.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();


        LoginController controller = loader.getController();
        controller.setServerConnection(serverConnection);
    }

    @FXML
    public void switchToGame() throws  IOException{
      stopSendingLobbyUpdateRequests();
      FXMLLoader loader = new FXMLLoader(getClass().getResource("/GameScene.fxml"));
      Parent root = loader.load();
      stage = (Stage)playersTableView.getScene().getWindow();
      scene = new Scene(root);
      stage.setScene(scene);
      stage.show();

      GameController controller = loader.getController();
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
        leaveGameButton.setDisable(false);
        if(userSession.getGameId() != 0){
            serverConnection.sendRequest("delete_game " + userSession.getGameId());
        }

    }
    @FXML
    private void leaveGame(ActionEvent actionEvent){
        serverConnection.sendRequest("leave_game " + userSession.getGameId());

    }

    public void startGame(ActionEvent actionEvent) {
        serverConnection.sendRequest("start_game " + userSession.getGameId());
    }

    @FXML
    private void logout(ActionEvent actionEvent) {
        serverConnection.sendRequest("logout " + userSession.getUsername());
        stopSendingLobbyUpdateRequests();
        if(userSession.getGameId() != 0){
            serverConnection.sendRequest("delete_game " + userSession.getGameId());
        }
        userSession.clearUserSession();
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
        stopSendingLobbyUpdateRequests();
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
            userSession.setPlayer1(true);
            createGameButton.setDisable(true);
            deleteGameButton.setDisable(false);
        }
        if(responseWords[0].equals("game_deleted")){
            userSession.setGameId(0);
            userSession.setPlayer1(false);
            createGameButton.setDisable(false);
            deleteGameButton.setDisable(true);
        }
        if(responseWords[0].equals("game_joined")){
            userSession.setGameId(Integer.parseInt(responseWords[1]));
            userSession.setPlayer1(false);
            leaveGameButton.setDisable(false);
            createGameButton.setDisable(true);
        }
        if(responseWords[0].equals("game_left")){
            userSession.setGameId(0);
            leaveGameButton.setDisable(true);
            createGameButton.setDisable(false);
        }
        if(responseWords[0].equals("waiting_room_update")){
            if(responseWords[1].equals("game_started")){
                int gameId = Integer.parseInt(responseWords[2]);
                String player1 = responseWords[3];
                String player2 = responseWords[4];
                String turn = responseWords[5];
                GameSession.getInstance(gameId, player1, player2, turn);
                stopSendingLobbyUpdateRequests();
                try {
                    switchToGame();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if(responseWords[1].equals("game_deleted")){
                userSession.setGameId(0);
                userSession.setPlayer1(true);
                createGameButton.setDisable(false);
                leaveGameButton.setDisable(true);
            }
        }
        if(responseWords[0].equals("game_started")){
            int gameId = Integer.parseInt(responseWords[1]);
            String player1 = responseWords[2];
            String player2 = responseWords[3];
            String turn = responseWords[4];
            GameSession.getInstance(gameId, player1, player2, turn);
            stopSendingLobbyUpdateRequests();
            try {
                switchToGame();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
    private void startSendingLobbyUpdateRequests() {
        lobbyTimeline = new Timeline(
                new KeyFrame(Duration.seconds(3), event -> {
                    serverConnection.sendRequest("get_players_list");
                    serverConnection.sendRequest("get_games_list");
                    if (userSession.getGameId() != 0 && !userSession.isPlayer1()) {
                        serverConnection.sendRequest("get_waiting_room_update " + userSession.getGameId());
                    }
                })
        );

        lobbyTimeline.setCycleCount(Timeline.INDEFINITE);
        lobbyTimeline.play();
    }

    public void stopSendingLobbyUpdateRequests() {
        if (lobbyTimeline != null) {
            lobbyTimeline.stop();
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
        if(userSession.getGameId() != 0 && !gameList.isEmpty()) {
            for (Game game : gameList) {
                if (Integer.parseInt(game.getId()) == userSession.getGameId() && game.getPlayer1().equals(userSession.getUsername()) && !game.getPlayer2().equals("")) {
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
