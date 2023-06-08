package com.example.client.sessions;

public class GameSession {
    private static GameSession instance;
    private int gameId = 0;

    private String player1;
    private String player2;
    private String status;
    private String turn = "player1";

    private GameSession(int gameId, String player1, String player2, String turn){
        this.gameId = gameId;
        this.player1 = player1;
        this.player2 = player2;
        this.turn = turn;
    }
    public static GameSession getInstance(){ return instance;}

    public static GameSession getInstance(int gameId, String player1, String player2, String turn){
        if(instance == null){
            instance = new GameSession(gameId, player1, player2, turn);
        }
        return instance;
    }

    public static void setInstance(GameSession instance) {
        GameSession.instance = instance;
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public String getPlayer1() {
        return player1;
    }

    public void setPlayer1(String player1) {
        this.player1 = player1;
    }

    public String getPlayer2() {
        return player2;
    }

    public void setPlayer2(String player2) {
        this.player2 = player2;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTurn() {
        return turn;
    }

    public void setTurn(String turn) {
        this.turn = turn;
    }

    public void clearGameSession(){
        gameId = 0;
        player1 = null;
        player2 = null;
        status = null;
    }
}
