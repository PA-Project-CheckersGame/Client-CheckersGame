package com.example.client.gameLogic;

public enum PieceType {
    RED(1), WHITE(-1), RED_KING(0), WHITE_KING(0);
    public final int moveDir;
    PieceType(int moveDir){
        this.moveDir = moveDir;
    }
}
