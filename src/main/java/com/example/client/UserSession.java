package com.example.client;

public class UserSession {
    private static UserSession instance;

    private String username;
    private String password;

    private int gameId = 0;

    private UserSession(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static UserSession getInstance(String username, String password) {
        if(instance == null) {
            instance = new UserSession(username, password);
        }
        return instance;
    }

    public static UserSession getInstance() {
        return instance;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setGameId(int gameId){
        this.gameId = gameId;
    }
    public int getGameId(){
        return gameId;
    }

    public void clearUserSession() {
        username = null;
        password = null;
        instance = null;
    }
}
