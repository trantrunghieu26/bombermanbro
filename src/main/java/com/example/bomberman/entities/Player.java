package com.example.bomberman.entities;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;

public class Player {
    private double x, y;
    private static final int TILE_SIZE = 32;
    private TileMap tileMap; // Tham chiếu đến TileMap để kiểm tra va chạm

    public Player(double x, double y, TileMap tileMap) {
        this.x = x;
        this.y = y;
        this.tileMap = tileMap;
    }

    public void move(KeyCode key) {
        double newX = x;
        double newY = y;

        switch (key) {
            case W:
                newY -= TILE_SIZE; // Di chuyển lên 1 ô
                break;
            case S:
                newY += TILE_SIZE; // Di chuyển xuống 1 ô
                break;
            case A:
                newX -= TILE_SIZE; // Di chuyển trái 1 ô
                break;
            case D:
                newX += TILE_SIZE; // Di chuyển phải 1 ô
                break;
            default:
                return; // Không di chuyển nếu phím không hợp lệ
        }

        // Kiểm tra va chạm với tường
        if (!tileMap.isWall(newX, newY)) {
            x = newX;
            y = newY;
        }
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(Color.BLUE);
        gc.fillOval(x, y, TILE_SIZE, TILE_SIZE);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}