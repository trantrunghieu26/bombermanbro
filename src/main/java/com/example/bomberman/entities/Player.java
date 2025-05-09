package com.example.bomberman.entities;

import com.example.bomberman.Bomberman;
import com.example.bomberman.Controller;
import com.example.bomberman.Map.Map;
import com.example.bomberman.Map.Tile;
import com.example.bomberman.Map.TileType;
import com.example.bomberman.entities.Items.Item;
import com.example.bomberman.graphics.Sprite;
import com.example.bomberman.graphics.Animation; // Import lớp Animation
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Player extends Entity {

    private static final int MAX_ALLOWED_BOMBS = 8;   // Ví dụ: Tối đa 8 quả bom
    private static final int MAX_ALLOWED_FLAMES = 8; // Ví dụ: Tối đa lửa dài 8 ô
    private static final double MAX_ALLOWED_SPEED = 250.0; // Ví dụ: Tối đa tốc độ 250

    private double speed = 100.0; // Tốc độ di chuyển (pixel / giây)
    private Direction currentDirection = Direction.NONE;

    private boolean isMoving = false;
    private boolean isAlive = true;
    private boolean isInvincible = false;
    private double invincibilityTimer = 0;
    private double invincibilityDuration = 2.0; // Thời gian bất tử sau khi trúng đòn (giây)

    private List<Bomb> bombs;
    // --- Thuộc tính Powerup nâng cao ---
    // TODO: private boolean canPassBrick = false; // Có thể đi xuyên gạch (sau khi đặt bom)
    public boolean canKickBomb = false; // Có thể đá Bom đã đặt (powerupBombpass/kickbomb)
    public Bomb kickableBombPending = null; // Lưu tham chiếu đến quả Bom sắp bị đá (Public để Bomberman truy cập)
    public Direction kickDirectionPending = Direction.NONE; // Lưu hướng đá pending (Public để Bomberman truy cập)

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

    private int maxBombs = 1; // Số lượng bom tối đa CÓ THỂ đặt cùng lúc. CÓ THỂ ĐƯỢC TĂNG BỞI ITEM.
    public int bombNumber = 1;
    private int flameLength = 1;
    private int lives = 3; // Số mạng của Player (cho vật phẩm LifeItem). CÓ THỂ ĐƯỢC TĂNG BỞI ITEM.
    private double deathAnimationDuration = 1.0; // Thời gian hiển thị animation chết (giây) // TODO: Điều chỉnh


    // Constructor
    public Player(int startGridX, int startGridY, Map map, Controller con) {
        super(startGridX, startGridY, map);
        this.con = con;
        bombs = new ArrayList<>();
        this.maxBombs = 1;
        this.bombNumber = this.maxBombs;
        this.flameLength = 1;
        this.lives = 3;

        // --- Khởi tạo trạng thái ban đầu ---
        this.isAlive = true;
        this.isMoving = false;
        this.currentDirection = Direction.NONE;
        this.canKickBomb = false;

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
        // Chỉ cho phép đổi hướng và di chuyển nếu Player còn sống
        if (!isAlive) {
            this.currentDirection = Direction.NONE;
            this.isMoving = false;
            return;
        }

        this.currentDirection = direction;
        this.isMoving = (direction != Direction.NONE);

        if (isMoving) {
            // Chọn animation di chuyển và cập nhật hướng cuối cùng (để biết đứng yên hướng nào)
            switch (direction) {
                case UP: currentAnimation = walkUpAnimation; lastNonNoneDirection = Direction.UP; break;
                case DOWN: currentAnimation = walkDownAnimation; lastNonNoneDirection = Direction.DOWN; break;
                case LEFT: currentAnimation = walkLeftAnimation; lastNonNoneDirection = Direction.LEFT; break;
                case RIGHT: currentAnimation = walkRightAnimation; lastNonNoneDirection = Direction.RIGHT; break;
            }
            // animationTimer = 0; // Không reset timer khi bắt đầu di chuyển, để animation chạy mượt
        } else {
            // Chọn animation đứng yên tương ứng với hướng cuối cùng và reset timer
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
                canMoveX = !this.checkCollision(nextPixelX, pixelY); // Kiểm tra va chạm chỉ với vị trí X mới
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
                canMoveY = !this.checkCollision(tempPixelX, nextPixelY); // Kiểm tra va chạm chỉ với vị trí Y mới
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
    }

    public boolean collidesWith(Item item) {

        // Kiểm tra xem Item có null hoặc không active không
        if (item == null || !item.isActive()) {
            return false;
        }

        double playerLeft = this.pixelX;
        double playerRight = this.pixelX + Sprite.SCALED_SIZE;
        double playerTop = this.pixelY;
        double playerBottom = this.pixelY + Sprite.SCALED_SIZE;

        double itemLeft = item.getPixelX();
        double itemRight = item.getPixelX() + Sprite.SCALED_SIZE;
        double itemTop = item.getPixelY();
        double itemBottom = item.getPixelY() + Sprite.SCALED_SIZE;

        return playerRight > itemLeft && playerLeft < itemRight &&
                playerBottom > itemTop && playerTop < itemBottom;
    }

    public boolean collidesWith(Bomb bomb) {
        // TODO: Có thể thêm kiểm tra trạng thái Player (ví dụ: isDead) nếu Player chết không va chạm với Bom
        // if (!this.isAlive()) return false;

        // Kiểm tra xem Bomb có null, không active hoặc đã nổ chưa
        // Thường chỉ va chạm với bom đang đếm ngược.
        if (bomb == null || !bomb.isRemoved() || bomb.isExploded()) { // isExploded() là getter trong Bomb
            return false;
        }

        // --- Logic kiểm tra va chạm dạng hình hộp (Axis-Aligned Bounding Box - AABB) ---
        // So sánh vị trí và kích thước của hộp va chạm Player và Bomb.
        // Giả định cả hai đều có kích thước va chạm bằng Sprite.SCALED_SIZE.

        double playerLeft = this.pixelX;
        double playerRight = this.pixelX + Sprite.SCALED_SIZE;
        double playerTop = this.pixelY;
        double playerBottom = this.pixelY + Sprite.SCALED_SIZE;

        double bombLeft = bomb.getPixelX();
        double bombRight = bomb.getPixelX() + Sprite.SCALED_SIZE;
        double bombTop = bomb.getPixelY();
        double bombBottom = bomb.getPixelY() + Sprite.SCALED_SIZE;

        // Kiểm tra chồng lấn giữa hai hình chữ nhật
        return playerRight > bombLeft && playerLeft < bombRight &&
                playerBottom > bombTop && playerTop < bombBottom;
    }

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
                    gc.drawImage(currentImage, pixelX, pixelY+ Bomberman.UI_PANEL_HEIGHT, Sprite.SCALED_SIZE, Sprite.SCALED_SIZE);
                }
            } else {
                // Vẽ bình thường khi không bất tử
                gc.drawImage(currentImage, pixelX, pixelY+Bomberman.UI_PANEL_HEIGHT, Sprite.SCALED_SIZE, Sprite.SCALED_SIZE);
            }
        }
    }

    public void requestPlayerPlaceBomb() {
        if (!isAlive) {
            return;
        }
        if (map == null) {
            return;
        }
        int playerGridX = this.getGridX();
        int playerGridY = this.getGridY();

        if (this.getBombNumber() <= 0) {
            System.out.println("Cannot place bomb: No bombs left!");
            return;
        } else {
            boolean bombAlreadyExists = false;
            for (List<Bomb> theBom : con.getBto()) {
                for (Bomb existingBomb : theBom) {
                    if (existingBomb.getGridX() == playerGridX && existingBomb.getGridY() == playerGridY) {
                        bombAlreadyExists = true;
                        break;
                    }
                }
            }

            Tile currentTile = map.getTile(playerGridX, playerGridY);
            boolean canPlaceOnTile = (currentTile != null && currentTile.isWalkable()); // Có thể đặt bom trên ô đi được (trống, cửa)

            // TODO: Cần kiểm tra nếu trên ô đó có vật thể khác không đi qua được khi bomb chưa nổ (vd: quái vật, item?)
            // Hiện tại Player có thể đặt bom dưới chân mình và quái vật cũng có thể đi qua ô có bomb chưa nổ.

            if (canPlaceOnTile && !bombAlreadyExists) {
                // Truyền tham chiếu đến Bomberman (this) vào constructor của Bomb
                Bomb newBomb = new Bomb(playerGridX, playerGridY, this.getFlameLength(), this);
                newBomb.setTimer(2.0);
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
    public int getLives() { return lives; } // Getter cho số mạng
    public void decreaseLive() { this.lives -= 1; }
    public int getBombNumber() { return bombNumber; }
    public int getMaxBombs() { return maxBombs; } // Getter cho số bom tối đa
    public int getFlameLength() { return flameLength; }
    public double getSpeed() { return speed; } // Getter cho tốc độ
    public List<Bomb> getBombs() {
        return bombs;
    }
    public boolean isAlive() { return isAlive; }
    public boolean isInvincible() { return isInvincible; }
    public Controller getController() { return this.con; }
    public void decreaseBombNumber(int amount) { this.bombNumber -= amount; if (this.bombNumber < 0) this.bombNumber = 0; }
    public Animation getCurrentAnimation() { return this.currentAnimation; }

    // Phương thức được gọi bởi Bomb khi nổ để Player có thể đặt thêm bom
    public void increaseBombNumber() {
        bombNumber = Math.min(bombNumber + 1, maxBombs);
        System.out.println("Bomb count increased after explosion. Current bombs: " + bombNumber + " (Max: " + maxBombs + ")"); // Log
    }

    // --- Phương thức áp dụng hiệu ứng từ Item BombItem ---
    // Được gọi từ BombItem.applyEffect()
    public void increaseMaxBombs() {
        if(maxBombs<MAX_ALLOWED_BOMBS){
            maxBombs++;
            bombNumber++; System.out.println("Collected BombItem! New Max Bombs: " + maxBombs + ", Current Bombs: " + bombNumber);
            // TODO: Có thể phát âm thanh power-up thành công
        } else {
            System.out.println("Max bomb limit reached (" + MAX_ALLOWED_BOMBS + ").");
            // TODO: Có thể phát âm thanh báo đã max hoặc cộng điểm thay thế
            con.addScore(50); // Ví dụ: cộng điểm nếu đã max
        }
    }

    // --- Phương thức áp dụng hiệu ứng từ Item FlameItem ---
    // Được gọi từ FlameItem.applyEffect()
    public void increaseFlameLength() {
        if(flameLength<MAX_ALLOWED_FLAMES){
            flameLength++;
            for (List<Bomb> theBomb : this.getController().getBto()) {
                for (Bomb bomb : theBomb) {
                    bomb.setFlameLength(1);
                }
            }
            System.out.println("Collected FlameItem! New Flame Length: " + flameLength);
        }
        else {
            System.out.println("Max flame length reached (" + MAX_ALLOWED_FLAMES + ").");
            // TODO: Âm thanh báo max / Cộng điểm
            con.addScore(50);
        }

    }

    // --- Phương thức áp dụng hiệu ứng từ Item SpeedItem ---
    // Được gọi từ SpeedItem.applyEffect()
    public void increaseSpeed() {
        if(speed<MAX_ALLOWED_SPEED){
            speed+=50;
            speed = Math.min(speed, MAX_ALLOWED_SPEED);
            System.out.println("Collected SpeedItem! New Speed: " + speed); // Log
            // TODO: Âm thanh power-up
        }
        else{
            System.out.println("Max speed reached (" + MAX_ALLOWED_SPEED + ").");
            // TODO: Âm thanh báo max / Cộng điểm
            con.addScore(50);

        }

        // TODO: Giới hạn tốc độ tối đa tuyệt đối (ví dụ: speed không quá 250)
    }

    // --- Phương thức áp dụng hiệu ứng từ Item LifeItem ---
    // Được gọi từ LifeItem.applyEffect()
    public void increaseLives() {
        lives++; // Tăng số mạng
        System.out.println("Collected LifeItem! New Lives: " + lives); // Log
        // TODO: Giới hạn số mạng tối đa tuyệt đối (ví dụ: lives không quá 9)
    }

    // --- Phương thức áp dụng hiệu ứng từ Item KickBombItem ---
    // Được gọi từ KickBombItem.applyEffect()
    public void enableKickBomb() {
        this.canKickBomb = true; // Bật khả năng Đá Bom cho Player
        System.out.println("Kick Bomb ability enabled for Player."); // Log
        // TODO: Thêm hiệu ứng hình ảnh cho Player khi có khả năng Đá Bom (tùy chọn)
    }

    // Phương thức xử lý khi Player trở nên bất tử
    public void becomeInvincible(double duration) {
        isInvincible = true;
        invincibilityTimer = duration;
        System.out.println("Player is now invincible for " + duration + " seconds."); // Log
    }

    @Override
    public boolean checkCollision(double checkPixelX, double checkPixelY) {
        this.kickableBombPending = null;
        this.kickDirectionPending = Direction.NONE;

        double playerSize = Sprite.SCALED_SIZE;
        double buffer = 6.0;

        // --- 1. Kiểm tra va chạm với các Tile làm vật cản (Wall, Brick) ---
        double topLeftX_tile = checkPixelX + buffer;
        double topLeftY_tile = checkPixelY + buffer;
        double topRightX_tile = checkPixelX + playerSize - buffer;
        double topRightY_tile = checkPixelY + buffer;
        double bottomLeftX_tile = checkPixelX + buffer;
        double bottomLeftY_tile = checkPixelY + playerSize - buffer;
        double bottomRightX_tile = checkPixelX + playerSize - buffer;
        double bottomRightY_tile = checkPixelY + playerSize - buffer;

        Set<String> tilesToCheck = new HashSet<>();
        tilesToCheck.add(((int) Math.floor(topLeftX_tile / playerSize)) + "," + ((int) Math.floor(topLeftY_tile / playerSize)));
        tilesToCheck.add(((int) Math.floor(topRightX_tile / playerSize)) + "," + ((int) Math.floor(topRightY_tile / playerSize)));
        tilesToCheck.add(((int) Math.floor(bottomLeftX_tile / playerSize)) + "," + ((int) Math.floor(bottomLeftY_tile / playerSize)));
        tilesToCheck.add(((int) Math.floor(bottomRightX_tile / playerSize)) + "," + ((int) Math.floor(bottomRightY_tile / playerSize)));


        // Kiểm tra từng ô lưới trong danh sách
        for (String tileCoord : tilesToCheck) {
            String[] coords = tileCoord.split(",");
            int checkGridX = Integer.parseInt(coords[0]);
            int checkGridY = Integer.parseInt(coords[1]);

            Tile tile = null;
            // Cần đảm bảo map không null và tọa độ hợp lệ khi lấy Tile
            if (map != null && checkGridX >= 0 && checkGridX < map.getCols() && checkGridY >= 0 && checkGridY < map.getRows()) {
                tile = map.getTile(checkGridX, checkGridY);
            } else {
                // Nếu điểm kiểm tra nằm ngoài biên bản đồ, coi như va chạm với rìa map
                return true; // Có va chạm chặn với biên map
            }

            if (tile != null) {
                // Kiểm tra va chạm với Wall hoặc Brick (chúng luôn chặn di chuyển)
                if (tile.getType() == TileType.WALL || tile.getType() == TileType.BRICK) { // TODO: Xử lý canPassBrick sau này (nếu canPassBrick, không return true ở đây)
                    // System.out.println("Player blocked by Tile at (" + checkGridX + "," + checkGridY + ") at checked position."); // Log va chạm Tile (có thể quá nhiều)
                    return true; // Va chạm với vật cản (Tile) -> CHẶN
                }
                // TODO: Xử lý va chạm với Portal sau này (nó chỉ chặn nếu chưa đủ điều kiện)
                // else if (tile.getType() == TileType.PORTAL) { ... if(isBlocking) return true; }
            }
        }

        // Trong lớp Player.java, trong phương thức checkCollision(...)
// Bên trong vòng lặp kiểm tra từng quả Bom 'bomb'

// --- 2. Kiểm tra va chạm với các Bom làm vật cản ---
// Chỉ kiểm tra nếu danh sách bombs được cung cấp (không null)
        if (con.getBto() != null) {
            for (List<Bomb> theBomb : con.getBto()) {
                for (Bomb bomb : theBomb) {
                    if (!bomb.isRemoved() && !bomb.isExploded() && bomb.getKick()) {
                            // Nếu đây là Bom của chính Player, kiểm tra xem Player có đang nằm trong phạm vi pixel của Bom đó không.
                            // Sử dụng tọa độ pixel HIỆN TẠI của Player (this.pixelX, this.pixelY)
                            double playerCurrentLeft = this.pixelX;
                            double playerCurrentRight = this.pixelX + Sprite.SCALED_SIZE;
                            double playerCurrentTop = this.pixelY;
                            double playerCurrentBottom = this.pixelY + Sprite.SCALED_SIZE;

                            double bombLeft = bomb.getPixelX();
                            double bombRight = bomb.getPixelX() + Sprite.SCALED_SIZE;
                            double bombTop = bomb.getPixelY();
                            double bombBottom = bomb.getPixelY() + Sprite.SCALED_SIZE;

                            // Kiểm tra xem hộp va chạm HIỆN TẠI của Player có chồng lấn với hộp va chạm của Bom không
                            // Chúng ta cần một sự chồng lấn đáng kể để coi là "đang đứng trên Bom"
                            // Kiểm tra đơn giản: Player's current bounding box overlaps with Bomb's bounding box
                            boolean playerCurrentlyOverlapsBomb = playerCurrentRight > bombLeft && playerCurrentLeft < bombRight &&
                                    playerCurrentBottom > bombTop && playerCurrentTop < bombBottom;

                            // Alternative: Kiểm tra xem Player có đang ở trên cùng ô lưới với Bom không (làm tròn vị trí pixel hiện tại)
                            // boolean playerOnSameGridAsBomb = (Math.round(this.pixelX / Sprite.SCALED_SIZE) == bomb.getGridX() && Math.round(this.pixelY / Sprite.SCALED_SIZE) == bomb.getGridY());

                            // Nếu Bom này là của Player VÀ Player đang nằm trong phạm vi pixel của Bom đó (tức là chồng lấn pixel)
                            if (playerCurrentlyOverlapsBomb) {
                                continue; // Bỏ qua phần còn lại của vòng lặp for với Bom này
                            }

                            double checkedPlayerLeft = checkPixelX;
                            double checkedPlayerRight = checkPixelX + Sprite.SCALED_SIZE;
                            double checkedPlayerTop = checkPixelY;
                            double checkedPlayerBottom = checkPixelY + Sprite.SCALED_SIZE;

                            double bombLefts = bomb.getPixelX();
                            double bombRights = bomb.getPixelX() + Sprite.SCALED_SIZE;
                            double bombTops = bomb.getPixelY();
                            double bombBottoms = bomb.getPixelY() + Sprite.SCALED_SIZE;

                            // Logic kiểm tra chồng lấn giữa hai hình hộp tại vị trí kiểm tra
                            boolean overlapAtCheckedPosition = checkedPlayerRight > bombLefts && checkedPlayerLeft < bombRights &&
                                    checkedPlayerBottom > bombTops && checkedPlayerTop < bombBottoms;


                            if (overlapAtCheckedPosition) {
                                // Va chạm chồng lấn với một quả Bom được phát hiện tại vị trí kiểm tra.
                                // Đây có phải là va chạm làm vật cản (nghĩa là Player bị chặn lại) không?
                                // Bom làm vật cản NẾU Player KHÔNG có khả năng Đá Bom HOẶC Bom ĐANG BỊ ĐÁ.
                                // Nếu Player có khả năng Đá Bom VÀ Bom CHƯA BỊ ĐÁ, va chạm này KHÔNG làm vật cản di chuyển.
                                // Logic kích hoạt đá Bom sẽ được xử lý ở Bomberman.handle, không ở đây.
                                // Tại đây, chúng ta CHỈ TRẢ VỀ true nếu nó LÀ VẬT CẢN CẦN CHẶN DI CHUYỂN.
                                if (!this.canKickBomb || bomb.isKicked()) { // canKickBomb là thuộc tính Player, isKicked() là getter của Bomb
                                    // System.out.println("Player blocked by Bomb at (" + bomb.getGridX() + "," + bomb.getGridY() + ") at checked position. (Cannot kick or Bomb is kicking)"); // Log va chạm Bom chặn (có thể quá nhiều)
                                    return true; // Va chạm với Bom làm vật cản -> CHẶN
                                }
                                else{
                                    System.out.println("DEBUG checkCollision: Potential kick detected! Setting pending state.");
                                    this.kickableBombPending = bomb;
                                    this.kickDirectionPending = this.currentDirection;
                                }
                                // Nếu Player CÓ khả năng đá và Bom CHƯA bị đá, thì va chạm này KHÔNG chặn di chuyển (return false sẽ xảy ra sau vòng lặp)
                                // TODO: Xử lý Player va chạm với Bom ĐANG BỊ ĐÁ (Player có thể bị đẩy?) sau này. Hiện tại nó sẽ chặn di chuyển.
                            }
                    }
                }
            }
        }
        return false; // Vị trí kiểm tra này có thể di chuyển đến -> KHÔNG CHẶN
    }

    public double getLastPixelX(double deltaTime) {
        double deltaPixelX = 0;

        switch (currentDirection) {
            case LEFT: deltaPixelX = -speed * deltaTime; break;
            case RIGHT: deltaPixelX = speed * deltaTime; break;
            case NONE: deltaPixelX = 0.0;
        }

        return pixelX - deltaPixelX;
    }

    public double getLastPixelY(double deltaTime) {
        double deltaPixelY = 0;

        switch (currentDirection) {
            case UP: deltaPixelY = -speed * deltaTime; break;
            case DOWN: deltaPixelY = speed * deltaTime; break;
            case NONE: deltaPixelY = 0.0;
        }

        return pixelY - deltaPixelY;
    }
}
