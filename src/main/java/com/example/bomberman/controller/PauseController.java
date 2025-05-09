package com.example.bomberman.controller;

import com.example.bomberman.entities.Direction;
import com.example.bomberman.Bomberman;
import com.example.bomberman.GameState;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;


public class PauseController implements SceneController {

    private Bomberman bomberman;

    public PauseController(Bomberman bomberman) {
        this.bomberman = bomberman;
        // Khi vào Pause, yêu cầu InputHandler xóa phím và dừng Player
        if (bomberman.getInputHandler() != null) {
            bomberman.getInputHandler().clearMovingKeys();
        }
        if (bomberman.getPlayer() != null) {
            bomberman.getPlayer().setMovingDirection(Direction.NONE);
        }
        // TODO: Pause music/sound
    }

    @Override
    public void update(double deltaTime) {
        // Trạng thái Pause không cần update logic game
    }

    @Override
    public void render(GraphicsContext gc) {
        // 1. Vẽ trạng thái game đang chơi ở dưới
        bomberman.renderGameComponents(gc);

        // 2. Vẽ lớp phủ và chữ "PAUSED"
        if (gc == null) return;
        Font uiFont = bomberman.getUiFont();
        if (uiFont == null) return;

        // Lớp phủ mờ
        gc.setFill(Color.rgb(0, 0, 0, 0.6));
        gc.fillRect(0, 0, bomberman.getCanvasWidth(), bomberman.getCanvasHeight());

        // Chữ "PAUSED"
        String pauseText = "PAUSED";
        gc.setFill(Color.YELLOW);
        Font pauseFont = Font.font(uiFont.getFamily(), 48);
        gc.setFont(pauseFont);
        Text textNode = new Text(pauseText); textNode.setFont(pauseFont);
        double textWidth = textNode.getLayoutBounds().getWidth();
        double x = (bomberman.getCanvasWidth() - textWidth) / 2.0;
        double y = bomberman.getCanvasHeight() / 2.0;
        gc.fillText(pauseText, x, y);

        // Hướng dẫn Resume
        gc.setFont(uiFont);
        gc.setFill(Color.WHITE);
        String resumeText = "Press ESC to Resume";
        Text resumeTextNode = new Text(resumeText); resumeTextNode.setFont(uiFont);
        double resumeTextWidth = resumeTextNode.getLayoutBounds().getWidth();
        gc.fillText(resumeText, (bomberman.getCanvasWidth() - resumeTextWidth) / 2.0, y + 40);
    }

    @Override
    public void handleInput(KeyCode code, boolean isPressed) {
        if (isPressed && code == KeyCode.ESCAPE) {
            // Yêu cầu Bomberman chuyển về trạng thái Playing
            bomberman.switchController(GameState.PLAYING);
            // TODO: Resume music/sound
        }
    }
}