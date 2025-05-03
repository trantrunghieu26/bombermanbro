package com.example.bomberman.controller;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;

/**
 * Interface chung cho tất cả các bộ điều khiển cảnh (trạng thái game).
 * Định nghĩa các phương thức cơ bản mà mỗi trạng thái cần xử lý.
 */
public interface SceneController {
    void update(double deltaTime);
    void render(GraphicsContext gc);
    void handleInput(KeyCode code, boolean isPressed);

}