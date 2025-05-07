package com.example.bomberman.entities;

import com.example.bomberman.Bomberman;
import com.example.bomberman.Map.Map;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Doll extends Enemy {

    private static final double DOLL_SPEED = 100.0; // Tốc độ vừa phải
    private static final int DOLL_SCORE = 300;    // Điểm cao hơn chút
    private static final double BOMB_COOLDOWN = 0.1; // Đặt bom mỗi 5 giây (điều chỉnh)
    private static final int ENEMY_BOMB_FLAME_LENGTH = 5; // Lửa của bom Enemy

    private double bombPlacementCooldownTimer = BOMB_COOLDOWN; // Bắt đầu có thể đặt bom ngay
    private int bombsPlacedByThisEnemy = 0; // Đếm số bom Enemy này đã đặt và chưa nổ
    private final int MAX_ENEMY_BOMBS_ACTIVE = 1; // Chỉ cho phép đặt 1 quả bom tại một thời điểm
    private Bomb activeBomb = null;

    public Doll(int startGridX, int startGridY, Map map, Bomberman gameManager) {
        super(startGridX, startGridY, DOLL_SPEED, DOLL_SCORE, map, gameManager);

        double frameDuration = 0.2;
        walkLeftAnimation = new Animation(frameDuration, true, Sprite.doll_left1, Sprite.doll_left2, Sprite.doll_left3);
        walkRightAnimation = new Animation(frameDuration, true, Sprite.doll_right1, Sprite.doll_right2, Sprite.doll_right3);
        deadAnimation = new Animation(TIME_TO_DIE / 4.0, false, Sprite.doll_dead, Sprite.mob_dead1, Sprite.mob_dead2, Sprite.mob_dead3);

        // Animation ban đầu được đặt bởi findInitialClearDirection() trong Enemy constructor
    }

    /**
     * Doll sẽ cố gắng đặt bom nếu Player ở gần và có đường đi đến Player,
     * hoặc di chuyển ngẫu nhiên.
     */
    @Override
    protected void calculateNextMove() {
        if (activeBomb != null && activeBomb.isActive()) {
            //System.out.println("DOLL DEBUG: Active bomb exists, attempting to flee.");
            fleeFromBomb(activeBomb); // Hàm mới để chạy trốn
            return; // Đã quyết định hành động là chạy
        }
        Player player = gameManager.getPlayer();
        if (player != null && player.isAlive() && bombPlacementCooldownTimer <= 0 && activeBomb==null) {
            if (shouldPlaceBomb(player)) {
                placeEnemyBomb();
                // Sau khi đặt bom, ngay lập tức cố gắng chạy trốn (nếu bom được đặt thành công)
                if (activeBomb != null && activeBomb.isActive()) { // Kiểm tra lại activeBomb sau khi đặt
                    //System.out.println("DOLL: Bomb just placed. Forcing flee.");
                    fleeFromBomb(activeBomb);
                } else {
                    // Đặt bom thất bại (hiếm) hoặc activeBomb không được gán đúng
                    //System.out.println("DOLL: Placed bomb but activeBomb is null or inactive. Moving randomly.");
                    setRandomDirection(); // Fallback
                }
                return; // Đã quyết định hành động (đặt bom và chạy, hoặc random)
            }
        }

        // Nếu không đặt bom, di chuyển ngẫu nhiên (sử dụng logic của lớp Enemy)
        // Hoặc nếu bạn muốn Doll thông minh hơn, có thể thêm 1 chút logic đuổi theo đơn giản
        if (!isMoving || currentDirection == Direction.NONE) {
            setRandomDirection();
        }
        // Nếu đang di chuyển rồi thì cứ để nó đi tiếp, move() sẽ xử lý chặn
    }
    private void fleeFromBomb(Bomb bombToFlee) {
        // Tìm hướng an toàn nhất để chạy (xa bom nhất và đi được)
        List<Direction> possibleFleeDirections = new ArrayList<>(Arrays.asList(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT));
        Collections.shuffle(possibleFleeDirections); // Thêm chút ngẫu nhiên

        Direction bestFleeDirection = Direction.NONE;


        Direction directionToBomb = getDirectionTowards(bombToFlee.getGridX(), bombToFlee.getGridY());
        Direction oppositeToBomb = getOppositeDirection(directionToBomb);

        // Ưu tiên hướng ngược lại với bom
        if (oppositeToBomb != Direction.NONE && canMoveTowards(oppositeToBomb)) {
            bestFleeDirection = oppositeToBomb;
        } else {
            // Nếu không đi được hướng ngược lại, thử các hướng khác
            for (Direction dir : possibleFleeDirections) {
                // Không chọn hướng đi thẳng vào bom trừ khi không còn lựa chọn nào khác
                if (dir == directionToBomb && possibleFleeDirections.size() > 1) continue;

                if (canMoveTowards(dir)) {
                    bestFleeDirection = dir;
                    break; // Tìm thấy hướng đi được đầu tiên
                }
            }
        }

        if (bestFleeDirection != Direction.NONE) {
            //System.out.println("DOLL DEBUG: Fleeing from bomb. Chosen direction: " + bestFleeDirection);
            currentDirection = bestFleeDirection;
            isMoving = true;
            updateAnimationForDirection(currentDirection);
        } else {
            currentDirection = Direction.NONE;
            isMoving = false;
        }
    }
    private Direction getDirectionTowards(int targetGridX, int targetGridY) {
        int dx = targetGridX - this.gridX;
        int dy = targetGridY - this.gridY;

        if (Math.abs(dx) > Math.abs(dy)) { // Ưu tiên di chuyển ngang
            return (dx > 0) ? Direction.RIGHT : Direction.LEFT;
        } else if (Math.abs(dy) > 0) { // Rồi đến di chuyển dọc
            return (dy > 0) ? Direction.DOWN : Direction.UP;
        }
        return Direction.NONE; // Đang ở cùng ô
    }

    // Hàm helper lấy hướng ngược lại
    private Direction getOppositeDirection(Direction dir) {
        switch (dir) {
            case UP: return Direction.DOWN;
            case DOWN: return Direction.UP;
            case LEFT: return Direction.RIGHT;
            case RIGHT: return Direction.LEFT;
            default: return Direction.NONE;
        }
    }
    private boolean shouldPlaceBomb(Player player) {
        int playerGridX = player.getGridX();
        int playerGridY = player.getGridY();
        int dx = Math.abs(gridX - playerGridX);
        int dy = Math.abs(gridY - playerGridY);

        // Điều kiện đơn giản: Player gần và trên cùng hàng/cột
        if ((dx <= 4 && dy == 0) || (dy <= 4 && dx == 0)) {
            // Kiểm tra đường thẳng đến Player có bị chặn không (đơn giản)
            if (dx == 0) { // Cùng cột
                for (int y = Math.min(gridY, playerGridY) + 1; y < Math.max(gridY, playerGridY); y++) {
                    if (isObstacle(gridX, y)) return false; // Có vật cản
                }
            } else { // Cùng hàng
                for (int x = Math.min(gridX, playerGridX) + 1; x < Math.max(gridX, playerGridX); x++) {
                    if (isObstacle(x, gridY)) return false; // Có vật cản
                }
            }
            return !isObstacle(gridX, gridY); // Đảm bảo ô Doll đứng không bị chặn
        }
        return false;
    }
    private void placeEnemyBomb() {
        if (activeBomb != null && activeBomb.isActive()) {
            //System.out.println("Doll: Already has an active bomb.");
            return; // Đã có bom active, không đặt thêm
        }

        Bomb enemyBomb = new Bomb(gridX, gridY, ENEMY_BOMB_FLAME_LENGTH, null, map, gameManager); // Owner = null

        gameManager.addBomb(enemyBomb); // Thêm bom vào danh sách chung của game
        activeBomb = enemyBomb;
        bombPlacementCooldownTimer = BOMB_COOLDOWN; // Reset cooldown
        //System.out.println("Doll placed bomb. Cooldown started.");
    }

    /**
     * Phương thức này được gọi từ Bomberman khi một quả bom (có thể là của Enemy) đã nổ
     * và cần giải phóng "slot" bom cho Enemy đó.
     * Chúng ta cần sửa Bomb.java và Bomberman.java để hỗ trợ điều này.
     * Hiện tại, chúng ta sẽ giả định có một cách để giảm bombsPlacedByThisEnemy khi bom nổ.
     * Một cách đơn giản là nếu bom không có owner (owner == null), khi nó bị xóa khỏi list bombs,
     * chúng ta có thể duyệt qua list enemies và nếu enemy là Doll, giảm bombsPlacedByThisEnemy
     * (cần cẩn thận nếu có nhiều Doll).
     * <p>
     * Cách tốt hơn: Khi tạo Bomb, truyền một tham chiếu ngược lại Enemy (nếu là bom của Enemy)
     * hoặc một ID đặc biệt.
     */
    public void enemyBombExploded() {
        if (bombsPlacedByThisEnemy > 0) {
            bombsPlacedByThisEnemy--;
        }
    }


    @Override
    public void update(double deltaTime) {
        super.update(deltaTime); // Gọi update của Enemy để xử lý di chuyển, chết...

        if (isAlive) {
            if (bombPlacementCooldownTimer > 0) {
                bombPlacementCooldownTimer -= deltaTime;
            }
            // Kiểm tra xem bom đã đặt có còn active không
            if (activeBomb != null && !activeBomb.isActive()) {
                activeBomb = null; // Bom đã nổ hoặc bị xóa, reset tham chiếu
                //System.out.println("Doll: Active bomb is no longer active.");
            }
        }
    }
}