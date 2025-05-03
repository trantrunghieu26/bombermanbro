package com.example.bomberman.entities;

import com.example.bomberman.Map.Map;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Enemy extends Entity {

    /// ////////////////////////Attributes/////////////////////////////////////////

    protected double speed;// Tốc độ di chuyển của địch (có thể khác với người chơi)
    protected Direction currentDirection = Direction.NONE;
    protected boolean isMoving = false;
    protected double deathAnimationDuration = 1.0; // Thời gian hiển thị animation chết (giây)
    protected double deathTimer = 0; // Thời gian tồn tại của animation chết

    // --- Animation attributes ---
    protected Animation walkLeftAnimation;
    protected Animation walkRightAnimation;
    protected Animation walkUpAnimation;
    protected Animation walkDownAnimation;
    protected Animation deadAnimation;
    protected Animation currentAnimation;

    protected double animationTimer = 0;
    protected Direction lastNonNoneDirection = Direction.LEFT; // Hướng nhìn mặc định

    protected boolean isAlive = true;

    protected double frameDuration = 0.15;

    /////////////////////////////////////////////////////Method/////////////////////////////////////////////


    // Constructor
    public Enemy(int startGridX, int startGridY, Map map) {
        super(startGridX, startGridY, map);
    }

    // --- Enemy AI (Simple Random Movement) ---
    public void update(double deltaTime) {

        if (!isAlive) {

            this.animationTimer += deltaTime;
            // Kiểm tra nếu animation chết đã kết thúc
            if (animationTimer >= deathAnimationDuration) { // TODO: Sử dụng animation.getTotalDuration() nếu có
                remove(); // Đánh dấu để loại bỏ hoàn toàn
            }
            return;
        }

        // Basic AI: Random movement
        if (!isMoving) {
            if (Math.random() < 0.02) { // 2% chance to change direction each frame
                int random = (int) (Math.random() * 4);
                switch (random) {
                    case 0:
                        currentDirection = Direction.UP;
                        break;
                    case 1:
                        currentDirection = Direction.DOWN;
                        break;
                    case 2:
                        currentDirection = Direction.LEFT;
                        break;
                    case 3:
                        currentDirection = Direction.RIGHT;
                        break;
                }
                isMoving = true;
            }
        }

        // --- Movement logic ---
        double distance = speed * deltaTime;
        double dx = 0, dy = 0;

        switch (currentDirection) {
            case UP:
                dy = -distance;
                currentAnimation = walkUpAnimation;
                lastNonNoneDirection = currentDirection;
                break;
            case DOWN:
                dy = distance;
                currentAnimation = walkDownAnimation;
                lastNonNoneDirection = currentDirection;
                break;
            case LEFT:
                dx = -distance;
                currentAnimation = walkLeftAnimation;
                lastNonNoneDirection = currentDirection;
                break;
            case RIGHT:
                dx = distance;
                currentAnimation = walkRightAnimation;
                lastNonNoneDirection = currentDirection;
                break;
            case NONE:
                isMoving = false;
                // Set idle animation based on last direction
                switch (lastNonNoneDirection) {
                    case UP:
                        currentAnimation = walkUpAnimation; // Tạm thời
                        break;
                    case DOWN:
                        currentAnimation = walkDownAnimation;
                        break;
                    case LEFT:
                        currentAnimation = walkLeftAnimation;
                        break;
                    case RIGHT:
                        currentAnimation = walkRightAnimation;
                        break;
                }
                break;
        }

        // --- Collision detection ---
        if (dx != 0 || dy != 0) {
            double newPixelX = pixelX + dx;
            double newPixelY = pixelY + dy;

            int newGridX = (int) (newPixelX / Sprite.SCALED_SIZE);
            int newGridY = (int) (newPixelY / Sprite.SCALED_SIZE);

            // Kiểm tra va chạm với bản đồ (đơn giản, có thể cải tiến)

            if (!checkCollision(newPixelX, newPixelY)) {
                pixelX = newPixelX;
                pixelY = newPixelY;
                gridX = newGridX;
                gridY = newGridY;
                isMoving = true;
            } else {
                isMoving = false;
                currentDirection = Direction.NONE; // Dừng di chuyển nếu có va chạm
            }
        }

        // Update animation timer
        if (isMoving) {
            animationTimer += deltaTime;
        }
    }


    @Override
    public void render(GraphicsContext gc) {
        Image currentImage = null;
        if (currentAnimation != null) {
            Sprite currentSpriteFrame = currentAnimation.getFrame(animationTimer);
            if (currentSpriteFrame != null) {
                currentImage = currentSpriteFrame.getFxImage();
            }
        }

        if (currentImage == null) {
            currentImage = walkLeftAnimation.getFrame(animationTimer).getFxImage(); // Default
        }

        gc.drawImage(currentImage, this.pixelX, this.pixelY);
    }

    public void takeHit() {
        if (isAlive) {
            isAlive = false;
            isMoving = false;
            currentDirection = Direction.NONE;
            currentAnimation = deadAnimation;
            animationTimer = 0; // Reset animation
        }
    }

    // --- Getters and Setters --- (Needed for other parts of the game)
    public double getPixelX() { return pixelX; }
    public double getPixelY() { return pixelY; }
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public boolean isAlive() { return isAlive; }
    public boolean isMoving() { return isMoving; }
    public Direction getCurrentDirection() { return currentDirection; }

    public void setPixelX(double pixelX) {
        this.pixelX = pixelX;
    }

    public void setPixelY(double pixelY) {
        this.pixelY = pixelY;
    }

    public void setGridX(int gridX) {
        this.gridX = gridX;
    }

    public void setGridY(int gridY) {
        this.gridY = gridY;
    }

    public void setCurrentDirection(Direction currentDirection) {
        this.currentDirection = currentDirection;
    }

    public void setMoving(boolean moving) {
        isMoving = moving;
    }

    public void setMap(Map map) {
        this.map = map;
    }

    public void setCurrentAnimation(Animation currentAnimation) {
        this.currentAnimation = currentAnimation;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public static boolean isCharOfEnemy(char ch) {
        return ch == '1' || ch == '2' || ch == '3' || ch == '4' || ch == '5' || ch == '6';
    }
}