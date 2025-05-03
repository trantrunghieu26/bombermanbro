package com.example.bomberman.controller;

import com.example.bomberman.Bomberman;
import com.example.bomberman.GameState;
import com.example.bomberman.entities.Direction;
import com.example.bomberman.entities.Enemy;
import com.example.bomberman.entities.Player;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;

public class PlayingController implements SceneController {

    private Bomberman bomberman;
    private Player player; // Giữ tham chiếu player để dễ truy cập

    public PlayingController(Bomberman bomberman) {
        this.bomberman = bomberman;
        this.player = bomberman.getPlayer(); // Lấy player từ Bomberman
    }

    @Override
    public void update(double deltaTime) {
        // --- Lấy các tham chiếu cần thiết từ Bomberman ---
        double levelTimeRemaining = bomberman.getLevelTimeRemaining();
        boolean portalActivated = bomberman.isPortalActivated();

        // --- Cập nhật thời gian và kiểm tra hết giờ ---
        if (levelTimeRemaining > 0) {
            levelTimeRemaining -= deltaTime;
            bomberman.setLevelTimeRemaining(levelTimeRemaining); // Cập nhật lại giá trị trong Bomberman
            if (levelTimeRemaining <= 0) {
                bomberman.setLevelTimeRemaining(0); // Đảm bảo không âm
                System.out.println("Time's up! (Detected in PlayingController)");
                bomberman.switchController(GameState.GAME_OVER_TRANSITION);
                return; // Dừng update frame này khi chuyển state
            }
        }

        // --- Cập nhật Player ---
        if (player != null) {
            player.update(deltaTime);
            // Kiểm tra player chết sau khi update
            if (!player.isAlive()) {
                System.out.println("Player died! (Detected in PlayingController)");
                bomberman.switchController(GameState.GAME_OVER_TRANSITION);
                return; // Dừng update frame này khi chuyển state
            }
        }

        // --- Cập nhật các entities khác (vẫn gọi hàm của Bomberman) ---
        bomberman.updateBombs(deltaTime);
        bomberman.updateFlames(deltaTime);
        bomberman.updateItems(deltaTime);
        bomberman.updateTemporaryAnimations(deltaTime);
        bomberman.updateEnemies(deltaTime);
        bomberman.handlePlayerEnemyCollisions(); // Gọi hàm này
        bomberman.handleFlameEnemyCollisions();
        // bomberman.updateEnemies(deltaTime); // TODO

        // --- Cập nhật trạng thái Portal (logic gốc là luôn bật) ---
        if (!portalActivated) {
            boolean allEnemiesDead = true;
            if(bomberman.getEnemies() != null) { // Kiểm tra null
                for (Enemy e : bomberman.getEnemies()) {
                    if (e.isAlive()) {
                        allEnemiesDead = false;
                        break;
                    }
                }
            } else {
                allEnemiesDead = false; // Nếu list null thì coi như chưa chết hết
            }


            if (allEnemiesDead) { // Chỉ kích hoạt khi không còn Enemy sống
                bomberman.setPortalActivated(true);
                System.out.println("Portal Activated! All enemies defeated. (Handled by PlayingController)");
            }


        }

        // --- Xử lý va chạm và tương tác (vẫn gọi hàm của Bomberman) ---
        bomberman.handleKickBombTrigger();
        bomberman.handleFlameBombCollisions();
        bomberman.handlePlayerItemCollisions();
        // bomberman.handlePlayerEnemyCollisions(); // TODO
        // bomberman.handleFlameEnemyCollisions(); // TODO
        bomberman.handlePortalTransition(); // Logic chuyển level nằm trong hàm này của Bomberman
    }

    @Override
    public void render(GraphicsContext gc) {
        // Ủy quyền hoàn toàn việc vẽ cho Bomberman (vì nó đã có logic render các lớp)
        // Hoặc có thể tách riêng từng phần vẽ vào đây nếu muốn kiểm soát chi tiết hơn
        bomberman.renderGameComponents(gc); // Cần tạo phương thức này trong Bomberman
        // để vẽ map, entities, UI mà không xóa màn hình
    }

    @Override
    public void handleInput(KeyCode code, boolean isPressed) {
        if (player == null) return;

        // Xử lý nhấn phím
        if (isPressed) {
            switch (code) {
                case W:
                case UP:
                    player.setMovingDirection(Direction.UP);
                    break;
                case S:
                case DOWN:
                    player.setMovingDirection(Direction.DOWN);
                    break;
                case A:
                case LEFT:
                    player.setMovingDirection(Direction.LEFT);
                    break;
                case D:
                case RIGHT:
                    player.setMovingDirection(Direction.RIGHT);
                    break;
                case SPACE:
                    player.placeBomb();
                    break;
                case ESCAPE:
                    // Yêu cầu Bomberman chuyển sang trạng thái Pause
                    bomberman.switchController(GameState.PAUSED);
                    // Logic clear input/stop player nên đặt trong onEnterState của PauseController
                    // hoặc trong hàm switchController của Bomberman
                    break;
                default:
                    // Các phím khác không xử lý khi đang chơi
                    break;
            }
        }
        // Xử lý nhả phím (chỉ cho di chuyển)
        else {
            if (bomberman.isMovementKey(code)) { // Cần isMovementKey trong Bomberman hoặc chuyển vào đây
                // Nếu phím được nhả trùng với hướng đang di chuyển hiện tại của player
                // thì mới dừng lại. Tránh trường hợp nhả phím khác hướng đang giữ.
                Direction releasedDirection = bomberman.getDirectionFromKey(code); // Cần hàm này
                if(player.getCurrentDirection() == releasedDirection){
                    player.setMovingDirection(Direction.NONE);
                }
                // Cập nhật lại hướng nếu còn phím khác đang giữ (logic này phức tạp hơn,
                // có thể cần giữ lại Set<KeyCode> trong InputHandler và gọi updateMovement từ PlayingController)
                // Cách đơn giản: chỉ dừng khi nhả đúng phím hướng đang đi.
            }
        }
        // Cập nhật lại hướng di chuyển cuối cùng dựa trên phím còn nhấn (nếu cần xử lý phức tạp hơn)
        // Player cần phương thức để lấy Set các phím đang nhấn hoặc InputHandler cần cung cấp
        // bomberman.updatePlayerMovementBasedOnPressedKeys(); // Ví dụ
    }
}