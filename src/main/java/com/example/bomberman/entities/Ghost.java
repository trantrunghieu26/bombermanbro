package com.example.bomberman.entities;

import com.example.bomberman.Bomberman;
import com.example.bomberman.Map.Map;
import com.example.bomberman.Map.Tile;
import com.example.bomberman.Map.TileType;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ghost extends Enemy {

    private static final double GHOST_SPEED = 65.0; // Tốc độ khá
    private static final int GHOST_SCORE = 400;   // Điểm cao vì khó chịu hơn

    public Ghost(int startGridX, int startGridY, Map map, Bomberman gameManager) {
        super(startGridX, startGridY, GHOST_SPEED, GHOST_SCORE, map, gameManager);

        double frameDuration = 0.2;
        walkLeftAnimation = new Animation(frameDuration, true, Sprite.ghost_left1, Sprite.ghost_left2, Sprite.ghost_left3);
        walkRightAnimation = new Animation(frameDuration, true, Sprite.ghost_right1, Sprite.ghost_right2, Sprite.ghost_right3);
        deadAnimation = new Animation(TIME_TO_DIE / 4.0, false, Sprite.ghost_dead, Sprite.mob_dead1, Sprite.mob_dead2, Sprite.mob_dead3);
    }

    /**
     * Ghost di chuyển ngẫu nhiên, nhưng có khả năng đi xuyên gạch.
     */
    @Override
    protected void calculateNextMove() {
        Player player = gameManager.getPlayer();

        if (player != null && player.isAlive()) {
            // Logic đuổi theo Player đơn giản
            Direction dirToPlayer = getDirectionTowards(player.getGridX(), player.getGridY());

            // Thử đi theo hướng đó nếu có thể
            if (dirToPlayer != Direction.NONE && canMoveTowards(dirToPlayer)) {
                currentDirection = dirToPlayer;
                isMoving = true;
                updateAnimationForDirection(currentDirection);
                return; // Đã quyết định hướng
            }
        }

        // Nếu không đuổi được Player (Player không tồn tại, hoặc hướng đến Player bị chặn)
        // thì di chuyển ngẫu nhiên (như Balloom hoặc logic cũ của Ghost)
        if (!isMoving || random.nextDouble() < 0.15) { // Tăng nhẹ xác suất đổi hướng ngẫu nhiên
            setRandomDirection(); // Hàm này của Enemy đã thông minh hơn
        }
    }
    private Direction getDirectionTowards(int targetGridX, int targetGridY) {
        int dx = targetGridX - this.gridX;
        int dy = targetGridY - this.gridY;

        List<Direction> preferredDirections = new ArrayList<>();
        List<Direction> fallbackDirections = new ArrayList<>();

        // Ưu tiên các hướng chính giúp giảm khoảng cách
        if (dx > 0) preferredDirections.add(Direction.RIGHT);
        else if (dx < 0) preferredDirections.add(Direction.LEFT);

        if (dy > 0) preferredDirections.add(Direction.DOWN);
        else if (dy < 0) preferredDirections.add(Direction.UP);

        // Các hướng phụ (nếu hướng chính không khả thi)
        if (dx == 0) { // Cùng cột, ưu tiên dọc
            if (dy > 0) {
                fallbackDirections.add(Direction.LEFT);
                fallbackDirections.add(Direction.RIGHT);
            } else if (dy < 0) {
                fallbackDirections.add(Direction.LEFT);
                fallbackDirections.add(Direction.RIGHT);
            }
        } else if (dy == 0) { // Cùng hàng, ưu tiên ngang
            if (dx > 0) {
                fallbackDirections.add(Direction.UP);
                fallbackDirections.add(Direction.DOWN);
            } else if (dx < 0) {
                fallbackDirections.add(Direction.UP);
                fallbackDirections.add(Direction.DOWN);
            }
        } else { // Chéo, thêm các hướng vuông góc vào fallback
            if (Math.abs(dx) > Math.abs(dy)) { // Di chuyển ngang là chính
                if (dy > 0) fallbackDirections.add(Direction.DOWN);
                else if (dy < 0) fallbackDirections.add(Direction.UP);
            } else { // Di chuyển dọc là chính
                if (dx > 0) fallbackDirections.add(Direction.RIGHT);
                else if (dx < 0) fallbackDirections.add(Direction.LEFT);
            }
        }


        Collections.shuffle(preferredDirections);
        Collections.shuffle(fallbackDirections);

        // Thử các hướng ưu tiên trước
        for (Direction dir : preferredDirections) {
            if (canMoveTowards(dir)) return dir;
        }
        // Nếu không được, thử các hướng phụ
        for (Direction dir : fallbackDirections) {
            if (canMoveTowards(dir)) return dir;
        }

        return Direction.NONE; // Không tìm thấy hướng tốt
    }



    // Override checkMovementCollision để có hành vi riêng khi đi xuyên gạch
    @Override
    protected boolean checkMovementCollision(double checkPixelX, double checkPixelY) {
        double entitySize = Sprite.SCALED_SIZE;
        double buffer = 1.0; // Ghost có thể cần buffer nhỏ hơn để "lách" tốt hơn

        double innerTop = checkPixelY + buffer;
        double innerBottom = checkPixelY + entitySize - buffer;
        double innerLeft = checkPixelX + buffer;
        double innerRight = checkPixelX + entitySize - buffer;
        double midX = checkPixelX + entitySize / 2.0;
        double midY = checkPixelY + entitySize / 2.0;

        // Kiểm tra 8 điểm, nhưng với logic isObstacleForGhost
        if (isObstacleAtPixelForGhost(innerLeft, innerTop) ||
                isObstacleAtPixelForGhost(innerRight, innerTop) ||
                isObstacleAtPixelForGhost(innerLeft, innerBottom) ||
                isObstacleAtPixelForGhost(innerRight, innerBottom) ||
                isObstacleAtPixelForGhost(midX, innerTop) ||
                isObstacleAtPixelForGhost(midX, innerBottom) ||
                isObstacleAtPixelForGhost(innerLeft, midY) ||
                isObstacleAtPixelForGhost(innerRight, midY)) {
            return true;
        }
        return false;
    }

    // Helper riêng cho Ghost, gọi isObstacle (đã được override trong Ghost)
    private boolean isObstacleAtPixelForGhost(double px, double py) {
        int gx = (int) Math.floor(px / Sprite.SCALED_SIZE);
        int gy = (int) Math.floor(py / Sprite.SCALED_SIZE);
        return this.isObstacle(gx, gy); // Gọi isObstacle của Ghost
    }

    // isObstacle của Ghost (bạn đã có, chỉ Tường và Bom chặn)
    @Override
    protected boolean isObstacle(int gX, int gY) {
        if (map == null || gX < 0 || gX >= map.getCols() || gY < 0 || gY >= map.getRows()) {
            return true;
        }
        Tile tile = map.getTile(gX, gY);
        if (tile != null && tile.getType() == TileType.WALL) { // Chỉ WALL chặn
            return true;
        }
        if (gameManager != null && gameManager.isBombAtGrid(gX, gY)) { // Bom vẫn chặn
            return true;
        }
        return false; // Gạch (BRICK) và ô trống không chặn Ghost
    }
}