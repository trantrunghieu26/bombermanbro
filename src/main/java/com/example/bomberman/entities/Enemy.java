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

public abstract class Enemy { // Đổi thành abstract class

    // --- Thuộc tính vị trí và di chuyển ---
    protected double pixelX;
    protected double pixelY;
    protected int gridX;
    protected int gridY;
    protected double speed; // Tốc độ riêng của mỗi loại Enemy
    protected Direction currentDirection = Direction.NONE;
    protected boolean isMoving = false;
    protected Random random = new Random(); // Để di chuyển ngẫu nhiên cho Enemy cơ bản

    // --- Thuộc tính trạng thái ---
    protected boolean isAlive = true;
    protected double dyingTimer = 0; // Thời gian cho animation chết
    protected final double TIME_TO_DIE = 0.8; // Thời gian animation chết (ví dụ)
    protected int scoreValue; // Điểm nhận được khi tiêu diệt

    // --- Tham chiếu ---
    protected Map map;
    protected Bomberman gameManager;

    // --- Animation ---
    protected Animation walkLeftAnimation;
    protected Animation walkRightAnimation;
    protected Animation deadAnimation;
    protected Animation currentAnimation;
    protected double animationTimer = 0;
    // --- Thêm các thuộc tính mới để xử lý bị kẹt ---
    private Direction lastBlockedDirection = Direction.NONE;
    private int consecutiveBlocks = 0;
    private final int MAX_CONSECUTIVE_BLOCKS = 4;

    // Constructor
    public Enemy(int startGridX, int startGridY, double speed, int scoreValue, Map map, Bomberman gameManager) {
        this.gridX = startGridX;
        this.gridY = startGridY;
        this.pixelX = startGridX * Sprite.SCALED_SIZE;
        this.pixelY = startGridY * Sprite.SCALED_SIZE;
        this.speed = speed;
        this.scoreValue = scoreValue;
        this.map = map;
        this.gameManager = gameManager;
        this.isAlive = true;
        // Khởi tạo animation sẽ được thực hiện trong lớp con
        // Chọn hướng di chuyển ban đầu ngẫu nhiên (hoặc logic khác)
        setRandomDirection();
    }

    // --- Phương thức cập nhật chung ---
    public void update(double deltaTime) {
        if (!isAlive) {
            // Xử lý animation chết
            dyingTimer += deltaTime;
            animationTimer += deltaTime; // Cập nhật timer cho animation chết
            if (dyingTimer >= TIME_TO_DIE) {
                // active = false; // Đánh dấu là không còn active để bị xóa (Cần thêm thuộc tính active nếu dùng)
                // Hoặc đơn giản là không làm gì nữa, chờ bị xóa khỏi list
            }
            return; // Dừng update nếu đang chết
        }

        // Cập nhật timer animation
        animationTimer += deltaTime;

        // Tính toán bước di chuyển tiếp theo (logic cụ thể trong lớp con)
        calculateNextMove();

        // Thực hiện di chuyển
        move(deltaTime);

        // Kiểm tra va chạm với Flame (có thể làm ở Bomberman.java)
        // checkFlameCollisions();
    }

    // --- Phương thức di chuyển ---
    protected void move(double deltaTime) {
        if (!isMoving || currentDirection == Direction.NONE) {
            return;
        }

        double deltaPixelX = 0;
        double deltaPixelY = 0;
        Direction intendedDirection = currentDirection; // Lưu hướng đang cố đi

        switch (currentDirection) {
            case UP:
                deltaPixelY = -speed * deltaTime;
                break;
            case DOWN:
                deltaPixelY = speed * deltaTime;
                break;
            case LEFT:
                deltaPixelX = -speed * deltaTime;
                currentAnimation = walkLeftAnimation;
                break; // Cập nhật animation khi rẽ trái
            case RIGHT:
                deltaPixelX = speed * deltaTime;
                currentAnimation = walkRightAnimation;
                break; // Cập nhật animation khi rẽ phải
            case NONE:
                isMoving = false;
                return; // Đã kiểm tra ở trên nhưng để chắc chắn
        }

        double nextPixelX = pixelX + deltaPixelX;
        double nextPixelY = pixelY + deltaPixelY;

        // Kiểm tra va chạm VỚI TILE VÀ BOM trước khi di chuyển
        if (!checkMovementCollision(nextPixelX, nextPixelY)) {
            pixelX = nextPixelX;
            pixelY = nextPixelY;
            updateGridPosition();
            updateAnimationForDirection(intendedDirection); // Cập nhật animation
            consecutiveBlocks = 0; // Reset bộ đếm kẹt
            lastBlockedDirection = Direction.NONE;
            if (this instanceof Oneal) {
                Oneal oneal = (Oneal) this;
                if (!oneal.isPathEmpty() && intendedDirection == oneal.peekNextPathDirection()) {
                    oneal.consumeNextPathDirection();
                }
            }

        } else {
            // Bị chặn!
            isMoving = false; // Dừng di chuyển theo hướng này

            // Xử lý logic khi bị chặn
            handleBlockedMovement(intendedDirection);

            // Nếu là Oneal và đang theo path, xóa path vì nó không còn đúng
            if (this instanceof Oneal) {
                ((Oneal) this).clearPath();
            }
        }
    }
    protected void handleBlockedMovement(Direction blockedDirection) {
        // Đếm số lần bị chặn liên tiếp theo cùng một logic/hướng
        if (blockedDirection == lastBlockedDirection) {
            consecutiveBlocks++;
        } else {
            consecutiveBlocks = 1;
            lastBlockedDirection = blockedDirection;
        }

        // Nếu bị kẹt ở hướng này quá nhiều lần -> dừng hẳn
        if (consecutiveBlocks >= MAX_CONSECUTIVE_BLOCKS) {
            //System.out.println("ENEMY: Stuck threshold reached for " + blockedDirection + ". Stopping.");
            currentDirection = Direction.NONE;
            isMoving = false;
            consecutiveBlocks = 0; // Reset
            lastBlockedDirection = Direction.NONE;
            return;
        }

        // Tìm hướng thay thế khả thi
        List<Direction> alternatives = new ArrayList<>(Arrays.asList(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT));
        alternatives.remove(blockedDirection); // Không thử lại hướng vừa bị chặn
        Collections.shuffle(alternatives);

        for (Direction alternativeDir : alternatives) {
            if (canMoveTowards(alternativeDir)) { // Kiểm tra xem hướng mới có đi được không
                //System.out.println("ENEMY: Found alternative direction: " + alternativeDir);
                currentDirection = alternativeDir;
                isMoving = true; // Thử đi theo hướng mới
                updateAnimationForDirection(alternativeDir);
                // Reset bộ đếm vì đã tìm được đường mới (hoặc không reset nếu muốn nó tích lũy?)
                // consecutiveBlocks = 0; // Reset ở đây nếu muốn mỗi lần đổi hướng là một lần thử mới
                // lastBlockedDirection = Direction.NONE; // Reset ở đây nếu muốn
                return; // Đã tìm thấy hướng mới, thoát
            }
        }

        // Không tìm thấy hướng thay thế nào khả thi -> dừng lại
        //System.out.println("ENEMY: No alternative direction found after being blocked. Stopping.");
        currentDirection = Direction.NONE;
        isMoving = false;
    }
    protected boolean canMoveTowards(Direction direction) {
        if (direction == Direction.NONE) return true;

        double testPixelX = pixelX;
        double testPixelY = pixelY;
        // Kiểm tra một bước rất nhỏ (ví dụ 1 pixel)
        double smallStep = 2.0;

        switch (direction) {
            case UP:    testPixelY -= smallStep; break;
            case DOWN:  testPixelY += smallStep; break;
            case LEFT:  testPixelX -= smallStep; break;
            case RIGHT: testPixelX += smallStep; break;
        }
        return !checkMovementCollision(testPixelX, testPixelY);
    }


    // --- Phương thức kiểm tra va chạm khi di chuyển (chỉ với Tile và Bomb) ---
    protected boolean checkMovementCollision(double checkPixelX, double checkPixelY) {
        double entitySize = Sprite.SCALED_SIZE;
        // Giảm buffer một chút để đi qua chỗ hẹp dễ hơn, nhưng tăng số điểm kiểm tra
        double buffer = 2.0; // Thử nghiệm giá trị này (2.0, 3.0, 4.0)

        // Tính toán tọa độ các điểm kiểm tra bên trong hitbox dự kiến
        double innerTop = checkPixelY + buffer;
        double innerBottom = checkPixelY + entitySize - buffer;
        double innerLeft = checkPixelX + buffer;
        double innerRight = checkPixelX + entitySize - buffer;
        double midX = checkPixelX + entitySize / 2.0;
        double midY = checkPixelY + entitySize / 2.0;

        // Kiểm tra 8 điểm: 4 góc và 4 điểm giữa cạnh
        if (isObstacleAtPixel(innerLeft, innerTop) ||       // Top-Left
                isObstacleAtPixel(innerRight, innerTop) ||      // Top-Right
                isObstacleAtPixel(innerLeft, innerBottom) ||    // Bottom-Left
                isObstacleAtPixel(innerRight, innerBottom) ||   // Bottom-Right
                isObstacleAtPixel(midX, innerTop) ||            // Mid-Top
                isObstacleAtPixel(midX, innerBottom) ||         // Mid-Bottom
                isObstacleAtPixel(innerLeft, midY) ||           // Mid-Left
                isObstacleAtPixel(innerRight, midY)) {          // Mid-Right
            return true; // Va chạm được phát hiện
        }

        return false; // Không có va chạm
    }
    private boolean isObstacleAtPixel(double px, double py) {
        int gx = (int) Math.floor(px / Sprite.SCALED_SIZE);
        int gy = (int) Math.floor(py / Sprite.SCALED_SIZE);
        return isObstacle(gx, gy);
    }
    protected void updateGridPosition() {
        gridX = (int) Math.round(pixelX / Sprite.SCALED_SIZE);
        gridY = (int) Math.round(pixelY / Sprite.SCALED_SIZE);
    }
    protected void updateAnimationForDirection(Direction dir) {
        Animation targetAnimation = null;
        switch (dir) {
            case LEFT:  targetAnimation = walkLeftAnimation; break;
            case RIGHT: targetAnimation = walkRightAnimation; break;
            // Thêm case cho UP/DOWN nếu có animation riêng
        }
        if (targetAnimation != null && currentAnimation != targetAnimation) {
            currentAnimation = targetAnimation;
            animationTimer = 0;
        }
    }


    // --- Helper kiểm tra xem một ô lưới có phải là vật cản không ---
    protected boolean isObstacle(int gX, int gY) {
        // Kiểm tra biên bản đồ
        if (map == null || gX < 0 || gX >= map.getCols() || gY < 0 || gY >= map.getRows()) {
            return true; // Ngoài map là vật cản
        }

        // Kiểm tra Tile
        Tile tile = map.getTile(gX, gY);
        if (tile != null && (tile.getType() == TileType.WALL || tile.getType() == TileType.BRICK)) {
            return true; // Tường hoặc Gạch là vật cản
        }

        // Kiểm tra Bomb
        if (gameManager != null && gameManager.isBombAtGrid(gX, gY)) {
            // Chỉ coi Bomb là vật cản nếu Enemy không phải loại đi xuyên Bom (nếu có)
            // if (!canPassThroughBombs) // Ví dụ
            return true; // Có Bom là vật cản
        }

        // TODO: Kiểm tra Enemy khác nếu cần (thường Enemy có thể đi xuyên nhau)

        return false; // Không phải vật cản
    }


    // --- Phương thức để lớp con định nghĩa logic chọn hướng đi ---
    // Oneal sẽ override phương thức này để dùng BFS
    protected abstract void calculateNextMove();

    // --- Phương thức chọn hướng ngẫu nhiên (cho Enemy cơ bản hoặc khi bị chặn) ---
    protected void setRandomDirection() {
        List<Direction> directions = new ArrayList<>(Arrays.asList(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT));
        Collections.shuffle(directions);

        for (Direction dir : directions) {
            if (canMoveTowards(dir)) { // Chỉ chọn hướng có thể đi được
                currentDirection = dir;
                isMoving = true;
                updateAnimationForDirection(currentDirection);
                // Reset bộ đếm kẹt khi chủ động đổi hướng
                consecutiveBlocks = 0;
                lastBlockedDirection = Direction.NONE;
                return;
            }
        }
        // Không tìm thấy hướng nào khả thi
        currentDirection = Direction.NONE;
        isMoving = false;
    }


    // --- Phương thức render chung ---
    public void render(GraphicsContext gc) {
        if (gc == null) return;

        Image currentImage = null;
        if (currentAnimation != null) {
            Sprite currentSpriteFrame = currentAnimation.getFrame(animationTimer);
            if (currentSpriteFrame != null) {
                currentImage = currentSpriteFrame.getFxImage();
            }
        }

        // Fallback nếu không có animation (không nên xảy ra nếu lớp con khởi tạo đúng)
        if (currentImage == null) {
            if (isAlive) {
                currentImage = Sprite.balloom_left1.getFxImage(); // Ảnh mặc định khi sống
            } else {
                currentImage = Sprite.mob_dead1.getFxImage(); // Ảnh mặc định khi chết
            }
        }

        if (currentImage != null) {
            // Vẽ với offset Y của UI panel
            gc.drawImage(currentImage, pixelX, pixelY + Bomberman.UI_PANEL_HEIGHT, Sprite.SCALED_SIZE, Sprite.SCALED_SIZE);
        }
    }

    // --- Phương thức xử lý khi Enemy chết ---
    public void die() {
        if (!isAlive) return; // Chỉ chết một lần

        System.out.println("Enemy died at (" + gridX + ", " + gridY + "). Score: " + scoreValue);
        isAlive = false;
        isMoving = false;
        currentDirection = Direction.NONE;
        dyingTimer = 0;
        animationTimer = 0; // Reset timer cho animation chết
        currentAnimation = deadAnimation; // Chuyển sang animation chết

        // Thông báo cho gameManager để cộng điểm
        if (gameManager != null) {
            gameManager.addScore(this.scoreValue);
            // gameManager.enemyDied(this); // Có thể cần phương thức này để quản lý portal
        }
        // TODO: Phát âm thanh Enemy chết
    }

    // --- Getters ---
    public boolean isAlive() { return isAlive; }
    public double getPixelX() { return pixelX; }
    public double getPixelY() { return pixelY; }
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public double getDyingTimer() { return dyingTimer; }
    public double getTimeToDie() { return TIME_TO_DIE; }

    // Có thể cần thêm getter/setter khác
}