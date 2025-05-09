package com.example.bomberman.controller;

import com.example.bomberman.Bomberman;
import com.example.bomberman.GameState;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Particle;
import com.example.bomberman.graphics.Sprite;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.List;


public class GameOverController implements SceneController {

    private Bomberman bomberman;
    private double gameOverTimer = 0;
    private double displayScore = 0;

    private final double SCORE_ANIMATION_DURATION = 1.5; // Lấy từ Bomberman hoặc định nghĩa
    private final double GAMEOVER_TEXT_FADE_IN_DURATION = 0.8; // Lấy từ Bomberman hoặc định nghĩa


    public GameOverController(Bomberman bomberman) {
        this.bomberman = bomberman;
        this.gameOverTimer = 0; // Reset timer
        this.displayScore = 0;  // Reset điểm hiển thị
        // Khởi tạo particle khi vào state
        bomberman.initializeGameOverParticles(); // Gọi hàm của Bomberman
    }

    @Override
    public void update(double deltaTime) {
        gameOverTimer += deltaTime;

        // Animate score
        int finalScore = bomberman.getScore(); // Lấy điểm cuối cùng
        if (gameOverTimer < SCORE_ANIMATION_DURATION) {
            displayScore = finalScore * (gameOverTimer / SCORE_ANIMATION_DURATION);
        } else {
            displayScore = finalScore;
        }

        // Update particles (gọi hàm của Bomberman)
        bomberman.updateGameOverParticles(deltaTime);
    }

    @Override
    public void render(GraphicsContext gc) {
        // --- Lấy tài nguyên từ Bomberman ---
        Image lastScreenSnapshot = bomberman.getLastScreenSnapshot();
        List<Particle> particles = bomberman.getParticles();
        Animation playerDeadAnimation = bomberman.getPlayerDeadAnimation();
        Font gameOverFont = bomberman.getGameOverFont();
        Font uiFont = bomberman.getUiFont();
        double canvasWidth = bomberman.getCanvasWidth();
        double canvasHeight = bomberman.getCanvasHeight();

        if (gc == null || gameOverFont == null || uiFont == null) return;

        // --- Vẽ nền ---
        if (lastScreenSnapshot != null) {
            gc.setGlobalAlpha(0.4); // Làm mờ snapshot
            gc.drawImage(lastScreenSnapshot, 0, 0, canvasWidth, canvasHeight);
            gc.setGlobalAlpha(1.0);
            gc.setFill(Color.rgb(0, 0, 0, 0.6)); // Lớp phủ đen
            gc.fillRect(0, 0, canvasWidth, canvasHeight);
        } else {
            gc.setFill(Color.BLACK); // Fallback
            gc.fillRect(0, 0, canvasWidth, canvasHeight);
        }

        // --- Vẽ Particles ---
        if (particles != null) {
            for (Particle p : particles) {
                p.render(gc);
            }
        }

        // --- Vẽ chữ "GAME OVER" ---
        double textAlpha = Math.min(1.0, gameOverTimer / GAMEOVER_TEXT_FADE_IN_DURATION);
        gc.setGlobalAlpha(textAlpha);
        String goText = "GAME OVER";
        gc.setFont(gameOverFont);
        Text goNode = new Text(goText); goNode.setFont(gameOverFont);
        double goWidth = goNode.getLayoutBounds().getWidth();
        double xGO = (canvasWidth - goWidth) / 2.0;
        double yGO = 200; // Vị trí gốc
        gc.setFill(Color.BLACK);
        gc.fillText(goText, xGO + 3, yGO + 3); // Shadow
        gc.setFill(Color.RED);
        gc.fillText(goText, xGO, yGO);
        gc.setGlobalAlpha(1.0);

        // --- Vẽ Điểm số ---
        String scoreText = "Final Score: " + (int) displayScore;
        Font scoreFont = Font.font(uiFont.getFamily(), 32); // Font gốc
        gc.setFont(scoreFont);
        gc.setFill(Color.YELLOW);
        Text scoreNode = new Text(scoreText); scoreNode.setFont(scoreFont);
        double scoreWidth = scoreNode.getLayoutBounds().getWidth();
        gc.fillText(scoreText, (canvasWidth - scoreWidth) / 2.0, yGO + 100);

        // --- Vẽ Player chết ---
        Image deadPlayerImage = null;
        if (playerDeadAnimation != null) {
            Sprite deadSprite = playerDeadAnimation.getFrame(gameOverTimer); // Dùng gameOverTimer cho animation chết gốc
            if(deadSprite != null) deadPlayerImage = deadSprite.getFxImage();
        }
        if (deadPlayerImage == null) { // Fallback nếu animation null hoặc lỗi gốc
            deadPlayerImage = Sprite.player_dead3.getFxImage();
        }

        if (deadPlayerImage != null) {
            // Lấy kích thước thật từ ảnh, không scale trong code gốc
            double playerW = deadPlayerImage.getWidth();
            double playerH = deadPlayerImage.getHeight();
            double playerX = canvasWidth / 2.0 - playerW / 2.0; // Giữa màn hình gốc
            double playerY = yGO + 120; // Dưới điểm số gốc
            gc.drawImage(deadPlayerImage, playerX, playerY);

        }

        // --- Vẽ hướng dẫn Restart/Exit ---
        gc.setFont(uiFont); // Font gốc
        gc.setFill(Color.WHITE);
        String restartText = "Press ENTER to Restart";
        String exitText = "Press ESC to Exit";
        Text restartNode = new Text(restartText); restartNode.setFont(uiFont);
        double restartWidth = restartNode.getLayoutBounds().getWidth();
        gc.fillText(restartText, (canvasWidth - restartWidth) / 2.0, canvasHeight - 100);
        Text exitNode = new Text(exitText); exitNode.setFont(uiFont);
        double exitWidth = exitNode.getLayoutBounds().getWidth();
        gc.fillText(exitText, (canvasWidth - exitWidth) / 2.0, canvasHeight - 70);
    }

    @Override
    public void handleInput(KeyCode code, boolean isPressed) {
        if (!isPressed) return; // Chỉ xử lý nhấn xuống

        if (code == KeyCode.ENTER) {
            // Yêu cầu Bomberman load lại level 1 và chuyển sang Playing
            bomberman.requestLoadLevelAndSwitchState(1, GameState.PLAYING);
        } else if (code == KeyCode.ESCAPE) {
            // Thoát game
            if (bomberman.getPrimaryStage() != null) {
                bomberman.getPrimaryStage().close();
            }
        }
    }
}