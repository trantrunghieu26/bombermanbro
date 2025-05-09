package com.example.bomberman.entities;

import com.example.bomberman.Bomberman;
import com.example.bomberman.Map.Map;
import com.example.bomberman.Map.Tile;
import com.example.bomberman.Map.TileType;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.*; // Import Random và List

public class Ghost extends Enemy {

    private static final double GHOST_SPEED = 65.0; // Tốc độ khá
    private static final int GHOST_SCORE = 400;   // Điểm cao vì khó chịu hơn
    private static final double RANDOM_CHANGE_PROBABILITY = 0.01; // Xác suất đổi hướng ngẫu nhiên khi đang di chuyển

    public Ghost(int startGridX, int startGridY, Map map, Bomberman gameManager) {
        super(startGridX, startGridY, GHOST_SPEED, GHOST_SCORE, map, gameManager);

        double frameDuration = 0.2;
        walkLeftAnimation = new Animation(frameDuration, true, Sprite.ghost_left1, Sprite.ghost_left2, Sprite.ghost_left3);
        walkRightAnimation = new Animation(frameDuration, true, Sprite.ghost_right1, Sprite.ghost_right2, Sprite.ghost_right3);
        deadAnimation = new Animation(TIME_TO_DIE / 3.0, false, Sprite.ghost_dead, Sprite.mob_dead1, Sprite.mob_dead2); // Sử dụng 3 frame chết
        // Update animation list nếu có animation UP/DOWN riêng
    }

    /**
     * Ghost di chuyển ngẫu nhiên, nhưng có khả năng đi xuyên gạch.
     * Nó cũng có logic đuổi theo Player đơn giản.
     */
    @Override
    protected void calculateNextMove() {
        Player player = gameManager.getPlayer();

        // Nếu Player tồn tại và còn sống, cố gắng đuổi theo
        if (player != null && player.isAlive()) {
            Direction dirToPlayer = getDirectionTowards(player.getGridX(), player.getGridY());

            // Nếu có hướng đến Player và có thể bắt đầu di chuyển theo hướng đó (Ghost có thể đi xuyên gạch)
            if (dirToPlayer != Direction.NONE && canMoveTowards(dirToPlayer)) {
                //System.out.println("GHOST DEBUG: Chasing player. Direction: " + dirToPlayer); // Log
                currentDirection = dirToPlayer;
                isMoving = true;
                updateAnimationForDirection(currentDirection);
                return; // Đã quyết định hướng
            }
        }

        // Nếu không đuổi được Player (Player chết, hoặc hướng đến Player bị chặn bởi WALL/BOMB)
        // Hoặc nếu nó đang di chuyển rồi (isMoving=true), thêm xác suất đổi hướng ngẫu nhiên.
        // Nếu !isMoving (vừa bị chặn), nó sẽ luôn gọi setRandomDirection() ở đây.
        if (!isMoving || random.nextDouble() < RANDOM_CHANGE_PROBABILITY) {
            //System.out.println("GHOST DEBUG: Chasing failed or random chance. Calling setRandomDirection."); // Log
            setRandomDirection(); // Chọn hướng ngẫu nhiên (sẽ chọn hướng đi được theo luật của Ghost)
        }
        // Nếu đang di chuyển và không đổi hướng ngẫu nhiên, nó sẽ tiếp tục đi theo currentDirection
        // Việc xử lý khi bị chặn sẽ nằm trong phương thức move() và handleBlockedMovement() (của lớp Enemy, override isObstacle của Ghost)

    }

    // Helper lấy hướng trực tiếp đến mục tiêu (đã có)
    // Có thể di chuyển lên lớp Enemy hoặc GameMath helper nếu cần dùng chung
    protected Direction getDirectionTowards(int targetGridX, int targetGridY) {
        int dx = targetGridX - this.gridX;
        int dy = targetGridY - this.gridY;

        // Ưu tiên di chuyển theo trục có khoảng cách lớn hơn để nhanh chóng đến gần
        if (Math.abs(dx) > Math.abs(dy)) {
            return (dx > 0) ? Direction.RIGHT : Direction.LEFT;
        } else if (Math.abs(dy) > 0) {
            return (dy > 0) ? Direction.DOWN : Direction.UP;
        }
        return Direction.NONE; // Đang ở cùng ô hoặc dx=dy=0
    }


    // Override isObstacle để Ghost có thể đi xuyên gạch.
    // Ghost bị chặn bởi WALL và BOMBS.
    @Override
    protected boolean isObstacle(int gX, int gY) {
        // Kiểm tra biên bản đồ
        if (map == null || gX < 0 || gX >= map.getCols() || gY < 0 || gY >= map.getRows()) {
            return true; // Ngoài map là vật cản
        }

        // Kiểm tra Tile
        Tile tile = map.getTile(gX, gY);
        // Ghost chỉ bị chặn bởi WALL
        if (tile != null && tile.getType() == TileType.WALL) {
            return true;
        }

        // Kiểm tra Bomb (Ghost bị chặn bởi Bomb)
        if (gameManager != null && gameManager.isBombAtGrid(gX, gY)) {
            return true;
        }

        // Ghost có thể đi xuyên qua BRICK, Portal, Item, Grass...
        return false; // Không phải vật cản cho Ghost
    }
    // Ghost sử dụng checkMovementCollision của lớp Enemy (đã được sửa)
    // Ghost sử dụng handleBlockedMovement của lớp Enemy (đã được sửa)
}