package com.example.client.gameLogic;

import com.example.client.controllers.GameController;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Tile extends Rectangle {
    private Piece piece;

    public boolean hasPiece(){
        return piece != null;
    }

    public Piece getPiece(){
        return piece;
    }

    public void setPiece(Piece piece){
        this.piece = piece;
    }

    public Tile(boolean light, int x, int y) {
        setWidth(GameController.TILE_SIZE);
        setHeight(GameController.TILE_SIZE);

        relocate(x * GameController.TILE_SIZE, y * GameController.TILE_SIZE);

        setFill(light ? Color.valueOf("#feb") : Color.valueOf("#582"));
    }
}