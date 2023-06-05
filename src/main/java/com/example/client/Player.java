package com.example.client;

public class Player {
    private String name = "";
    private String password = "";

    public Player() {}
    public Player(String name, String password) {
        this.name = name;
        this.password = password;
    }
    public Player(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
