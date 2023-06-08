package com.example.client.controllers;

import com.example.client.sessions.GameSession;
import com.example.client.serverConnection.ServerConnection;
import com.example.client.serverConnection.ServerResponseListener;
import com.example.client.sessions.UserSession;
import com.example.client.gameLogic.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class GameController  implements ServerResponseListener {
    private ServerConnection serverConnection;
    private UserSession userSession = UserSession.getInstance();
    private GameSession gameSession = GameSession.getInstance();
    private Timeline timeline;

    public static final int TILE_SIZE = 100;
    public static final int WIDTH = 8;
    public static final int HEIGHT = 8;
    private Tile[][] board = new Tile[WIDTH][HEIGHT];
    private Group tileGroup = new Group();
    private Group pieceGroup = new Group();

    private PieceType userColor;
    private PieceType currentTurn;
    private int redPiecesCount = 0;
    private int whitePiecesCount = 0;

    private Stage stage;
    private Scene scene;

    @FXML
    Pane gameBoard;
    @FXML
    Label player1UsernameLabel;
    @FXML
    Label player2UsernameLabel;
    @FXML
    Label turnLabel;
    @FXML
    Label playerTurnLabel;
    @FXML
    Label redPiecesCountLabel;
    @FXML
    Label whitePiecesCountLabel;
    @FXML
    Button leaveGameButton;
    @FXML
    Button exitGameButton;


    public void initialize(){
        leaveGameButton.setText("Leave game");
        leaveGameButton.setTextFill(Color.RED);
        createGameBoard();
        currentTurn = PieceType.WHITE;
        if(userSession.getUsername().equals(gameSession.getPlayer1())){
            userColor = PieceType.RED;
        } else if(userSession.getUsername().equals(gameSession.getPlayer2())){
            userColor = PieceType.WHITE;
        }
        player1UsernameLabel.setText(gameSession.getPlayer1());
        player2UsernameLabel.setText(gameSession.getPlayer2());
    }

    private Parent createGameBoard() {

        gameBoard.setPrefSize(WIDTH * TILE_SIZE, HEIGHT * TILE_SIZE);
        gameBoard.getChildren().addAll(tileGroup, pieceGroup);

        //creating the initial arrangement of pieces
        for(int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++){
                Tile tile = new Tile((x+y) % 2 == 0, x, y);
                board[x][y] = tile;

                tileGroup.getChildren().add(tile);

                Piece piece = null;

                if(y <= 2 && (x + y) % 2 != 0){
                    piece = makePiece(PieceType.RED, x, y);
                }

                if(y >= 5 && (x + y) % 2 != 0){
                    piece = makePiece(PieceType.WHITE, x, y);
                }

                if(piece != null) {
                    tile.setPiece(piece);
                    pieceGroup.getChildren().add(piece);
                }
            }
        }

        return gameBoard;
    }

    private MoveResult tryMove(Piece piece, int newX, int newY){

        if(currentTurn != userColor){
            return new MoveResult(MoveType.NONE);
        }

        if(currentTurn == PieceType.RED && (piece.getType() == PieceType.WHITE || piece.getType() == PieceType.WHITE_KING)){
            return new MoveResult(MoveType.NONE);
        }
        if(currentTurn == PieceType.WHITE && (piece.getType() == PieceType.RED || piece.getType() == PieceType.RED_KING)){
            return new MoveResult(MoveType.NONE);
        }

        if(board[newX][newY].hasPiece() || (newX + newY) % 2 == 0) {
            return new MoveResult(MoveType.NONE);
        }
        int x0 = toBoard(piece.getOldX());
        int y0 = toBoard(piece.getOlxY());

        //NORMAL PIECES
        if(piece.getType() == PieceType.RED || piece.getType() == PieceType.WHITE) {
            if (Math.abs(newX - x0) == 1 && newY - y0 == piece.getType().moveDir) {
                return new MoveResult(MoveType.NORMAL);
            } else if (Math.abs(newX - x0) == 2 && newY - y0 == piece.getType().moveDir * 2) {
                int x1 = x0 + (newX - x0) / 2;
                int y1 = y0 + (newY - y0) / 2;

                if (board[x1][y1].hasPiece() && board[x1][y1].getPiece().getType() != piece.getType()) {
                    return new MoveResult(MoveType.KILL, board[x1][y1].getPiece());
                }
            }
        }
        //KINGS
        if(piece.getType() == PieceType.RED_KING || piece.getType() == PieceType.WHITE_KING) {
            if (Math.abs(newX - x0) == 1 && Math.abs(newY - y0) == 1) {
                return new MoveResult(MoveType.NORMAL);
            } else if (Math.abs(newX - x0) == 2 && Math.abs(newY - y0) == 2) {
                int x1 = x0 + (newX - x0) / 2;
                int y1 = y0 + (newY - y0) / 2;

                if (board[x1][y1].hasPiece()) {
                    if(piece.getType() == PieceType.RED_KING && (board[x1][y1].getPiece().getType() == PieceType.WHITE || board[x1][y1].getPiece().getType() == PieceType.WHITE_KING)) {
                        return new MoveResult(MoveType.KILL, board[x1][y1].getPiece());
                    }
                    if(piece.getType() == PieceType.WHITE_KING && (board[x1][y1].getPiece().getType() == PieceType.RED || board[x1][y1].getPiece().getType() == PieceType.RED_KING)){
                        return new MoveResult(MoveType.KILL, board[x1][y1].getPiece());
                    }
                }
            }
        }
        return new MoveResult(MoveType.NONE);
    }

    private Piece makePiece(PieceType type, int x, int y){
        Piece piece = new Piece(type, x, y);

        piece.setOnMouseReleased(e -> {

            int newX = toBoard(piece.getLayoutX());
            int newY = toBoard(piece.getLayoutY());

            if (newX < 0 || newY < 0 || newX > 7 || newY > 7) {
                piece.abortMove();
                return;
            }

            MoveResult result = tryMove(piece, newX, newY);

            int x0 = toBoard(piece.getOldX());
            int y0 = toBoard(piece.getOlxY());

            switch (result.getType()){
                case NONE:
                    piece.abortMove();
                    break;

                case NORMAL:
                    piece.move(newX, newY);
                    if(piece.getType() == PieceType.RED && newY == 7){
                        piece.changeAppearance(PieceType.RED_KING);
                    } else if (piece.getType() == PieceType.WHITE && newY == 0) {
                        piece.changeAppearance(PieceType.WHITE_KING);
                    }
                    board[x0][y0].setPiece(null);
                    board[newX][newY].setPiece(piece);
                    switchTurns();
                    sendGameMoveRequest();
                    break;

                case KILL:
                    piece.move(newX, newY);
                    if(piece.getType() == PieceType.RED && newY == 7){
                        piece.changeAppearance(PieceType.RED_KING);
                    } else if (piece.getType() == PieceType.WHITE && newY == 0) {
                        piece.changeAppearance(PieceType.WHITE_KING);
                    }
                    board[x0][y0].setPiece(null);
                    board[newX][newY].setPiece(piece);

                    Piece otherPiece = result.getPiece();
                    board[toBoard(otherPiece.getOldX())][toBoard(otherPiece.getOlxY())].setPiece(null);
                    pieceGroup.getChildren().remove(otherPiece);
                    switchTurns();
                    sendGameMoveRequest();
                    break;
            }
        });

        return piece;
    }

    private int toBoard(double pixel){
        return (int)(pixel + TILE_SIZE / 2) / TILE_SIZE;
    }
    private void switchTurns(){
        currentTurn = (currentTurn == PieceType.RED) ? PieceType.WHITE : PieceType.RED;
        if(currentTurn == PieceType.RED && userColor == PieceType.RED) {
            playerTurnLabel.setText("Your Turn!");
            playerTurnLabel.setTextFill(Color.GREEN);
        } else if (currentTurn == PieceType.RED && userColor == PieceType.WHITE){
            playerTurnLabel.setText(gameSession.getPlayer2() + "'s turn!");
            playerTurnLabel.setTextFill(Color.RED);
        }
        if(currentTurn == PieceType.WHITE && userColor == PieceType.WHITE){
            playerTurnLabel.setText("Your turn!");
            playerTurnLabel.setTextFill(Color.GREEN);
        } else if (currentTurn == PieceType.WHITE && userColor == PieceType.RED){
            playerTurnLabel.setText(gameSession.getPlayer1() + "'s turn!");
            playerTurnLabel.setTextFill(Color.RED);
        }
        countPieces();
        redPiecesCountLabel.setText(String.valueOf(redPiecesCount));
        whitePiecesCountLabel.setText(String.valueOf(whitePiecesCount));
        checkEndGame();
    }
    private void countPieces() {
        redPiecesCount = 0;
        whitePiecesCount = 0;

        for(int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++){
                Piece piece = board[x][y].getPiece();
                if (piece != null) {
                    if (piece.getType() == PieceType.RED || piece.getType() == PieceType.RED_KING) {
                        redPiecesCount++;
                    } else if (piece.getType() == PieceType.WHITE || piece.getType() == PieceType.WHITE_KING) {
                        whitePiecesCount++;
                    }
                }
            }
        }
    }

    public boolean canMove(PieceType type) {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Piece piece = board[x][y].getPiece();
                if (piece != null && (piece.getType() == type)){
                    // Try all possible directions
                    int[] dx = {-1, 1, -1, 1, -2, 2, -2, 2};
                    int[] dy = {-1, -1, 1, 1, -2, -2, 2, 2};
                    for (int dir = 0; dir < 8; dir++) {
                        int newX = x + dx[dir];
                        int newY = y + dy[dir];
                        if (newX >= 0 && newY >= 0 && newX < WIDTH && newY < HEIGHT) {
                            MoveResult result = tryMove(piece, newX, newY);
                            if (result.getType() != MoveType.NONE) {
                                System.out.println("Player " + type + " can move");
                                return true;
                            }
                        }
                    }
                }
            }
        }
        System.out.println("Player " + type + " CAN NOT move");
        return false;
    }

    public void checkEndGame() {
        if ((currentTurn == PieceType.RED && !canMove(PieceType.RED)) || redPiecesCount == 0) {
            if(userColor == PieceType.RED) {
                turnLabel.setText("Game Ended!");
                playerTurnLabel.setText("You Lost!");
                playerTurnLabel.setTextFill(Color.RED);
                gameSession.setStatus("player1");
                serverConnection.sendRequest("set_game_status game_ended won" + gameSession.getStatus());
            } else if (userColor == PieceType.WHITE) {
                turnLabel.setText("Game Ended!");
                playerTurnLabel.setText("You Won!");
                playerTurnLabel.setTextFill(Color.GREEN);
                gameSession.setStatus("player1");
                serverConnection.sendRequest("set_game_status game_ended won" + gameSession.getStatus());
            }
        } else if ((currentTurn == PieceType.WHITE &&!canMove(PieceType.WHITE)) || whitePiecesCount == 0) {
            if(userColor == PieceType.RED) {
                turnLabel.setText("Game Ended!");
                playerTurnLabel.setText("You Won!");
                playerTurnLabel.setTextFill(Color.GREEN);
                gameSession.setStatus("player2");
                serverConnection.sendRequest("set_game_status game_ended won" + gameSession.getStatus());
            } else if (userColor == PieceType.WHITE) {
                turnLabel.setText("Game Ended!");
                playerTurnLabel.setText("You lost!");
                playerTurnLabel.setTextFill(Color.RED);
                gameSession.setStatus("player2");
                serverConnection.sendRequest("set_game_status game_ended won" + gameSession.getStatus());
            }
        }else  if (!canMove(PieceType.RED) && !canMove(PieceType.WHITE)) {
            turnLabel.setText("Game Ended!");
            playerTurnLabel.setText("Draw!");
            playerTurnLabel.setTextFill(Color.GRAY);
            gameSession.setStatus("draw");
            serverConnection.sendRequest("set_game_status game_ended" + gameSession.getStatus());
        }
    }

    public void sendGameMoveRequest() {
        StringBuilder boardString = new StringBuilder();
        boardString.append("set_game_move ");
        boardString.append(gameSession.getGameId());
        if(currentTurn == PieceType.RED) {
            boardString.append(" " + gameSession.getPlayer2());
        }
        if(currentTurn == PieceType.WHITE) {
            boardString.append(" " + gameSession.getPlayer1());
        }
        boardString.append(" ");
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Piece piece = board[x][y].getPiece();
                if (piece == null) {
                    boardString.append("0 ");
                } else {
                    switch (piece.getType()) {
                        case WHITE:
                            boardString.append("1 ");
                            break;
                        case RED:
                            boardString.append("2 ");
                            break;
                        case WHITE_KING:
                            boardString.append("3 ");
                            break;
                        case RED_KING:
                            boardString.append("4 ");
                            break;
                    }
                }
            }
        }

        serverConnection.sendRequest(boardString.toString().trim());
    }
    public void updateGameBoard(String boardString) {
        String[] values = boardString.split(" ");

        String gameUpdate = values[0]; // "game_update"
        String gameStatus = values[1]; // "turn"
        if(gameStatus.equals(userSession.getUsername())){
            currentTurn = userColor;
        }else {
            if (userColor == PieceType.RED){
                currentTurn = PieceType.WHITE;
            } else if (userColor == PieceType.WHITE) {
                currentTurn = PieceType.RED;
            }
        }

        // Clear the current board pieces
        pieceGroup.getChildren().clear();
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                board[x][y].setPiece(null);
            }
        }

        // Populate the board with new pieces based on the input string
        // Start from index 2 because the first two elements are the status words
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                int index = (y * WIDTH + x) + 2; // "+ 2" to skip the status words
                String value = values[index];
                Piece piece = null;

                switch (value) {
                    case "1":
                        piece = makePiece(PieceType.WHITE, x, y);
                        break;
                    case "2":
                        piece = makePiece(PieceType.RED, x, y);
                        break;
                    case "3":
                        piece = makePiece(PieceType.WHITE_KING, x, y);
                        break;
                    case "4":
                        piece = makePiece(PieceType.RED_KING, x, y);
                        break;
                }

                if (piece != null) {
                    board[x][y].setPiece(piece);
                    pieceGroup.getChildren().add(piece);
                }
            }
        }

        // Optionally, use gameUpdate and gameStatus variables as needed.
    }

    public void setServerConnection(ServerConnection serverConnection) {
        this.serverConnection = serverConnection;
        this.serverConnection.addListener(this);
    }

    @Override
    public void onServerResponse(String response) {
        if(response.startsWith("game_update")){
           updateGameBoard(response);
        }

        String[] values = response.split(" ");
        if(values[0].equals("game_status")){
            if(values[1].equals("game_ended")){
                turnLabel.setText("Game ended!");
                gameSession.setStatus("game_ended");
                if(values[2].equals("won")){
                    if(values[3].equals("player1")){
                        if(userSession.getUsername().equals(gameSession.getPlayer1())){
                            playerTurnLabel.setText("You won!");
                            playerTurnLabel.setTextFill(Color.GREEN);
                        }else {
                            playerTurnLabel.setText("You lost!");
                            playerTurnLabel.setTextFill(Color.RED);
                        }
                    }else if(values[3].equals("player2")){
                        if(userSession.getUsername().equals(gameSession.getPlayer2())){
                            playerTurnLabel.setText("You won!");
                            playerTurnLabel.setTextFill(Color.GREEN);
                        }else {
                            playerTurnLabel.setText("You lost!");
                            playerTurnLabel.setTextFill(Color.RED);
                        }
                    }
                } else if (values[2].equals("draw")) {
                    playerTurnLabel.setText("Draw!");
                    playerTurnLabel.setTextFill(Color.GRAY);
                } else if (values[2].equals("forfeit")){
                    if(values[3].equals("player1")){
                        if(userSession.getUsername().equals(gameSession.getPlayer2())){
                            playerTurnLabel.setText("You won!");
                            playerTurnLabel.setTextFill(Color.GREEN);
                        }
                    }else if(values[3].equals("player2")){
                        if(userSession.getUsername().equals(gameSession.getPlayer1())){
                            playerTurnLabel.setText("You won!");
                            playerTurnLabel.setTextFill(Color.GREEN);
                        }
                    }
                }
                stopSendingUpdateRequests();
                leaveGameButton.setText("Go back to lobby");
                leaveGameButton.setTextFill(Color.GREEN);
            }
        }
    }

    private void startSendingUpdateRequests(){
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(3), event -> {
                    serverConnection.sendRequest("get_game_update " + gameSession.getGameId());
                    serverConnection.sendRequest("get_game_status " + gameSession.getGameId());
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

    public void leaveGame(ActionEvent actionEvent) {
        if(gameSession.getStatus().equals("game_ended")){
            gameSession.clearGameSession();
            userSession.setGameId(0);
            try {
                switchToLobby();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }else {
            serverConnection.sendRequest("set_status game_ended forfeit " + userSession.getUsername());
            gameSession.clearGameSession();
            userSession.setGameId(0);
            stopSendingUpdateRequests();
            try {
                switchToLobby();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @FXML
    private void switchToLobby() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/LobbyScene.fxml"));
        Parent root = loader.load(); // folosim aici obiectul 'loader'
        stage = (Stage)leaveGameButton.getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();

        // obtin noul controller si ii setez conexiunea la server
        LobbyController controller = loader.getController(); // 'loader' este acum cunoscut
        controller.setServerConnection(serverConnection);
    }


    public void exitGame(ActionEvent actionEvent) {
        serverConnection.sendRequest("logout " + userSession.getUsername());
        gameSession.clearGameSession();
        userSession.clearUserSession();
        stopSendingUpdateRequests();
        Stage stage = (Stage) exitGameButton.getScene().getWindow();
        stage.close();
    }
}
