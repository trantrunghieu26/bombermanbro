package com.example.bomberman.controller;

import com.example.bomberman.Bomberman;
import com.example.bomberman.GameState;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.image.WritableImage; // Import WritableImage

import java.util.Random; // Import Random

public class GameOverTransitionController implements SceneController {

    private Bomberman bomberman;
    private double transitionTimer = 0;
    private final double TRANSITION_DURATION = 1.0; // Lấy từ Bomberman hoặc định nghĩa ở đây
    private Random random = new Random(); // Tạo Random riêng cho hiệu ứng rung


    public GameOverTransitionController(Bomberman bomberman) {
        this.bomberman = bomberman;
        this.transitionTimer = 0; // Reset timer khi vào state
    }

    @Override
    public void update(double deltaTime) {
        transitionTimer += deltaTime;
        if (transitionTimer >= TRANSITION_DURATION) {
            // Chụp snapshot TRƯỚC khi chuyển state
            WritableImage snapshot = bomberman.getCanvas().snapshot(null, null); // Cần getCanvas()
            bomberman.setLastScreenSnapshot(snapshot); // Cần setter

            // Yêu cầu Bomberman chuyển sang trạng thái GameOver
            bomberman.switchController(GameState.GAME_OVER);
        }
    }

    @Override
    public void render(GraphicsContext gc) {
        if (gc == null) return;

        // 1. Vẽ trạng thái game bình thường ở dưới
        bomberman.renderGameComponents(gc);

        // 2. Áp dụng hiệu ứng rung và mờ dần
        double effectProgress = Math.min(1.0, transitionTimer / TRANSITION_DURATION);
        double shakeIntensity = 5.0 * (1.0 - effectProgress); // Giá trị gốc
        double offsetX = (random.nextDouble() - 0.5) * shakeIntensity;
        double offsetY = (random.nextDouble() - 0.5) * shakeIntensity;

        gc.save();
        gc.translate(offsetX, offsetY);

        double fadeAlpha = effectProgress * 0.7; // Giá trị gốc
        gc.setFill(Color.rgb(0, 0, 0, fadeAlpha));
        // Vẽ hình chữ nhật phủ lên toàn bộ màn hình (điều chỉnh tọa độ do translate)
        gc.fillRect(-offsetX, -offsetY, bomberman.getCanvasWidth(), bomberman.getCanvasHeight());

        gc.restore();
    }

    @Override
    public void handleInput(KeyCode code, boolean isPressed) {
        // Không xử lý input trong quá trình transition
    }
}