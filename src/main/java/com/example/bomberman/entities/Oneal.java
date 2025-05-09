package com.example.bomberman.entities;
import com.example.bomberman.Bomberman;
import com.example.bomberman.Map.Map;
// Không cần import Tile, TileType ở đây nữa vì không override isObstacle
// import com.example.bomberman.Map.Tile;
// import com.example.bomberman.Map.TileType;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite;

import java.util.*; // Import Random và List
import java.lang.Math; // Import Math cho abs (được dùng trong getDirectionTowards)


public class Oneal extends Enemy {

    private static final double ONEAL_SPEED = 60.0; // Tốc độ của Oneal
    private static final int ONEAL_SCORE = 200;    // Điểm của Oneal

    // Xác suất đổi hướng ngẫu nhiên khi đang di chuyển (để có hành vi "lúc lắc" như Ghost)
    private static final double RANDOM_CHANGE_PROBABILITY = 0.01; // Ví dụ: 1% cơ hội mỗi frame để đổi hướng


    // Không còn các thuộc tính liên quan đến BFS/pathfinding phức tạp


    public Oneal(int startGridX, int startGridY, Map map, Bomberman gameManager) {
        super(startGridX, startGridY, ONEAL_SPEED, ONEAL_SCORE, map, gameManager);

        // --- Khởi tạo Animations cho Oneal ---
        // frameDuration này nên được điều chỉnh để phù hợp với ONEAL_SPEED = 75.0.
        double frameDuration = 0.2; // Có thể thử 0.18, 0.15... tùy cảm giác
        walkLeftAnimation = new Animation(frameDuration, true, Sprite.oneal_left1, Sprite.oneal_left2, Sprite.oneal_left3);
        walkRightAnimation = new Animation(frameDuration, true, Sprite.oneal_right1, Sprite.oneal_right2, Sprite.oneal_right3);
        // Animation chết dùng chung mob_dead hoặc oneal_dead
        deadAnimation = new Animation(TIME_TO_DIE / 4.0, false, // Sử dụng 4 frame cho đồng nhất với Doll
                Sprite.oneal_dead,
                Sprite.mob_dead1,
                Sprite.mob_dead2,
                Sprite.mob_dead3);

        // Animation ban đầu được đặt bởi setRandomDirection() trong Enemy constructor
        // sau khi super() được gọi. setRandomDirection() sẽ sử dụng isObstacle() mặc định của Enemy.
    }

    /**
     * Override phương thức tính toán nước đi cho Oneal.
     * Logic tương tự Ghost:
     * - Nếu Player còn sống, cố gắng đi thẳng đến Player.
     * - Nếu Player chết hoặc hướng thẳng bị chặn bởi vật cản (WALL/BRICK/BOMB), chuyển sang di chuyển ngẫu nhiên.
     * - Vẫn sử dụng isObstacle() mặc định của Enemy (chặn WALL, BRICK, BOMB).
     */
    @Override
    protected void calculateNextMove() {
        Player player = gameManager.getPlayer();

        // 1. Nếu Player tồn tại và còn sống
        if (player != null && player.isAlive()) {
            Direction dirToPlayer = getDirectionTowards(player.getGridX(), player.getGridY());

            // Nếu có hướng đến Player VÀ có thể đi bước đầu tiên theo hướng đó (không bị chặn bởi WALL/BRICK/BOMB)
            // canMoveTowards() sử dụng checkMovementCollision(), checkMovementCollision() gọi isObstacle().
            // Oneal giờ sử dụng isObstacle() mặc định của Enemy, chặn WALL, BRICK, BOMB.
            if (dirToPlayer != Direction.NONE && canMoveTowards(dirToPlayer)) {
                //System.out.println("ONEAL DEBUG: Chasing player. Direction: " + dirToPlayer); // Log
                currentDirection = dirToPlayer; // Đặt hướng di chuyển là hướng thẳng đến Player
                isMoving = true;
                // updateAnimationForDirection(currentDirection); // Animation được cập nhật ở move()
                return; // Đã quyết định hành động (đuổi theo)
            }
        }

        // 2. Nếu không đuổi được Player (Player chết, hoặc hướng đến Player bị chặn bởi vật cản)
        // HOẶC nếu nó đang di chuyển rồi (isMoving=true), thêm xác suất đổi hướng ngẫu nhiên giống Ghost.
        // Nếu !isMoving (vừa bị chặn), nó sẽ LUÔN gọi setRandomDirection() ở đây.
        // setRandomDirection() sẽ chọn hướng ngẫu nhiên khả thi (sẽ sử dụng isObstacle() mặc định của Enemy).
        if (!isMoving || random.nextDouble() < RANDOM_CHANGE_PROBABILITY) {
            //System.out.println("ONEAL DEBUG: Chasing failed or random chance. Calling setRandomDirection."); // Log
            setRandomDirection();
        }
        // Nếu đang di chuyển và không đổi hướng ngẫu nhiên, nó sẽ tiếp tục đi theo currentDirection
        // Việc xử lý khi bị chặn sẽ nằm trong phương thức move() và handleBlockedMovement() của lớp Enemy.

        // Cập nhật animation dựa trên currentDirection cuối cùng được quyết định trong calculateNextMove
        // Đặt ở đây để đảm bảo animation luôn khớp với hướng được chọn.
        if (currentDirection != Direction.NONE) {
            updateAnimationForDirection(currentDirection);
        } else {
            updateAnimationForDirection(Direction.NONE); // Chuyển sang animation đứng yên nếu currentDirection là NONE
        }
    }

    // Helper lấy hướng trực tiếp đến mục tiêu (copy từ Ghost/phiên bản Oneal trước)
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


    // Phương thức update vẫn giữ nguyên, chỉ gọi update của lớp cha (Enemy).
    @Override
    public void update(double deltaTime) {
        if (!isAlive) {
            super.update(deltaTime); // Xử lý animation chết
            return;
        }
        // Gọi update của lớp cha (bao gồm calculateNextMove và move với substepping)
        super.update(deltaTime);
    }


    /**
     * isObstacle() KHÔNG được override ở đây.
     * Oneal sẽ sử dụng phương thức isObstacle() mặc định từ lớp Enemy.
     * isObstacle() mặc định coi WALL và BRICK là vật cản.
     * Nó cũng coi BOMB là vật cản (nếu gameManager.isBombAtGrid() đúng).
     */
    // @Override // XÓA DÒNG NÀY VÀ PHƯƠNG THỨC ISOBSTACLE() DƯỚI ĐÂY
    /*
    protected boolean isObstacle(int gX, int gY) {
        // ... logic từ phiên bản trước đã bỏ qua BRICK ...
        // Logic này sẽ bị loại bỏ để dùng của lớp cha
    }
    */


    // Các phương thức và thuộc tính BFS không còn cần thiết đã bị xóa.
    // Các phương thức checkMovementCollision, handleBlockedMovement, setRandomDirection
    // được kế thừa từ lớp Enemy và sẽ sử dụng isObstacle() mặc định.
    // render() và die() sử dụng logic chung của lớp Enemy.
    // updateGridPosition() cũng sử dụng logic chung của Entity/Enemy.

    // Thêm getter nếu cần cho các lớp khác
    public double getSpeed() { return speed; }
    public Direction getCurrentDirection() { return currentDirection; }
    public boolean isMoving() { return isMoving; }
    // Getter cho cờ active từ Entity
    // public boolean isActive() { return active; } // Đã có trong Entity
    // Getter cho trạng thái sống game play
    // public boolean isAlive() { return isAlive; } // Đã có trong Entity
}