package com.example.client.gameLogic;

import com.example.client.controllers.GameController;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;

public class Piece extends StackPane {
    private PieceType type;

    private double mouseX, mouseY;
    private double oldX, olxY;

    private Ellipse bg;
    private Ellipse ellipse;

    public PieceType getType(){

        return type;
    }
    public void setType(PieceType type){
        this.type = type;
    }

    public double getOldX(){
        return oldX;
    }
    public double getOlxY(){
        return olxY;
    }

    public Piece(PieceType type, int x, int y) {
        this.type = type;

        move(x, y);

        bg = new Ellipse(GameController.TILE_SIZE * 0.3125, GameController.TILE_SIZE * 0.26);
        bg.setFill(Color.BLACK);

        bg.setStroke(Color.BLACK);
        bg.setStrokeWidth(GameController.TILE_SIZE * 0.03);

        bg.setTranslateX((GameController.TILE_SIZE - GameController.TILE_SIZE * 0.3125 * 2) / 2);
        bg.setTranslateY((GameController.TILE_SIZE - GameController.TILE_SIZE * 0.26 * 2) / 2 + GameController.TILE_SIZE * 0.07);

        ellipse = new Ellipse(GameController.TILE_SIZE * 0.3125, GameController.TILE_SIZE * 0.26);
        updateEllipseAppearance();

        getChildren().addAll(bg, ellipse);

        setOnMousePressed(e -> {
            mouseX = e.getSceneX();
            mouseY = e.getSceneY();
        });

        setOnMouseDragged(e -> {
            relocate(e.getSceneX() - mouseX + oldX, e.getSceneY() - mouseY + olxY);
        });
    }

    private void updateEllipseAppearance() {
        if(type == PieceType.RED || type == PieceType.WHITE) {
            ellipse.setFill(type == PieceType.RED ? Color.RED : Color.WHITE);
        }
        if(type == PieceType.RED_KING || type == PieceType.WHITE_KING){
            ellipse.setFill(type == PieceType.RED_KING ? Color.RED : Color.WHITE);
            ellipse.setStrokeWidth(9);
            ellipse.setStroke(Color.BLACK);
        }
        ellipse.setTranslateX((GameController.TILE_SIZE - GameController.TILE_SIZE * 0.3125 * 2) / 2);
        ellipse.setTranslateY((GameController.TILE_SIZE - GameController.TILE_SIZE * 0.26 * 2) / 2);
    }

    public void changeAppearance(PieceType newType) {
        this.type = newType;
        updateEllipseAppearance();
    }

    public void move(int x, int y){
        oldX = x * GameController.TILE_SIZE;
        olxY = y * GameController.TILE_SIZE;
        relocate(oldX, olxY);
    }

    public void abortMove() {
        relocate(oldX, olxY);
    }

}
