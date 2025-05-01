package com.example.bomberman.entities;

import com.example.bomberman.Controller;
import com.example.bomberman.Map.Map;
import com.example.bomberman.Map.Tile;
import com.example.bomberman.Map.TileType;
import com.example.bomberman.graphics.Sprite;
import com.example.bomberman.graphics.Animation; // Import lớp Animation
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Player extends Entity {

    private double pixelX;
    private double pixelY;

    private int gridX;
    private int gridY;

    private double speed = 100.0; // Tốc độ di chuyển (pixel / giây)
    private Direction currentDirection = Direction.NONE;

    private boolean isMoving = false;
    private boolean isAlive = true;
    private boolean isInvincible = false;
    private double invincibilityTimer = 0;
    private double invincibilityDuration = 2.0; // Thời gian bất tử sau khi trúng đòn (giây)

    private List<Bomb> bombs;

    private Controller con;

    // --- Animation attributes ---
    private Animation walkUpAnimation;
    private Animation walkDownAnimation;
    private Animation walkLeftAnimation;
    private Animation walkRightAnimation;
    private Animation idleUpAnimation;
    private Animation idleDownAnimation;
    private Animation idleLeftAnimation;
    private Animation idleRightAnimation;
    private Animation currentAnimation;

    private Animation deadAnimation; // Animation khi player chết

    private double animationTimer = 0;
    private Direction lastNonNoneDirection = Direction.DOWN;

    // TODO: Bomb attributes
    public int bombNumber = 1;
    private int flameLength = 1;
    // TODO: Other state attributes
    private double deathAnimationDuration = 1.0; // Thời gian hiển thị animation chết (giây) // TODO: Điều chỉnh


    // Constructor
    public Player(int startGridX, int startGridY, Map map, Controller con) {
        super(startGridX, startGridY, map);
        this.con = con;
        bombs = new ArrayList<>();

        // --- Initialize animations ---
        double frameDuration = 0.15; // Điều chỉnh giá trị này

        walkUpAnimation = new Animation(frameDuration, true, Sprite.player_up, Sprite.player_up_1, Sprite.player_up_2, Sprite.player_up_1);
        walkDownAnimation = new Animation(frameDuration, true, Sprite.player_down, Sprite.player_down_1, Sprite.player_down_2, Sprite.player_down_1);
        walkLeftAnimation = new Animation(frameDuration, true, Sprite.player_left, Sprite.player_left_1, Sprite.player_left_2, Sprite.player_left_1);
        walkRightAnimation = new Animation(frameDuration, true, Sprite.player_right, Sprite.player_right_1, Sprite.player_right_2, Sprite.player_right_1);

        idleUpAnimation = new Animation(frameDuration, true, Sprite.player_up);
        idleDownAnimation = new Animation(frameDuration, true, Sprite.player_down);
        idleLeftAnimation = new Animation(frameDuration, true, Sprite.player_left);
        idleRightAnimation = new Animation(frameDuration, true, Sprite.player_right);

        deadAnimation = new Animation(frameDuration, false, Sprite.player_dead1, Sprite.player_dead2, Sprite.player_dead3);
        currentAnimation = idleDownAnimation;
    }

    // --- Called by InputHandler ---
    public void setMovingDirection(Direction direction) {
        this.currentDirection = direction;
        this.isMoving = (direction != Direction.NONE);

        if (isMoving) {
            switch (direction) {
                case UP: currentAnimation = walkUpAnimation; lastNonNoneDirection = Direction.UP; break;
                case DOWN: currentAnimation = walkDownAnimation; lastNonNoneDirection = Direction.DOWN; break;
                case LEFT: currentAnimation = walkLeftAnimation; lastNonNoneDirection = Direction.LEFT; break;
                case RIGHT: currentAnimation = walkRightAnimation; lastNonNoneDirection = Direction.RIGHT; break;
            }
            animationTimer = 0;
        } else {
            switch (lastNonNoneDirection) {
                case UP: currentAnimation = idleUpAnimation; break;
                case DOWN: currentAnimation = idleDownAnimation; break;
                case LEFT: currentAnimation = idleLeftAnimation; break;
                case RIGHT: currentAnimation = idleRightAnimation; break;
                default: currentAnimation = idleDownAnimation; break;
            }
            animationTimer = 0;
        }
    }

    // --- Called by Game Loop (AnimationTimer) ---
    public void update(double deltaTime) {
        // CẬP NHẬT TRẠNG THÁI BẤT TỬ
        if (isInvincible) {
            invincibilityTimer -= deltaTime;
            if (invincibilityTimer <= 0) {
                isInvincible = false;
                System.out.println("Player is no longer invincible."); // Log
            }
        }

        if (!isAlive) {
            this.animationTimer += deltaTime;
            if (this.deathAnimationDuration <= this.animationTimer) {
                this.remove();
                this.currentAnimation = null;
            }
            return;
        }
        if (isMoving) {
            double deltaPixelX = 0;
            double deltaPixelY = 0;

            switch (currentDirection) {
                case UP: deltaPixelY = -speed * deltaTime; break;
                case DOWN: deltaPixelY = speed * deltaTime; break;
                case LEFT: deltaPixelX = -speed * deltaTime; break;
                case RIGHT: deltaPixelX = speed * deltaTime; break;
                case NONE: break;
            }

            // --- Logic kiểm tra va chạm và cập nhật vị trí (Kiểm tra riêng từng trục) ---

            double nextPixelX = pixelX + deltaPixelX;
            double nextPixelY = pixelY + deltaPixelY;

            boolean canMoveX = true;
            boolean canMoveY = true;

            // Kiểm tra va chạm theo trục X
            if (deltaPixelX != 0) {
                canMoveX = !checkCollision(nextPixelX, pixelY); // Kiểm tra va chạm chỉ với vị trí X mới
            }

            // Cập nhật tạm thời pixelX để kiểm tra va chạm Y từ vị trí mới hoặc neo lại
            double tempPixelX = pixelX;
            if (canMoveX) {
                tempPixelX = nextPixelX;
            } else {
                // Neo lại ở cạnh Tile khi va chạm theo X
                if (deltaPixelX > 0) { // Va chạm khi đi sang phải
                    tempPixelX = (int) Math.floor((pixelX + Sprite.SCALED_SIZE) / Sprite.SCALED_SIZE) * Sprite.SCALED_SIZE - Sprite.SCALED_SIZE;
                } else { // Va chạm khi đi sang trái
                    tempPixelX = (int) Math.ceil(pixelX / Sprite.SCALED_SIZE) * Sprite.SCALED_SIZE;
                }
            }

            // Kiểm tra va chạm theo trục Y (dựa trên tempPixelX)
            if (deltaPixelY != 0) {
                canMoveY = !checkCollision(tempPixelX, nextPixelY); // Kiểm tra va chạm chỉ với vị trí Y mới
            }

            // --- Cập nhật vị trí cuối cùng ---
            if (canMoveX) {
                pixelX = nextPixelX;
            }
            if (canMoveY) {
                pixelY = nextPixelY;
            }

            // Cập nhật vị trí lưới dựa trên vị trí pixel mới
            this.gridX = (int) Math.round(pixelX / Sprite.SCALED_SIZE);
            this.gridY = (int) Math.round(pixelY / Sprite.SCALED_SIZE);

            // --- Cập nhật trạng thái animation ---
            if (currentAnimation != null) {
                animationTimer += deltaTime; // Tăng thời gian animation
            }

        } else {
            // --- Cập nhật trạng thái animation khi đứng yên ---
            if (currentAnimation != null && isAlive) {
                animationTimer += deltaTime;
            }
        }
        // TODO: Other update logic (collect items, take damage, death, invincibility...)
    }

    // Phương thức kiểm tra va chạm tại một vị trí pixel cụ thể (x, y)
    // Trả về true nếu CÓ va chạm, false nếu KHÔNG va chạm (có thể đi qua)


    // Phương thức được gọi bởi Vòng lặp Game để vẽ Player
    @Override
    public void render(GraphicsContext gc) {
        // --- Get current animation frame to draw ---
        Image currentImage = null;
        if (currentAnimation != null) {
            // Lấy frame hiện tại từ animation đang chạy
            Sprite currentSpriteFrame = currentAnimation.getFrame(animationTimer);
            if (currentSpriteFrame != null) {
                currentImage = currentSpriteFrame.getFxImage();
            }
        } else {
            // Fallback: Nếu không có animation nào được gán, dùng sprite mặc định
            currentImage = Sprite.player_down.getFxImage();
        }

        // Fallback: Nếu vẫn không có hình ảnh, dùng sprite mặc định cuối cùng
        if (currentImage == null) {
            currentImage = Sprite.player_down.getFxImage();
        }


        if (currentImage != null) {
            // XỬ LÝ VẼ KHI BẤT TỬ (VÍ DỤ: NHẤP NHÁY)
            if (isInvincible) {
                // Vẽ nhấp nháy: chỉ vẽ ở các frame nhất định
                // Sử dụng animationTimer để tạo hiệu ứng nhấp nháy đơn giản
                double blinkRate = 0.1; // Điều chỉnh tốc độ nhấp nháy
                if (Math.floor(animationTimer / blinkRate) % 2 == 0) {
                    gc.drawImage(currentImage, pixelX, pixelY);
                }
            } else {
                // Vẽ bình thường khi không bất tử
                gc.drawImage(currentImage, pixelX, pixelY);
            }
        }
    }

    public void requestPlayerPlaceBomb() {
        int playerGridX = this.getGridX();
        int playerGridY = this.getGridY();

        if (this.getBombNumber() <= 0) {
            System.out.println("Cannot place bomb: No bombs left!");
        } else {
            boolean bombAlreadyExists = false;
            for (Bomb existingBomb : bombs) {
                if (existingBomb.getGridX() == playerGridX && existingBomb.getGridY() == playerGridY) {
                    bombAlreadyExists = true;
                    break;
                }
            }

            Tile currentTile = map.getTile(playerGridX, playerGridY);
            boolean canPlaceOnTile = (currentTile != null && currentTile.isWalkable()); // Có thể đặt bom trên ô đi được (trống, cửa)

            // TODO: Cần kiểm tra nếu trên ô đó có vật thể khác không đi qua được khi bomb chưa nổ (vd: quái vật, item?)
            // Hiện tại Player có thể đặt bom dưới chân mình và quái vật cũng có thể đi qua ô có bomb chưa nổ.

            if (canPlaceOnTile && !bombAlreadyExists) {
                // Truyền tham chiếu đến Bomberman (this) vào constructor của Bomb
                Bomb newBomb = new Bomb(playerGridX, playerGridY, this.getFlameLength(), this);
                newBomb.setKick(true);

                addBomb(newBomb); // Thêm bomb vào danh sách

                this.decreaseBombNumber(1); // Giảm số bomb của người chơi
                System.out.println("Bomb placed at (" + playerGridX + ", " + playerGridY + "). Bombs left: " + this.getBombNumber());

            } else {
                System.out.println("Cannot place bomb at (" + playerGridX + ", " + playerGridY + "). Bomb already exists or tile not walkable.");
            }
        }
    }

    public void die() {
        if (isAlive) {
            isAlive = false;
            isMoving = false;
            currentDirection = Direction.NONE;
            currentAnimation = deadAnimation;
            animationTimer = 0; // Reset animation
        }
    }

    public void addBomb(Bomb bomb) {
        bombs.add(bomb);
    }

    // --- Getters ---
    public double getPixelX() { return pixelX; }
    public double getPixelY() { return pixelY; }
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public Direction getCurrentDirection() { return currentDirection; }
    public boolean isMoving() { return isMoving; }
    public int getBombNumber() { return bombNumber; }
    public int getFlameLength() { return flameLength; }
    public List<Bomb> getBombs() {
        return bombs;
    }
    public boolean isAlive() { return isAlive; }
    public boolean isInvincible() { return isInvincible; }
    public Controller getController() { return this.con; }
    public void decreaseBombNumber(int amount) { this.bombNumber -= amount; if (this.bombNumber < 0) this.bombNumber = 0; }
    public void increaseBombNumber(int amount) { this.bombNumber += amount; }

    // Phương thức xử lý khi Player trở nên bất tử
    public void becomeInvincible(double duration) {
        isInvincible = true;
        invincibilityTimer = duration;
        System.out.println("Player is now invincible for " + duration + " seconds."); // Log
    }
}
