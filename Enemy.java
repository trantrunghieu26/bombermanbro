package com.example.bomberman.entities;

import com.example.bomberman.Map.Map;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Enemy extends Entity {

    private double pixelX;
    private double pixelY;

    private int gridX;
    private int gridY;

    private double speed = 170.0; // Tốc độ di chuyển của địch (có thể khác với người chơi)
    private Direction currentDirection = Direction.NONE;
    private boolean isMoving = false;

    private Map map;

    // --- Animation attributes ---
    private Animation walkLeftAnimation;
    private Animation walkRightAnimation;
    private Animation walkUpAnimation;
    private Animation walkDownAnimation;
    private Animation deadAnimation;
    private Animation currentAnimation;
    private double animationTimer = 0;
    private Direction lastNonNoneDirection = Direction.LEFT; // Hướng nhìn mặc định

    private boolean isAlive = true;

    // Constructor
    public Enemy(int startGridX, int startGridY, Map map) {
        super(map);
        this.gridX = startGridX;
        this.gridY = startGridY;
        this.pixelX = gridX * Sprite.SCALED_SIZE;
        this.pixelY = gridY * Sprite.SCALED_SIZE;

        // --- Initialize animations ---
        double frameDuration = 0.15;

        walkLeftAnimation = new Animation(frameDuration, true, Sprite.balloom_left1, Sprite.balloom_left2, Sprite.balloom_left3);
        walkRightAnimation = new Animation(frameDuration, true, Sprite.balloom_right1, Sprite.balloom_right2, Sprite.balloom_right3);
        walkUpAnimation = new Animation(frameDuration, true, Sprite.balloom_left1, Sprite.balloom_left2, Sprite.balloom_left3); // Tạm thời dùng left
        walkDownAnimation = new Animation(frameDuration, true, Sprite.balloom_right1, Sprite.balloom_right2, Sprite.balloom_right3); // Tạm thời dùng right
        deadAnimation = new Animation(frameDuration, false, Sprite.balloom_dead); // Animation chết, không lặp lại
        currentAnimation = walkLeftAnimation; // Animation mặc định
    }

    // --- Enemy AI (Simple Random Movement) ---
    public void update(double deltaTime) {

        // TODO: Xử lý chết!

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


    public void render(GraphicsContext gc) {
        Image currentImage = null;
        if (currentAnimation != null) {
            Sprite currentSpriteFrame = currentAnimation.getFrame(animationTimer);
            if (currentSpriteFrame != null) {
                currentImage = currentSpriteFrame.getFxImage();
            }
        }

        if (currentImage == null) {
            currentImage = Sprite.balloom_left1.getFxImage(); // Default
        }

        gc.drawImage(currentImage, this.pixelX, this.pixelY);
    }

    public void kill() {
        if (isAlive) {
            isAlive = false;
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
}