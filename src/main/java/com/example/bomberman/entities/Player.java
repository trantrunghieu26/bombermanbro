package com.example.bomberman.entities;

import com.example.bomberman.Map.Map;
import com.example.bomberman.Map.Tile;
import com.example.bomberman.Map.TileType;
import com.example.bomberman.graphics.Sprite;
import com.example.bomberman.graphics.Animation;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import com.example.bomberman.entities.Items.Item; // IMPORT LỚP ITEM
import com.example.bomberman.entities.Bomb; // IMPORT LỚP BOMB
// TODO: IMPORT LỚP ENEMY khi bạn triển khai nó
// import com.example.bomberman.entities.Enemy;


import java.util.HashSet;
import java.util.List; // IMPORT LỚP LIST
import java.util.Set;
import com.example.bomberman.Bomberman;

// Lớp đại diện cho người chơi Bomberman
public class Player {
    private static final int MAX_ALLOWED_BOMBS = 8;   // Ví dụ: Tối đa 8 quả bom
    private static final int MAX_ALLOWED_FLAMES = 8; // Ví dụ: Tối đa lửa dài 8 ô
    private static final double MAX_ALLOWED_SPEED = 250.0; // Ví dụ: Tối đa tốc độ 250

    // --- Thuộc tính vị trí và di chuyển ---
    private double pixelX; // Vị trí pixel theo trục X
    private double pixelY; // Vị trí pixel theo trục Y
    private int gridX; // Vị trí lưới theo trục X (cột)
    private int gridY; // Vị trí lưới theo trục Y (hàng)
    private double speed = 100.0; // Tốc độ di chuyển (pixel / giây). CÓ THỂ ĐƯỢC TĂNG BỞI ITEM.
    private Direction currentDirection = Direction.NONE; // Hướng di chuyển hiện tại
    private boolean isMoving = false; // Cờ cho biết đang di chuyển hay đứng yên

    // --- Thuộc tính game play (liên quan đến Item và Bomb) ---
    private int maxBombs = 1; // Số lượng bom tối đa CÓ THỂ đặt cùng lúc. CÓ THỂ ĐƯỢC TĂNG BỞI ITEM.
    private int currentBombs; // Số lượng bom HIỆN TẠI Player có thể đặt (giảm khi đặt, tăng khi bom nổ).
    private int flameLength = 1; // Độ dài ngọn lửa khi bom nổ. CÓ THỂ ĐƯỢC TĂNG BỞI ITEM.
    private int lives = 3; // Số mạng của Player (cho vật phẩm LifeItem). CÓ THỂ ĐƯỢC TĂNG BỞI ITEM.

    // --- Thuộc tính Powerup nâng cao ---
    // TODO: private boolean canPassBrick = false; // Có thể đi xuyên gạch (sau khi đặt bom)
    public boolean canKickBomb = false; // Có thể đá Bom đã đặt (powerupBombpass/kickbomb)
    public Bomb kickableBombPending = null; // Lưu tham chiếu đến quả Bom sắp bị đá (Public để Bomberman truy cập)
    public Direction kickDirectionPending = Direction.NONE; // Lưu hướng đá pending (Public để Bomberman truy cập)


    // --- Trạng thái cơ bản ---
    private boolean isAlive = true; // Cờ cho biết Player còn sống hay không
    // TODO: private double deathTimer = 0; // Bộ đếm thời gian cho animation chết


    // --- Tham chiếu đến Map và GameManager ---
    private Map map; // Tham chiếu đến đối tượng Map
    private Bomberman gameManager; // Tham chiếu đến lớp quản lý game chính (Bomberman)


    // --- Animation attributes ---
    // Các animation cho từng hướng di chuyển và đứng yên
    private Animation walkUpAnimation;
    private Animation walkDownAnimation;
    private Animation walkLeftAnimation;
    private Animation walkRightAnimation;
    private Animation idleUpAnimation;
    private Animation idleDownAnimation;
    private Animation idleLeftAnimation;
    private Animation idleRightAnimation;
    // TODO: Animation chết
    private Animation currentAnimation; // Animation đang chạy hiện tại
    private double animationTimer = 0; // Bộ đếm thời gian cho animation
    private Direction lastNonNoneDirection = Direction.DOWN; // Hướng cuối cùng không phải NONE (để biết đứng yên hướng nào)


    // --- Constructor ---
    public Player(int startGridX, int startGridY, Map map, Bomberman gameManager) {
        // Tính toán vị trí pixel dựa trên vị trí lưới và kích thước Sprite đã scale
        this.gridX = startGridX;
        this.gridY = startGridY;
        this.pixelX = startGridX * Sprite.SCALED_SIZE;
        this.pixelY = startGridY * Sprite.SCALED_SIZE;

        this.map = map; // Gán tham chiếu Map
        this.gameManager = gameManager; // Gán tham chiếu GameManager

        // --- Khởi tạo các thuộc tính game play ban đầu ---
        this.maxBombs = 1;
        this.currentBombs = this.maxBombs; // Ban đầu số bom hiện có bằng số bom tối đa
        this.flameLength = 1;
        this.lives = 3;

        // --- Khởi tạo trạng thái ban đầu ---
        this.isAlive = true;
        this.isMoving = false;
        this.currentDirection = Direction.NONE;

        // --- Khởi tạo các thuộc tính Powerup ban đầu ---
        this.canKickBomb = false;
        // TODO: canPassBrick = false;
        // TODO: canPassFlame = false;
        // TODO: isInvincible = false;
        // TODO: invincibilityTimer = 0;


        // --- Khởi tạo các Animation ---
        double frameDuration = 0.15; // Thời gian hiển thị mỗi frame animation (có thể điều chỉnh)

        // Animation di chuyển
        walkUpAnimation = new Animation(frameDuration, true, Sprite.player_up, Sprite.player_up_1, Sprite.player_up_2, Sprite.player_up_1);
        walkDownAnimation = new Animation(frameDuration, true, Sprite.player_down, Sprite.player_down_1, Sprite.player_down_2, Sprite.player_down_1);
        walkLeftAnimation = new Animation(frameDuration, true, Sprite.player_left, Sprite.player_left_1, Sprite.player_left_2, Sprite.player_left_1);
        walkRightAnimation = new Animation(frameDuration, true, Sprite.player_right, Sprite.player_right_1, Sprite.player_right_2, Sprite.player_right_1);

        // Animation đứng yên
        idleUpAnimation = new Animation(frameDuration, true, Sprite.player_up);
        idleDownAnimation = new Animation(frameDuration, true, Sprite.player_down);
        idleLeftAnimation = new Animation(frameDuration, true, Sprite.player_left);
        idleRightAnimation = new Animation(frameDuration, true, Sprite.player_right);

        // Animation ban đầu khi game bắt đầu
        currentAnimation = idleDownAnimation;

        // TODO: Khởi tạo animation chết nếu có
    }


    // --- Phương thức được gọi bởi InputHandler để đặt hướng di chuyển ---
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
                case NONE: break; // Trường hợp này isMoving sẽ là false
            }
            // animationTimer = 0; // Không reset timer khi bắt đầu di chuyển, để animation chạy mượt
        } else {
            // Chọn animation đứng yên tương ứng với hướng cuối cùng và reset timer
            switch (lastNonNoneDirection) {
                case UP: currentAnimation = idleUpAnimation; break;
                case DOWN: currentAnimation = idleDownAnimation; break;
                case LEFT: currentAnimation = idleLeftAnimation; break;
                case RIGHT: currentAnimation = idleRightAnimation; break;
                default: currentAnimation = idleDownAnimation; break; // Mặc định đứng yên hướng xuống
            }
            animationTimer = 0; // Reset timer animation khi dừng di chuyển
        }
    }


    // Phương thức được gọi bởi InputHandler khi nhấn phím đặt bom
    public void placeBomb() {
        // Chỉ cho phép đặt bom nếu Player còn sống và không đang trong trạng thái không thể đặt bom
        if (!isAlive) {
            return;
        }
        int bombGridX = (int) Math.round(pixelX / Sprite.SCALED_SIZE);
        int bombGridY = (int) Math.round(pixelY / Sprite.SCALED_SIZE);

        // TODO: Kiểm tra xem ô hiện tại đã có bom chưa (tránh đặt chồng lên bom khác)

        // Cần thêm phương thức public boolean isBombAtGrid(int gridX, int gridY) vào Bomberman và sử dụng gameManager
        boolean isBombAtCurrentLocation = false;
        if (gameManager != null) {
             isBombAtCurrentLocation = gameManager.isBombAtGrid(gridX, gridY);
        }


        // Kiểm tra xem có đủ số bom cho phép (currentBombs > 0) và ô hiện tại chưa có bom
        if (currentBombs > 0 && !isBombAtCurrentLocation) {
            // Xác định vị trí lưới chính xác để đặt bom (làm tròn vị trí pixel của Playe

            // TODO: Kiểm tra xem vị trí đặt bom có hợp lệ không (ví dụ: không đặt trong tường cứng)
             Tile tileAtBombPos = map.getTile(bombGridX, bombGridY);
            if (map != null) { // Kiểm tra map không null
                tileAtBombPos = map.getTile(bombGridX, bombGridY);
            }
            if (tileAtBombPos != null && tileAtBombPos.getType() == TileType.WALL) {
                System.out.println("Cannot place bomb inside a solid wall.");
                // TODO: Âm thanh báo lỗi
                return;
            }


            // Tạo một đối tượng Bomb mới. Truyền gameMap để Bomb có thể kiểm tra va chạm Tile khi bị đá.
            Bomb newBomb = new Bomb(bombGridX, bombGridY, flameLength, this, map); // Truyền owner (this) và map

            // Thêm bom vào danh sách quản lý bom của game thông qua gameManager
            if (gameManager != null) {
                gameManager.addBomb(newBomb); // gameManager phải có phương thức addBomb(Bomb bomb)
                System.out.println("Player placed a bomb at (" + bombGridX + ", " + bombGridY + "). Current bombs left: " + (currentBombs - 1)); // Log
                // Giảm số lượng bom mà Player có thể đặt SAU KHI thêm thành công
                currentBombs--;
                // TODO: Phát âm thanh đặt bom
            } else {

            }

        } else if (isBombAtCurrentLocation) {
            System.out.println("Cannot place bomb: Another bomb is already at (" + bombGridX + ", " + bombGridY + ")");
            // TODO: Phát âm thanh báo lỗi (ví dụ: tiếng "tạch")
        } else { // Trường hợp currentBombs <= 0
            System.out.println("Cannot place bomb: No bombs available (Current: " + currentBombs + "/" + maxBombs + ")");
            // TODO: Phát âm thanh báo lỗi
        }


    }

    // Phương thức được gọi bởi Bomb khi nổ để Player có thể đặt thêm bom
    public void increaseBombCount() {
        // Tăng số bom hiện có, không vượt quá số bom tối đa (maxBombs)
        currentBombs = Math.min(currentBombs + 1, maxBombs);
        System.out.println("Bomb count increased after explosion. Current bombs: " + currentBombs + " (Max: " + maxBombs + ")"); // Log
    }


    // --- Phương thức áp dụng hiệu ứng từ Item BombItem ---
    // Được gọi từ BombItem.applyEffect()
    public void increaseMaxBombs() {
        if(maxBombs<MAX_ALLOWED_BOMBS){
            maxBombs++;
            currentBombs++; System.out.println("Collected BombItem! New Max Bombs: " + maxBombs + ", Current Bombs: " + currentBombs);
            // TODO: Có thể phát âm thanh power-up thành công
        } else {
            System.out.println("Max bomb limit reached (" + MAX_ALLOWED_BOMBS + ").");
            // TODO: Có thể phát âm thanh báo đã max hoặc cộng điểm thay thế
            gameManager.addScore(50); // Ví dụ: cộng điểm nếu đã max
        }
    }

    // --- Phương thức áp dụng hiệu ứng từ Item FlameItem ---
    // Được gọi từ FlameItem.applyEffect()
    public void increaseFlameLength() {
        if(flameLength<MAX_ALLOWED_FLAMES){
            flameLength++;
            System.out.println("Collected FlameItem! New Flame Length: " + flameLength);
        }
        else {
            System.out.println("Max flame length reached (" + MAX_ALLOWED_FLAMES + ").");
            // TODO: Âm thanh báo max / Cộng điểm
            gameManager.addScore(50);
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
              gameManager.addScore(50);

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

    // TODO: Thêm phương thức applyEffect cho các item khác khi triển khai (canPassBrick, canPassFlame, Invincibility)


    // --- Phương thức cập nhật trạng thái Player mỗi frame ---
    // Được gọi bởi Vòng lặp Game (Bomberman.handle)
    public void update(double deltaTime) {
        // TODO: Xử lý trạng thái chết, bất tử tạm thời ở đây
        // if (!isAlive) { /* Cập nhật animation chết */ /* Cập nhật deathTimer */ /* Kiểm tra hồi sinh */ return; }
        // if (isInvincible) { /* Cập nhật invincibilityTimer */ /* Tắt bất tử khi hết giờ */ }


        // LẤY DANH SÁCH BOM (VÀ ENEMY SAU NÀY) TỪ GAMEMANAGER ĐỂ KIỂM TRA VA CHẠM THỰC THỂ
        // Cần phương thức public List<Bomb> getBombs() trong lớp Bomberman (đã thêm ở bước trước)
        // TODO: Cần phương thức public List<Enemy> getEnemies() sau này
        List<com.example.bomberman.entities.Bomb> bombs = (gameManager != null) ? gameManager.getBombs() : null;
        // TODO: List<Object> enemies = (gameManager != null) ? gameManager.getEnemies() : null; // Tạm thời bỏ Enemy


        // --- Cập nhật vị trí dựa trên hướng di chuyển và tốc độ ---
        if (isMoving) {
            double deltaPixelX = 0;
            double deltaPixelY = 0;

            // Tính toán thay đổi vị trí pixel trong frame này
            switch (currentDirection) {
                case UP: deltaPixelY = -speed * deltaTime; break;
                case DOWN: deltaPixelY = speed * deltaTime; break;
                case LEFT: deltaPixelX = -speed * deltaTime; break;
                case RIGHT: deltaPixelX = speed * deltaTime; break;
                case NONE: break; // Trường hợp này isMoving sẽ là false
            }

            // --- Logic kiểm tra va chạm với Tile VÀ Blocking Entities (Bombs) ---
            // Kiểm tra riêng từng trục X, Y để xử lý va chạm và neo lại ở cạnh vật cản

            double nextPixelX = pixelX + deltaPixelX;
            double nextPixelY = pixelY + deltaPixelY;

            boolean canMoveX = true;
            boolean canMoveY = true;

            // Kiểm tra va chạm làm vật cản cho chuyển động theo trục X tại vị trí nextPixelX
            if (deltaPixelX != 0) {
                // GỌI PHƯƠNG THỨC checkCollision ĐÃ SỬA ĐỔI, TRUYỀN DANH SÁCH BOM (VÀ ENEMY)
                // CHỈ TRUYỀN DANH SÁCH BOM TẠM THỜI VÌ CHƯA CÓ ENEMY
                canMoveX = !checkCollision(nextPixelX, pixelY, bombs /* TODO: Thêm enemies vào đây sau */); // checkCollision giờ kiểm tra cả Tile và Bom chặn
            }

            // Cập nhật tạm thời pixelX (hoặc neo lại nếu va chạm X) để kiểm tra va chạm Y
            double tempPixelX = pixelX;
            if (canMoveX) {
                tempPixelX = nextPixelX;
            } else {
                // Neo lại ở cạnh vật cản (Tile hoặc Entity) khi va chạm theo X
                tempPixelX = (deltaPixelX > 0) ?
                        (int) Math.floor((pixelX + Sprite.SCALED_SIZE - 1) / Sprite.SCALED_SIZE) * Sprite.SCALED_SIZE :
                        (int) Math.ceil(pixelX / Sprite.SCALED_SIZE) * Sprite.SCALED_SIZE;
                // TODO: Tinh chỉnh logic neo này nếu va chạm với Entity có kích thước khác Tile.
            }

            // Kiểm tra va chạm làm vật cản cho chuyển động theo trục Y tại vị trí nextPixelY (sử dụng tempPixelX đã điều chỉnh)
            if (deltaPixelY != 0) {
                // GỌI PHƯƠNG THỨC checkCollision ĐÃ SỬA ĐỔI, TRUYỀN DANH SÁCH BOM (VÀ ENEMY)
                // CHỈ TRUYỀN DANH SÁCH BOM TẠM THỜI VÌ CHƯA CÓ ENEMY
                canMoveY = !checkCollision(tempPixelX, nextPixelY, bombs /* TODO: Thêm enemies vào đây sau */); // checkCollision giờ kiểm tra cả Tile và Bom chặn
            }

            // --- Cập nhật vị trí pixel cuối cùng nếu có thể di chuyển ---
            if (canMoveX) {
                pixelX = nextPixelX;
            }
            if (canMoveY) {
                pixelY = nextPixelY;
            }

            // Cập nhật vị trí lưới (gridX, gridY) dựa trên vị trí pixel mới
            this.gridX = (int) Math.round(pixelX / Sprite.SCALED_SIZE);
            this.gridY = (int) Math.round(pixelY / Sprite.SCALED_SIZE);

            // TODO: Kiểm tra va chạm với Portal sau khi cập nhật vị trí (Logic chuyển màn)
            // Logic chặn bởi Portal (nếu nó chặn) đã nằm trong checkCollision.
            // Logic chuyển màn khi đứng trên Portal và đủ điều kiện (hết quái vật) sẽ ở đây.
            // Tile currentTile = map.getTile(gridX, gridY);
            // if (currentTile != null && currentTile.getType() == TileType.PORTAL) {
            //     // if (gameManager != null && gameManager.allEnemiesDefeated()) { // Cần getEnemies() và logic đếm/kiểm tra trong Bomberman
            //     //    gameManager.levelCleared(); // Cần phương thức levelCleared() trong Bomberman
            //     // }
            // }

            // --- SỬA LỖI: CẬP NHẬT TIMER ANIMATION KHI ĐANG DI CHUYỂN ---
            if (currentAnimation != null) {
                animationTimer += deltaTime; // THÊM DÒNG NÀY ĐỂ TIMER CHẠY KHI DI CHUYỂN
            }


        } else {
            // --- Cập nhật animation timer khi không di chuyển ---
            // Logic này giữ nguyên, nhưng giờ timer cũng chạy khi di chuyển ở khối if
            if (currentAnimation != null) {
                animationTimer += deltaTime;
                // TODO: Cập nhật animation khi chết nếu !isAlive
            }
        }


        // TODO: Xóa logic kiểm tra va chạm Player với Entity (Bom, Enemy, Flame, Item) khỏi Bomberman.handle
        // Ngoại trừ logic KÍCH HOẠT Đá Bom và logic nhận sát thương/nhặt item.
        // Va chạm chặn di chuyển đã được xử lý ở trên trong checkCollision.
        // Va chạm nhận sát thương (Flame, Enemy) và nhặt Item sẽ được xử lý ở Bomberman.handle hoặc Entity base class.

    }


    // --- Phương thức kiểm tra va chạm tại một vị trí pixel cụ thể (x, y) ---
    // Phương thức này giờ kiểm tra va chạm với Tile KHÔNG đi qua HOẶC Bom làm vật cản.
    // Trả về true nếu CÓ va chạm làm vật cản, false nếu KHÔNG va chạm.
    // CHỮ KÝ ĐƯỢC SỬA ĐỔI ĐỂ NHẬN DANH SÁCH BOM (VÀ ENEMY)
    // TẠM THỜI KHÔNG NHẬN DANH SÁCH ENEMY VÌ CHƯA CÓ LỚP ENEMY
    private boolean checkCollision(double checkPixelX, double checkPixelY, List<Bomb> bombs /* TODO: Thêm List<Object> enemies sau này */) {
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
        if (bombs != null) {
            // Duyệt qua từng quả Bom trong danh sách
            for (com.example.bomberman.entities.Bomb bomb : bombs) {
                // Chỉ kiểm tra với Bom còn active, chưa nổ (bom đã nổ không chặn di chuyển theo cách này)
                if (bomb.isActive() && !bomb.isExploded()) { // isExploded() là getter trong Bomb

                    // --- SỬA LỖI: KIỂM TRA XEM PLAYER CÓ ĐANG TRONG PHẠM VI PIXEL CỦA QUẢ BOM CỦA CHÍNH MÌNH KHÔNG ---
                    // Kiểm tra xem Bom này có phải của Player không.
                    boolean isOwnBomb = (bomb.getOwner() == this);

                    if (isOwnBomb) {
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
                    }

                    double checkedPlayerLeft = checkPixelX;
                    double checkedPlayerRight = checkPixelX + Sprite.SCALED_SIZE;
                    double checkedPlayerTop = checkPixelY;
                    double checkedPlayerBottom = checkPixelY + Sprite.SCALED_SIZE;

                    double bombLeft = bomb.getPixelX();
                    double bombRight = bomb.getPixelX() + Sprite.SCALED_SIZE;
                    double bombTop = bomb.getPixelY();
                    double bombBottom = bomb.getPixelY() + Sprite.SCALED_SIZE;

                    // Logic kiểm tra chồng lấn giữa hai hình hộp tại vị trí kiểm tra
                    boolean overlapAtCheckedPosition = checkedPlayerRight > bombLeft && checkedPlayerLeft < bombRight &&
                            checkedPlayerBottom > bombTop && checkedPlayerTop < bombBottom;


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
        return false; // Vị trí kiểm tra này có thể di chuyển đến -> KHÔNG CHẶN
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
    public boolean collidesWith(com.example.bomberman.entities.Bomb bomb) {
        // TODO: Có thể thêm kiểm tra trạng thái Player (ví dụ: isDead) nếu Player chết không va chạm với Bom
        // if (!this.isAlive()) return false;

        // Kiểm tra xem Bomb có null, không active hoặc đã nổ chưa
        // Thường chỉ va chạm với bom đang đếm ngược.
        if (bomb == null || !bomb.isActive() || bomb.isExploded()) { // isExploded() là getter trong Bomb
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

    // TODO: Thêm các phương thức va chạm chồng lấn với Entity khác (Enemy, Flame) sau này
    // public boolean collidesWith(Enemy enemy) { ... }
    // public boolean collidesWith(Flame flame) { ... }


    // --- Getters ---
    // Cung cấp các phương thức để các lớp khác truy cập thông tin của Player
    public double getPixelX() { return pixelX; }
    public double getPixelY() { return pixelY; }
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public Direction getCurrentDirection() { return currentDirection; }
    public boolean isMoving() { return isMoving; }
    public boolean isAlive() { return isAlive; } // Getter cho trạng thái sống
    public int getLives() { return lives; } // Getter cho số mạng
    public int getCurrentBombs() { return currentBombs; } // Getter cho số bom hiện có
    public int getMaxBombs() { return maxBombs; } // Getter cho số bom tối đa
    public int getFlameLength() { return flameLength; } // Getter cho độ dài lửa
    public double getSpeed() { return speed; } // Getter cho tốc độ
    // TODO: public boolean canKickBomb() { return canKickBomb; } // Có thể thay thuộc tính public bằng getter
    // TODO: Getters cho các thuộc tính Powerup nâng cao khác (canPassBrick, canPassFlame, isInvincible)


    // Phương thức được gọi bởi Vòng lặp Game để vẽ Player
    public void render(GraphicsContext gc) {
        // TODO: Áp dụng hiệu ứng nhấp nháy/trong suốt nếu đang bất tử tạm thời sau này
        // if (isInvincible) { gc.setGlobalAlpha(0.5); } // Ví dụ

        // Lấy frame animation hiện tại để vẽ
        Image currentImage = null;
        if (currentAnimation != null) {
            Sprite currentSpriteFrame = currentAnimation.getFrame(animationTimer);
            if (currentSpriteFrame != null) {
                currentImage = currentSpriteFrame.getFxImage();
            }
        } else {
            // Fallback nếu không có animation
            currentImage = Sprite.player_down.getFxImage();
        }

        // Fallback cuối cùng
        if (currentImage == null) {
            currentImage = Sprite.player_down.getFxImage();
        }

        // Vẽ hình ảnh Player tại vị trí pixel hiện tại với kích thước đã scale
        if (currentImage != null) {
            // TODO: Điều chỉnh vị trí vẽ nếu cần căn giữa sprite hoặc offset so với vị trí pixel
            // Ví dụ: gc.drawImage(currentImage, pixelX - offsetX, pixelY - offsetY, Sprite.SCALED_SIZE, Sprite.SCALED_SIZE);
            gc.drawImage(currentImage, pixelX, pixelY+Bomberman.UI_PANEL_HEIGHT, Sprite.SCALED_SIZE, Sprite.SCALED_SIZE);
        }

        // TODO: Đặt lại global alpha về 1.0 sau khi vẽ nếu đã thay đổi cho hiệu ứng bất tử sau này
        // if (isInvincible) { gc.setGlobalAlpha(1.0); }

        // TODO: Logic vẽ các yếu tố UI cho Player (số mạng, vv) - Thường vẽ ở lớp UI hoặc Bomberman
    }

    // TODO: Phương thức nhận sát thương
    // public void takeDamage(int damage) {
    //     if (!isAlive) return;
    //     if (isInvincible) return;
    //     lives -= damage;
    //     System.out.println("Player took damage. Remaining lives: " + lives); // Log
    //     if (lives <= 0) {
    //         die(); // Gọi phương thức chết
    //     } else {
    //         // Kích hoạt bất tử tạm thời sau khi nhận sát thương
    //         isInvincible = true;
    //         invincibilityTimer = 0; // Reset timer
    //         // TODO: Phát âm thanh nhận sát thương
    //     }
    // }

    // TODO: Phương thức chết
    // private void die() {
    //     isAlive = false;
    //     isMoving = false; // Dừng di chuyển
    //     currentDirection = Direction.NONE; // Reset hướng
    //     deathTimer = 0; // Bắt đầu timer chết
    //     // TODO: Chuyển sang animation chết
    //     // TODO: Thông báo cho gameManager Player đã chết
    //     System.out.println("Player died."); // Log
    //     // TODO: Phát âm thanh chết
    // }

    // TODO: Phương thức hồi sinh (được gọi bởi gameManager sau khi chết và còn mạng)
    // public void respawn(int startGridX, int startGridY) {
    //     isAlive = true;
    //     // Reset vị trí
    //     this.gridX = startGridX;
    // this.gridY = startGridY;
    //     this.pixelX = startGridX * Sprite.SCALED_SIZE;
    //     this.pixelY = startGridY * Sprite.SCALED_SIZE;
    //     // Reset trạng thái
    //     isMoving = false;
    //     currentDirection = Direction.DOWN;
    //     // TODO: Reset animation về trạng thái sống
    //     currentAnimation = idleDownAnimation;
    //     // TODO: Kích hoạt bất tử tạm thời sau khi hồi sinh
    //     isInvincible = true;
    //     invincibilityTimer = 0;
    //     System.out.println("Player respawned."); // Log
    // }


}