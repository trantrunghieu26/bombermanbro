package com.example.bomberman.Input; // Đặt trong package Input

import com.example.bomberman.entities.Player; // Cần import lớp Player
import com.example.bomberman.entities.Direction; // Cần import enum Direction
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.HashSet;
import java.util.Set;

public class InputHandler {

    private Scene gameScene;
    private Player player;
    // Set để theo dõi các phím di chuyển đang được giữ
    private Set<KeyCode> movingKeysPressed = new HashSet<>();
    // Có thể thêm các cờ trạng thái khác nếu cần (ví dụ: isBombKeyPressed)

    // Constructor nhận Scene và Player
    public InputHandler(Scene gameScene, Player player) {
        this.gameScene = gameScene;
        this.player = player;
        this.attachInputListeners(); // Gắn các trình nghe sự kiện khi tạo đối tượng
    }

    // Phương thức để gắn các trình nghe sự kiện vào Scene
    private void attachInputListeners() {
        // Bắt sự kiện nhấn phím
        gameScene.setOnKeyPressed(event -> {

            if (this.player.isAlive()) {
                KeyCode code = event.getCode();

                // Xử lý các phím di chuyển
                if (isMovementKey(code)) {
                    if (!movingKeysPressed.contains(code)) { // Chỉ xử lý lần nhấn đầu tiên
                        movingKeysPressed.add(code);
                        updatePlayerMovement(); // Cập nhật hướng di chuyển của Player
                    }
                }

                // Xử lý phím đặt bom (chỉ cần xử lý khi nhấn xuống)
                if (code == KeyCode.SPACE) {
                    // Gọi phương thức requestPlayerPlaceBomb() của player
                    if (player != null) { // Kiểm tra game khác null trước khi gọi
                        this.player.requestPlayerPlaceBomb(); // *** DÒNG GÂY LỖI TRONG ẢNH NẾU 'game' null/không tồn tại ***
                    }
                }

                // Xử lý phím tạm dừng (chỉ cần xử lý khi nhấn xuống)
                if (code == KeyCode.ESCAPE) {
                    // Logic tạm dừng game. Có thể gọi một phương thức trong lớp GameState
                    // gameManager.togglePause(); // Cần tham chiếu đến GameStateManager
                }

                // Ngăn chặn sự kiện được xử lý tiếp bởi các Node khác nếu cần
                // event.consume();
            }
        });

        // Bắt sự kiện nhả phím
        gameScene.setOnKeyReleased(event -> {

            if (this.player.isAlive()) {
                KeyCode code = event.getCode();

                // Xử lý khi nhả các phím di chuyển
                if (isMovementKey(code)) {
                    movingKeysPressed.remove(code);
                    updatePlayerMovement(); // Cập nhật hướng di chuyển của Player khi nhả phím
                }

                // Có thể thêm xử lý khi nhả phím đặt bom nếu cần
                // if (code == KeyCode.SPACE) { ... }

                // Ngăn chặn sự kiện được xử lý tiếp bởi các Node khác nếu cần
                // event.consume();
            }
        });
    }

    // Phương thức helper kiểm tra có phải phím di chuyển không
    private boolean isMovementKey(KeyCode code) {
        return code == KeyCode.W || code == KeyCode.S || code == KeyCode.A || code == KeyCode.D;
    }

    // Cập nhật hướng di chuyển của Player dựa trên các phím đang được giữ
    private void updatePlayerMovement() {
        // Xác định hướng ưu tiên nếu nhiều phím được giữ (tùy logic game)
        // Ví dụ: ưu tiên phím được nhấn gần nhất, hoặc phím theo hướng nào đó.
        // Cách đơn giản: ưu tiên thứ tự Right, Left, Down, Up
        Direction currentDirection = Direction.NONE;

        if (movingKeysPressed.contains(KeyCode.D)) {
            currentDirection = Direction.RIGHT;
        } else if (movingKeysPressed.contains(KeyCode.A)) {
            currentDirection = Direction.LEFT;
        } else if (movingKeysPressed.contains(KeyCode.S)) {
            currentDirection = Direction.DOWN;
        } else if (movingKeysPressed.contains(KeyCode.W)) {
            currentDirection = Direction.UP;
        }

        // Thông báo cho Player biết hướng di chuyển mới
        // Lớp Player cần có phương thức setMovingDirection(Direction direction)
        if (player != null) {
            player.setMovingDirection(currentDirection);
        }
    }

    // Bạn có thể thêm các phương thức public nếu cần bật/tắt InputHandler tạm thời
    // public void enable() { gameScene.addEventHandler(...); }
    // public void disable() { gameScene.removeEventHandler(...); }
}