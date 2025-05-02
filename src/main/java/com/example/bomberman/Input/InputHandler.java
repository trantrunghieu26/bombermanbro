package com.example.bomberman.Input; // Đặt trong package Input
import com.example.bomberman.Bomberman;
import com.example.bomberman.entities.Player; // Cần import lớp Player
import com.example.bomberman.entities.Direction; // Cần import enum Direction
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import com.example.bomberman.GameState;
import java.util.HashSet;
import java.util.Set;

public class InputHandler {

    private Bomberman gameManager;
    private Scene gameScene;
    private Player player;
    // Set để theo dõi các phím di chuyển đang được giữ
    private Set<KeyCode> movingKeysPressed = new HashSet<>();
    // Có thể thêm các cờ trạng thái khác nếu cần (ví dụ: isBombKeyPressed)

    // Constructor nhận Scene và Player
    public InputHandler(Scene gameScene, Player player,Bomberman gameManager) {
        this.gameScene = gameScene;
        this.player = player;
        this.gameManager=gameManager;
        attachInputListeners(); // Gắn các trình nghe sự kiện khi tạo đối tượng
    }

    // Phương thức để gắn các trình nghe sự kiện vào Scene
    private void attachInputListeners() {
        // Bắt sự kiện nhấn phím
        gameScene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            GameState currentGameState = gameManager.getCurrentState();
            if (gameManager == null || player == null) return;
            switch (currentGameState) {
                case PLAYING:
                    if (isMovementKey(code)) {
                        if (!movingKeysPressed.contains(code)) { // Chỉ xử lý lần nhấn đầu tiên
                            movingKeysPressed.add(code);
                            updatePlayerMovement(); // Cập nhật hướng di chuyển của Player
                        }
                    }
                    // Xử lý phím đặt bom (chỉ cần xử lý khi nhấn xuống)
                    if (code == KeyCode.SPACE) {
                        if (player != null) player.placeBomb();
                    }
                    // Xử lý phím tạm dừng (chỉ cần xử lý khi nhấn xuống)
                    if (code == KeyCode.ESCAPE) {
                        if (gameManager != null) { // Kiểm tra null
                            gameManager.togglePause(); // Gọi phương thức mới trong Bomberman
                        }
                    }
                    break;
                case PAUSED:
                    if (code == KeyCode.ESCAPE) {
                        gameManager.togglePause();
                    }
                    break;
                case MENU:
                    if (code == KeyCode.UP || code == KeyCode.W) {
                        gameManager.navigateMenuUp(); // Gọi hàm của Bomberman
                    } else if (code == KeyCode.DOWN || code == KeyCode.S) {
                        gameManager.navigateMenuDown(); // Gọi hàm của Bomberman
                    } else if (code == KeyCode.ENTER) {
                        gameManager.selectMenuOption(); // Gọi hàm của Bomberman
                    } else if (code == KeyCode.ESCAPE) {
                        if (gameManager.getPrimaryStage() != null) gameManager.getPrimaryStage().close();
                    }
                    break;
                case GAME_OVER:
                    if (code == KeyCode.ENTER) {
                        // *** THÊM DÒNG NÀY ĐỂ DEBUG ***
                        System.out.println("GAME_OVER: ENTER detected, attempting to call restartGame...");
                        gameManager.restartGame();
                    }
                    if (code == KeyCode.ESCAPE) {
                        // *** THÊM DÒNG NÀY ĐỂ DEBUG ***
                        System.out.println("GAME_OVER: ESCAPE detected, attempting to close stage...");
                        if (gameManager.getPrimaryStage() != null) {
                            gameManager.getPrimaryStage().close();
                        } else {
                            System.out.println("GAME_OVER: primaryStage is null!"); // Kiểm tra thêm
                        }
                    }
                    break;
            }

        });

        // Bắt sự kiện nhả phím
        gameScene.setOnKeyReleased(event -> {
            KeyCode code = event.getCode();
            if (gameManager == null || player == null) return;
            GameState currentGameState = gameManager.getCurrentState();
            if (currentGameState == GameState.PLAYING) { // Chỉ xử lý nhả phím khi đang chơi
                if (isMovementKey(code)) {
                    movingKeysPressed.remove(code);
                    updatePlayerMovement();
                }
            }

        });
    }

    // Phương thức helper kiểm tra có phải phím di chuyển không
    private boolean isMovementKey(KeyCode code) {
        return code == KeyCode.W || code == KeyCode.S || code == KeyCode.A || code == KeyCode.D
        ||code == KeyCode.UP || code == KeyCode.DOWN || code == KeyCode.LEFT || code == KeyCode.RIGHT;
    }

    // Cập nhật hướng di chuyển của Player dựa trên các phím đang được giữ
    private void updatePlayerMovement() {
        if (player == null) return;
        Direction currentDirection = Direction.NONE;

        if (movingKeysPressed.contains(KeyCode.W) || movingKeysPressed.contains(KeyCode.UP)) {
            currentDirection = Direction.UP;
        } else if (movingKeysPressed.contains(KeyCode.S) || movingKeysPressed.contains(KeyCode.DOWN)) {
            currentDirection = Direction.DOWN;
        } else if (movingKeysPressed.contains(KeyCode.A) || movingKeysPressed.contains(KeyCode.LEFT)) {
            currentDirection = Direction.LEFT;
        } else if (movingKeysPressed.contains(KeyCode.D) || movingKeysPressed.contains(KeyCode.RIGHT)) {
            currentDirection = Direction.RIGHT;
        }

        // Thông báo cho Player biết hướng di chuyển mới
        // Lớp Player cần có phương thức setMovingDirection(Direction direction)
        player.setMovingDirection(currentDirection);
    }
    public void clearMovingKeys() {
        movingKeysPressed.clear(); // Xóa sạch Set
        // Không cần gọi updatePlayerMovement ở đây vì Player đã được bảo dừng bởi togglePause
        System.out.println("InputHandler: Moving keys cleared."); // Log (tùy chọn)
    }

    // Bạn có thể thêm các phương thức public nếu cần bật/tắt InputHandler tạm thời
    // public void enable() { gameScene.addEventHandler(...); }
    // public void disable() { gameScene.removeEventHandler(...); }
}