package com.example.bomberman.entities;

import com.example.bomberman.Map.Map;
import com.example.bomberman.Map.Tile;
import com.example.bomberman.Map.TileType;
import com.example.bomberman.graphics.Sprite;
import com.example.bomberman.graphics.Animation; // Import lớp Animation
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.HashSet;
import java.util.Set;
import com.example.bomberman.Bomberman; // Import lớp Bomberman (hoặc GameStateManager)

public class Player {

    private double pixelX;
    private double pixelY;

    private int gridX;
    private int gridY;

    private double speed = 100.0; // Tốc độ di chuyển (pixel / giây)
    private Direction currentDirection = Direction.NONE;
    private boolean isMoving = false;

    private Map map;

    // --- Tham chiếu đến lớp quản lý game (Bomberman hoặc GameStateManager) ---
    private Bomberman gameManager; // Giữ tham chiếu đến Bomberman

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
    private double animationTimer = 0;
    private Direction lastNonNoneDirection = Direction.DOWN;

    // --- Bomb attributes ---
    public int bombNumber = 1; // Số lượng bom có thể đặt cùng lúc
    private int flameLength = 1; // Bán kính nổ của bom
    // TODO: Other state attributes

    // --- CONSTRUCTOR ĐÃ SỬA ĐỔI: Nhận thêm tham chiếu đến Bomberman ---
    public Player(int startGridX, int startGridY, Map map, Bomberman gameManager) {
        this.gridX = startGridX;
        this.gridY = startGridY;
        this.pixelX = startGridX * Sprite.SCALED_SIZE;
        this.pixelY = startGridY * Sprite.SCALED_SIZE;

        this.map = map;
        this.gameManager = gameManager; // Lưu tham chiếu

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

    // Phương thức được gọi bởi InputHandler khi nhấn phím đặt bom
    public void placeBomb() {
        // TODO: Kiểm tra xem ô hiện tại đã có bom chưa (tránh đặt chồng lên bom khác)
        // Cần truy cập danh sách bom từ gameManager để kiểm tra
        boolean isBombAtCurrentLocation = false;
        // if (gameManager != null && gameManager.isBombAtGrid(gridX, gridY)) { // Cần thêm phương thức isBombAtGrid vào Bomberman
        //    isBombAtCurrentLocation = true;
        // }


        // Kiểm tra xem có đủ số bom cho phép không VÀ ô hiện tại chưa có bom
        if (bombNumber > 0 && !isBombAtCurrentLocation) {
            // Xác định vị trí lưới chính xác để đặt bom (làm tròn vị trí pixel)
            int bombGridX = (int) Math.round(pixelX / Sprite.SCALED_SIZE);
            int bombGridY = (int) Math.round(pixelY / Sprite.SCALED_SIZE);

            // Tạo một đối tượng Bomb mới
            // Truyền 'this' (tham chiếu đến Player này) làm owner
            Bomb newBomb = new Bomb(bombGridX, bombGridY, flameLength, this, map);

            // --- Thêm bom vào danh sách quản lý bom của game ---
            // Gọi phương thức addBomb của Bomberman (gameManager)
            if (gameManager != null) { // Kiểm tra gameManager không null
                gameManager.addBomb(newBomb);
            } else {
                System.err.println("Error: gameManager is null in Player!");
            }


            // Giảm số lượng bom mà Player có thể đặt
            bombNumber--;

            System.out.println("Player placed a bomb at (" + bombGridX + ", " + bombGridY + "). Bombs left: " + bombNumber); // Log
            // TODO: Phát âm thanh đặt bom
        } else {
            // System.out.println("Cannot place bomb. Bombs left: " + bombNumber + ", Bomb at location: " + isBombAtCurrentLocation); // Debug log nếu không đặt được
        }
    }

    // --- Called by Game Loop (AnimationTimer) ---
    public void update(double deltaTime) {
        // TODO: Check state (e.g., if isDead, don't update movement)

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
            if (currentAnimation != null) {
                animationTimer += deltaTime;
            }
        }
        // TODO: Other update logic (collect items, take damage, death, invincibility...)
    }

    // Phương thức kiểm tra va chạm tại một vị trí pixel cụ thể (x, y)
    // Trả về true nếu CÓ va chạm, false nếu KHÔNG va chạm (có thể đi qua)
    private boolean checkCollision(double checkPixelX, double checkPixelY) {
        double playerSize = Sprite.SCALED_SIZE;
        // --- TINH CHỈNH GIÁ TRỊ BUFFER NÀY ---
        // Thử giảm giá trị này nếu nhân vật bị trễ khi bắt đầu di chuyển
        double buffer = 6.0; // Thử 2.0, 1.0, 0.5, 0.1

        // Tính toán tọa độ pixel của 4 góc hộp va chạm Player tại vị trí checkPixelX, checkPixelY
        // Các điểm này cần nằm bên trong hộp va chạm Player
        double topLeftX = checkPixelX + buffer;
        double topLeftY = checkPixelY + buffer;
        double topRightX = checkPixelX + playerSize - buffer;
        double topRightY = checkPixelY + buffer;
        double bottomLeftX = checkPixelX + buffer;
        double bottomLeftY = checkPixelY + playerSize - buffer;
        double bottomRightX = checkPixelX + playerSize - buffer;
        double bottomRightY = checkPixelY + playerSize - buffer;

        // Chuyển đổi các tọa độ pixel của các góc sang tọa độ lưới
        // Sử dụng Math.floor để đảm bảo chúng ta kiểm tra ô mà góc đó nằm trong
        int gridX1 = (int) Math.floor(topLeftX / playerSize);
        int gridY1 = (int) Math.floor(topLeftY / playerSize);

        int gridX2 = (int) Math.floor(topRightX / playerSize);
        int gridY2 = (int) Math.floor(topRightY / playerSize);

        int gridX3 = (int) Math.floor(bottomLeftX / playerSize);
        int gridY3 = (int) Math.floor(bottomLeftY / playerSize);

        int gridX4 = (int) Math.floor(bottomRightX / playerSize);
        int gridY4 = (int) Math.floor(bottomRightY / playerSize);

        // Tập hợp các ô lưới cần kiểm tra (loại bỏ các ô trùng lặp)
        Set<String> tilesToCheck = new HashSet<>();
        tilesToCheck.add(gridX1 + "," + gridY1);
        tilesToCheck.add(gridX2 + "," + gridY2);
        tilesToCheck.add(gridX3 + "," + gridY3);
        tilesToCheck.add(gridX4 + "," + gridY4);

        // Kiểm tra từng ô lưới trong danh sách
        for (String tileCoord : tilesToCheck) {
            String[] coords = tileCoord.split(",");
            int checkGridX = Integer.parseInt(coords[0]);
            int checkGridY = Integer.parseInt(coords[1]);

            // Lấy Tile từ Map, kiểm tra biên
            Tile tile = map.getTile(checkGridX, checkGridY);

            // Nếu ô Tile tồn tại và KHÔNG thể đi qua được, thì CÓ va chạm
            if (tile != null && !tile.isWalkable()) {
                // TODO: Handle collision with special Tile types (e.g., Portal)
                if (tile.getType() == TileType.PORTAL) {
                    // Logic to check if conditions are met to enter Portal
                    // If not met, treat as not walkable
                    // return true;
                    // If conditions ARE met, treat as walkable
                    // continue;
                } else {
                    // Collision with Wall or Brick
                    return true; // Collision detected
                }
            }
        }

        // If all checked tiles are walkable, no collision detected
        return false; // No collision
    }

    // --- Phương thức mới để tăng số bom sau khi bom nổ ---
    // Phương thức này sẽ được gọi từ Bomberman khi bom nổ
    public void increaseBombCount() {
        bombNumber++;
        System.out.println("Bomb count increased. Current bombs: " + bombNumber); // Log
    }


    // Phương thức được gọi bởi Vòng lặp Game để vẽ Player
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
            // Vẽ hình ảnh Player tại vị trí pixel hiện tại
            // Cần điều chỉnh vị trí vẽ nếu sprite có kích thước khác Sprite.SCALED_SIZE
            // hoặc nếu bạn muốn căn giữa sprite.
            // Hiện tại đang vẽ góc trên bên trái tại pixelX, pixelY
            gc.drawImage(currentImage, pixelX, pixelY);
        }

        // TODO: Logic vẽ các hiệu ứng khác của Player (ví dụ: khi chết)
    }

    // --- Getters ---
    public double getPixelX() { return pixelX; }
    public double getPixelY() { return pixelY; }
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public Direction getCurrentDirection() { return currentDirection; }
    public boolean isMoving() { return isMoving; }
    // TODO: Getters for other attributes
}
