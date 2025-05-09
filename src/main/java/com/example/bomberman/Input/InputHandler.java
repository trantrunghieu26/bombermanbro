package com.example.bomberman.Input; // Đặt trong package Input

import com.example.bomberman.Bomberman;
import com.example.bomberman.GameState; // Cần thiết nếu muốn kiểm tra state phụ
import com.example.bomberman.controller.SceneController; // Import Interface
import com.example.bomberman.entities.Player; // Cần import lớp Player
// import com.example.bomberman.entities.Direction; // Không cần trực tiếp ở đây nữa
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;

import java.util.HashSet;
import java.util.Set;

public class InputHandler {

    private Bomberman gameManager;
    private Scene gameScene;
    private Player player; // Vẫn giữ tham chiếu để xử lý trực tiếp nếu cần
    // Set để theo dõi các phím di chuyển đang được giữ (VẪN CẦN nếu xử lý di chuyển ở đây)
    // private Set<KeyCode> movingKeysPressed = new HashSet<>();

    public InputHandler(Scene gameScene, Player player, Bomberman gameManager) {
        this.gameScene = gameScene;
        this.player = player; // Lưu tham chiếu player
        this.gameManager = gameManager;
        attachInputListeners();
    }

    private void attachInputListeners() {
        gameScene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (gameManager != null) {
                SceneController controller = gameManager.getCurrentController(); // Lấy controller hiện tại
                if (controller != null) {
                    // Ủy quyền xử lý input cho controller hiện tại
                    controller.handleInput(code, true); // true = pressed
                }
            }
            // --- XỬ LÝ TRỰC TIẾP (nếu không muốn đưa vào controller.handleInput) ---
            // Ví dụ: Di chuyển vẫn xử lý ở đây cho đơn giản ban đầu
            // if (gameManager.getCurrentState() == GameState.PLAYING && isMovementKey(code)) {
            //     if (!movingKeysPressed.contains(code)) {
            //        movingKeysPressed.add(code);
            //        updatePlayerMovement(); // Vẫn gọi hàm cập nhật trực tiếp Player
            //     }
            // }
        });

        gameScene.setOnKeyReleased(event -> {
            KeyCode code = event.getCode();
            if (gameManager != null) {
                SceneController controller = gameManager.getCurrentController();
                if (controller != null) {
                    // Ủy quyền xử lý nhả phím nếu cần
                    controller.handleInput(code, false); // false = released
                }
            }
            // --- XỬ LÝ TRỰC TIẾP (nếu không muốn đưa vào controller.handleInput) ---
            // Ví dụ: Di chuyển vẫn xử lý ở đây
            // if (gameManager.getCurrentState() == GameState.PLAYING && isMovementKey(code)) {
            //     movingKeysPressed.remove(code);
            //     updatePlayerMovement();
            // }
        });
    }

    // --- CÁC HÀM HELPER DI CHUYỂN (Có thể giữ lại nếu xử lý di chuyển ở đây) ---
    /*
    private boolean isMovementKey(KeyCode code) {
        return code == KeyCode.W || code == KeyCode.S || code == KeyCode.A || code == KeyCode.D
                || code == KeyCode.UP || code == KeyCode.DOWN || code == KeyCode.LEFT || code == KeyCode.RIGHT;
    }

    private void updatePlayerMovement() {
        if (player == null) return;
        Direction currentDirection = Direction.NONE;

        // Xác định hướng dựa trên các phím đang nhấn trong Set movingKeysPressed
        if (movingKeysPressed.contains(KeyCode.W) || movingKeysPressed.contains(KeyCode.UP)) {
            currentDirection = Direction.UP;
        } else if (movingKeysPressed.contains(KeyCode.S) || movingKeysPressed.contains(KeyCode.DOWN)) {
            currentDirection = Direction.DOWN;
        } else if (movingKeysPressed.contains(KeyCode.A) || movingKeysPressed.contains(KeyCode.LEFT)) {
            currentDirection = Direction.LEFT;
        } else if (movingKeysPressed.contains(KeyCode.D) || movingKeysPressed.contains(KeyCode.RIGHT)) {
            currentDirection = Direction.RIGHT;
        }
        player.setMovingDirection(currentDirection);
    }

    public void clearMovingKeys() {
        movingKeysPressed.clear();
        updatePlayerMovement(); // Gọi để đảm bảo Player dừng lại
        System.out.println("InputHandler: Moving keys cleared.");
    }
    */

    // --- HÀM CLEAR KEYS ĐƠN GIẢN HƠN NẾU KHÔNG QUẢN LÝ STATE Ở ĐÂY ---
    public void clearMovingKeys() {
        // Hàm này có thể không cần thiết nữa nếu logic dừng Player được xử lý khi vào Pause
        System.out.println("InputHandler: clearMovingKeys() called (potentially no effect).");
    }

    // Setter để cập nhật Player nếu cần (ví dụ sau khi load level)
    public void setPlayer(Player player) {
        this.player = player;
        // movingKeysPressed.clear(); // Reset trạng thái phím khi đổi player
    }
}