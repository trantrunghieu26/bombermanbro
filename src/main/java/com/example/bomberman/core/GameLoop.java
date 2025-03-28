package com.example.bomberman.core;

import com.example.bomberman.entities.Player;
import com.example.bomberman.entities.TileMap;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;

public class GameLoop extends AnimationTimer {
    private GraphicsContext gc;
    private Player player;
    private TileMap tileMap;

    public GameLoop(GraphicsContext gc, Player player, TileMap tileMap) {
        this.gc = gc;
        this.player = player;
        this.tileMap = tileMap;
    }

    @Override
    public void handle(long now) {
        // Xóa canvas
        gc.clearRect(0, 0, gc.getCanvas().getWidth(), gc.getCanvas().getHeight());

        // Vẽ bản đồ
        tileMap.draw(gc);

        // Vẽ người chơi
        player.draw(gc);
    }
}