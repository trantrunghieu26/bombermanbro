package com.example.bomberman.entities;

import com.example.bomberman.Bomberman;
import com.example.bomberman.Map.Map;
import com.example.bomberman.Map.Tile;
import com.example.bomberman.Map.TileType;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.*;

public abstract class Enemy extends Entity { // Kế thừa từ Entity

    // --- Thuộc tính vị trí và di chuyển (Đã có trong Entity) ---
    // protected double x; // Sử dụng x từ Entity
    // protected double y; // Sử dụng y từ Entity
    // protected int gridX; // Sử dụng gridX từ Entity
    // protected int gridY; // Sử dụng gridY từ Entity

    protected double speed; // Tốc độ riêng của mỗi loại Enemy
    protected Direction currentDirection = Direction.NONE;
    protected boolean isMoving = false;
    protected Random random = new Random(); // Để di chuyển ngẫu nhiên cho Enemy cơ bản

    // --- Thuộc tính trạng thái (isAlive, active đã có trong Entity) ---
    // protected boolean active = true; // Sử dụng active từ Entity
    protected boolean isAlive = true; // Vẫn giữ cờ isAlive cho rõ ràng trạng thái sống/chết game play
    protected double dyingTimer = 0; // Thời gian cho animation chết
    protected final double TIME_TO_DIE = 0.8; // Thời gian animation chết (giây)
    protected int scoreValue; // Điểm nhận được khi tiêu diệt

    // --- Tham chiếu ---
    protected Map map;
    protected Bomberman gameManager; // Sẽ được refactor sau này

    // --- Animation ---
    protected Animation walkLeftAnimation;
    protected Animation walkRightAnimation;
    protected Animation deadAnimation;
    protected Animation currentAnimation;
    protected double animationTimer = 0;

    // Thêm thuộc tính để lưu hướng cuối cùng không phải NONE (cho animation đứng yên)
    protected Direction lastNonNoneDirection = Direction.DOWN; // Mặc định hướng xuống

    // --- Thêm các thuộc tính mới để xử lý bị kẹt ---
    private Direction lastAttemptedDirection = Direction.NONE; // Hướng vừa cố gắng đi và bị chặn
    private int consecutiveBlocks = 0;
    private final int MAX_CONSECUTIVE_BLOCKS = 4; // Ngưỡng bị kẹt: Số lần liên tiếp bị chặn theo cùng một hướng

    // Constructor
    public Enemy(int startGridX, int startGridY, double speed, int scoreValue, Map map, Bomberman gameManager) {
        // Gọi constructor của lớp cha Entity
        super(startGridX, startGridY, null); // Entity cần một Image ban đầu, hoặc sửa Entity để chấp nhận null

        // Cập nhật vị trí pixel và lưới từ Entity sau khi gọi super()
        this.gridX = startGridX;
        this.gridY = startGridY;
        this.x = startGridX * Sprite.SCALED_SIZE; // Đảm bảo x, y cũng được set
        this.y = startGridY * Sprite.SCALED_SIZE;

        this.speed = speed;
        this.scoreValue = scoreValue;
        this.map = map;
        this.gameManager = gameManager;
        this.isAlive = true; // Bắt đầu là sống

        // animation sẽ được khởi tạo trong lớp con

        // Chọn hướng di chuyển ban đầu ngẫu nhiên
        setRandomDirection(); // Enemy bắt đầu bằng cách chọn hướng ngẫu nhiên đầu tiên
        // Cập nhật lastNonNoneDirection nếu setRandomDirection chọn hướng khác NONE
        if (currentDirection != Direction.NONE) {
            lastNonNoneDirection = currentDirection;
        } else {
            // Nếu không tìm được hướng nào khi spawn, mặc định hướng xuống
            lastNonNoneDirection = Direction.DOWN;
            // Cập nhật animation đứng yên mặc định (sẽ được làm ở updateAnimationForDirection)
        }
    }

    // --- Phương thức cập nhật chung ---
    @Override // Override từ Entity
    public void update(double deltaTime) {
        // Nếu không còn active (đã bị xóa khỏi game), không update gì cả
        if (!isActive()) return; // Sử dụng cờ active từ Entity

        // Nếu đang trong quá trình chết (animation chết đang chạy)
        if (!isAlive) {
            dyingTimer += deltaTime;
            animationTimer += deltaTime; // Cập nhật timer cho animation chết
            // Cờ active sẽ được đặt false bởi Bomberman sau khi dyingTimer >= TIME_TO_DIE
            return; // Dừng update logic di chuyển nếu đang chết
        }

        // Cập nhật timer animation chung (dùng cho animation di chuyển)
        animationTimer += deltaTime;

        // Tính toán bước di chuyển tiếp theo (logic cụ thể trong lớp con)
        // calculateNextMove() sẽ quyết định DESIRED currentDirection cho frame này
        calculateNextMove();

        // Thực hiện di chuyển dựa trên currentDirection được quyết định bởi calculateNextMove()
        move(deltaTime);

        // Cập nhật vị trí lưới sau khi di chuyển
        updateGridPosition(); // Di chuyển updateGridPosition xuống đây

        // Kiểm tra va chạm với Flame (có thể làm ở Bomberman.java hoặc CollisionManager)
        // checkFlameCollisions();
    }

    // --- Phương thức di chuyển ---
    // Thực hiện di chuyển vật lý và xử lý va chạm/neo
    protected void move(double deltaTime) {
        // Nếu không có hướng di chuyển, chuyển sang animation đứng yên và dừng
        if (currentDirection == Direction.NONE) {
            isMoving = false; // Đảm bảo cờ isMoving là false
            updateAnimationForDirection(Direction.NONE);
            return;
        }

        // Nếu có hướng di chuyển, đánh dấu là đang di chuyển và cập nhật animation
        isMoving = true; // Đảm bảo cờ isMoving là true
        updateAnimationForDirection(currentDirection);


        double deltaPixelX = 0;
        double deltaPixelY = 0;

        // Tính toán thay đổi vị trí dự kiến cho frame này
        switch (currentDirection) {
            case UP:    deltaPixelY = -speed * deltaTime; break;
            case DOWN:  deltaPixelY = speed * deltaTime; break;
            case LEFT:  deltaPixelX = -speed * deltaTime; break;
            case RIGHT: deltaPixelX = speed * deltaTime; break;
            case NONE: break; // Trường hợp này không xảy ra nhờ kiểm tra ở trên
        }

        double nextPixelX = x + deltaPixelX; // Vị trí X dự kiến
        double nextPixelY = y + deltaPixelY; // Vị trí Y dự kiến

        boolean collided = false;

        // --- Xử lý va chạm trục X và di chuyển/neo ---
        if (deltaPixelX != 0) {
            // Kiểm tra va chạm tại vị trí X mới (nextPixelX), Y cũ (y)
            if (checkMovementCollision(nextPixelX, y)) {
                collided = true;
                // Nếu bị chặn, neo Enemy lại sát cạnh vật cản theo trục X
                if (deltaPixelX > 0) { // Di chuyển sang phải
                    x = (int) Math.floor((x + width) / Sprite.SCALED_SIZE) * Sprite.SCALED_SIZE - width;
                } else { // Di chuyển sang trái
                    x = (int) Math.ceil(x / Sprite.SCALED_SIZE) * Sprite.SCALED_SIZE;
                }
            } else {
                // Nếu không bị chặn theo X, cập nhật vị trí X
                x = nextPixelX;
            }
        }


        // --- Xử lý va chạm trục Y và di chuyển/neo ---
        // Sử dụng vị trí X đã CẬP NHẬT (hoặc neo lại) để kiểm tra va chạm theo Y
        if (deltaPixelY != 0) {
            // Kiểm tra va chạm tại vị trí X hiện tại (sau xử lý X), Y mới (nextPixelY)
            if (checkMovementCollision(x, nextPixelY)) {
                collided = true;
                // Nếu bị chặn, neo Enemy lại sát cạnh vật cản theo trục Y
                if (deltaPixelY > 0) { // Di chuyển xuống
                    y = (int) Math.floor((y + height) / Sprite.SCALED_SIZE) * Sprite.SCALED_SIZE - height;
                } else { // Di chuyển lên
                    y = (int) Math.ceil(y / Sprite.SCALED_SIZE) * Sprite.SCALED_SIZE;
                }
            } else {
                // Nếu không bị chặn theo Y, cập nhật vị trí Y
                y = nextPixelY;
            }
        }

        // Nếu bị chặn theo bất kỳ trục nào trong frame này
        if (collided) {
            // Gọi logic xử lý bị chặn với hướng mà nó đang cố gắng đi (currentDirection)
            // handleBlockedMovement sẽ đặt currentDirection về NONE và isMoving về false nếu không tìm được hướng thay thế
            handleBlockedMovement(currentDirection);
        }
        // Nếu không bị chặn, move thành công và cờ isMoving + currentDirection vẫn giữ nguyên (đã set ở đầu hàm)
        // consecutiveBlocks và lastAttemptedDirection chỉ được reset trong setRandomDirection() và handleBlockedMovement()
    }

    /**
     * Phương thức xử lý khi Enemy bị chặn khi cố gắng di chuyển theo `blockedDirection`.
     * Đặt trạng thái bị chặn và cố gắng tìm hướng thay thế.
     * Được gọi bởi move() khi phát hiện va chạm.
     * @param blockedDirection Hướng mà Enemy vừa cố gắng đi và bị chặn.
     */
    protected void handleBlockedMovement(Direction blockedDirection) {
        //System.out.println("ENEMY DEBUG: Blocked attempting to move " + blockedDirection + " at grid (" + gridX + "," + gridY + ")"); // Log

        // Đặt trạng thái không di chuyển tạm thời
        isMoving = false;
        currentDirection = Direction.NONE; // Đặt hướng về NONE tạm thời

        // Đếm số lần bị chặn liên tiếp theo hướng vừa cố gắng đi
        if (blockedDirection != Direction.NONE && blockedDirection == lastAttemptedDirection) {
            consecutiveBlocks++;
        } else {
            consecutiveBlocks = 1;
            lastAttemptedDirection = blockedDirection; // Lưu hướng vừa bị chặn
        }

        // Nếu bị kẹt ở hướng này quá nhiều lần -> thử chiến lược ngẫu nhiên mạnh hơn
        if (consecutiveBlocks >= MAX_CONSECUTIVE_BLOCKS) {
            // System.out.println("ENEMY DEBUG: Stuck threshold (" + MAX_CONSECUTIVE_BLOCKS + ") reached for " + blockedDirection + ". Forcing random re-direction.");
            // Reset bộ đếm và hướng bị chặn
            consecutiveBlocks = 0;
            lastAttemptedDirection = Direction.NONE;
            // Gọi setRandomDirection() để chọn một hướng ngẫu nhiên khả thi
            // setRandomDirection() sẽ dùng canMoveTowards đã cải tiến
            setRandomDirection();
            // System.out.println("ENEMY DEBUG: Forced setRandomDirection() chose: " + currentDirection); // Log
            return; // Đã tìm hướng mới hoặc không tìm được, thoát
        }


        // --- Logic tìm hướng thay thế thông minh hơn (khi chưa đạt ngưỡng kẹt) ---
        // Các lớp con có thể ghi đè calculateNextMove để có logic tìm đường phức tạp hơn (BFS),
        // nhưng khi bị chặn và chưa đến lúc chạy lại calculateNextMove phức tạp,
        // logic này cung cấp một cách phản ứng nhanh.

        List<Direction> alternativeDirections = new ArrayList<>();

        // 1. Ưu tiên hướng vuông góc với hướng bị chặn
        if (blockedDirection == Direction.UP || blockedDirection == Direction.DOWN) {
            alternativeDirections.add(Direction.LEFT);
            alternativeDirections.add(Direction.RIGHT);
        } else if (blockedDirection == Direction.LEFT || blockedDirection == Direction.RIGHT) {
            alternativeDirections.add(Direction.UP);
            alternativeDirections.add(Direction.DOWN);
        }
        // Không cần thêm hướng bị chặn vào danh sách này

        // Xáo trộn các hướng thay thế (để có sự ngẫu nhiên nhưng vẫn ưu tiên hướng vuông góc)
        Collections.shuffle(alternativeDirections);

        // Thử các hướng thay thế đã được ưu tiên và xáo trộn
        for (Direction alternativeDir : alternativeDirections) {
            // Chỉ kiểm tra xem hướng mới có thể BẮT ĐẦU đi được không
            if (canMoveTowards(alternativeDir)) { // Sử dụng canMoveTowards đã cải tiến
                // System.out.println("ENEMY DEBUG: Found alternative direction (perpendicular): " + alternativeDir); // Log
                // Đặt hướng mới và trạng thái di chuyển
                currentDirection = alternativeDir;
                isMoving = true;
                // KHÔNG reset consecutiveBlocks/lastAttemptedDirection ở đây.
                // Nếu nó lại bị chặn theo hướng này, bộ đếm sẽ tăng lên.
                // updateAnimationForDirection(currentDirection); // Animation được cập nhật ở move()
                return; // Đã tìm thấy hướng mới, thoát
            }
        }

        // Nếu không tìm thấy hướng thay thế khả thi từ các hướng vuông góc (và chưa đạt ngưỡng kẹt)
        // Enemy sẽ đứng yên tạm thời (isMoving=false, currentDirection=NONE)
        // calculateNextMove() ở frame tiếp theo sẽ thấy !isMoving
        // và sẽ gọi logic của Enemy cụ thể (random cho Balloom/Doll, BFS cho Oneal, chase/random for Ghost)
        // logic đó sẽ gọi setRandomDirection() HOẶC tìm đường đi mới bằng BFS,
        // sử dụng canMoveTowards đã cải tiến.

        // System.out.println("ENEMY DEBUG: No immediate alternative direction found from handleBlockedMovement. Stopping for now."); // Log
        // currentDirection và isMoving đã được đặt false ở đầu hàm
    }


    /**
     * Kiểm tra xem Enemy có thể BẮT ĐẦU di chuyển theo hướng `direction` hay không.
     * Phương pháp kiểm tra chặt chẽ hơn bằng cách giả lập bước di chuyển bằng tốc độ
     * trong một khoảng thời gian nhỏ (ví dụ 1 frame) và sử dụng logic va chạm pixel.
     * Trả về true nếu có thể bắt đầu đi, false nếu bị chặn ngay lập tức.
     */
    protected boolean canMoveTowards(Direction direction) {
        if (direction == Direction.NONE) return true; // Luôn có thể "di chuyển" theo hướng NONE (đứng yên)

        // Giả lập vị trí sau một bước di chuyển nhỏ (tương đương 1 frame)
        double testPixelX = x; // Vị trí pixel hiện tại (góc trên bên trái)
        double testPixelY = y;

        // Tính toán bước di chuyển trong một frame giả định (ví dụ: 1/60 giây)
        double step = speed * (1.0 / 60.0); // Sử dụng tốc độ thực tế và thời gian frame cố định (hoặc nhỏ hơn)

        switch (direction) {
            case UP:    testPixelY -= step; break;
            case DOWN:  testPixelY += step; break;
            case LEFT:  testPixelX -= step; break;
            case RIGHT: testPixelX += step; break;
            default: return false; // Không kiểm tra hướng khác
        }

        // Sử dụng logic va chạm pixel (checkMovementCollision) để kiểm tra xem
        // hộp va chạm của Enemy tại vị trí (testPixelX, testPixelY) có bị vật cản chặn hay không.
        // CheckCollision logic sẽ gọi isObstacleAtPixel, sử dụng isObstacle của lớp con.
        return !checkMovementCollision(testPixelX, testPixelY);
    }


    /**
     * Kiểm tra va chạm pixel-perfect tại một vị trí (checkPixelX, checkPixelY).
     * Phương thức này kiểm tra nhiều điểm bên trong hộp va chạm
     * và gọi isObstacleAtPixel (sử dụng isObstacle() của lớp con).
     * @param checkPixelX Vị trí X (pixel) để kiểm tra.
     * @param checkPixelY Vị trí Y (pixel) để kiểm tra.
     * @return true nếu có va chạm làm vật cản tại vị trí đó, false nếu không.
     */
    protected boolean checkMovementCollision(double checkPixelX, double checkPixelY) {
        double entitySize = Sprite.SCALED_SIZE;
        // Buffer va chạm. Giá trị này quan trọng để Enemy không bị dính vào góc hẹp.
        // Thường nên nhỏ hơn Sprite.SCALED_SIZE / 2.0.
        // Điều chỉnh giá trị này để tìm sự cân bằng.
        double buffer =0.5;

        // Tính toán tọa độ các điểm kiểm tra bên trong hộp va chạm dự kiến
        // Kiểm tra các điểm nằm bên trong (không sát biên) hộp va chạm
        double innerTop = checkPixelY + buffer;
        double innerBottom = checkPixelY + entitySize - buffer;
        double innerLeft = checkPixelX + buffer;
        double innerRight = checkPixelX + entitySize - buffer;
        double midX = checkPixelX + entitySize / 5.0; // Tâm X
        double midY = checkPixelY + entitySize / 5.0; // Tâm Y

        // Kiểm tra 9 điểm: 4 góc (với buffer), 4 điểm giữa cạnh (với buffer), 1 tâm
        // Nếu bất kỳ điểm nào nằm trong ô vật cản (theo isObstacle() của lớp con), trả về true.
        if (isObstacleAtPixel(innerLeft, innerTop) ||       // Top-Left
                isObstacleAtPixel(innerRight, innerTop) ||      // Top-Right
                isObstacleAtPixel(innerLeft, innerBottom) ||    // Bottom-Left
                isObstacleAtPixel(innerRight, innerBottom) ||   // Bottom-Right
                isObstacleAtPixel(midX, innerTop) ||            // Mid-Top
                isObstacleAtPixel(midX, innerBottom) ||         // Mid-Bottom
                isObstacleAtPixel(innerLeft, midY) ||           // Mid-Left
                isObstacleAtPixel(innerRight, midY) ||          // Mid-Right
                isObstacleAtPixel(midX, midY) // Center
        ) {
            return true; // Va chạm được phát hiện
        }

        return false; // Không có va chạm
    }
    // Helper kiểm tra xem một điểm pixel có nằm trong ô vật cản không
    // Logic này chuyển tọa độ pixel sang lưới và gọi isObstacle()
    private boolean isObstacleAtPixel(double px, double py) {
        // Chuyển đổi tọa độ pixel sang tọa độ lưới bằng cách làm tròn xuống (floor)
        int gx = (int) Math.floor(px / Sprite.SCALED_SIZE);
        int gy = (int) Math.floor(py / Sprite.SCALED_SIZE);

        // Kiểm tra biên bản đồ trước khi lấy Tile
        if (map == null || gx < 0 || gx >= map.getCols() || gy < 0 || gy >= map.getRows()) {
            return true; // Ngoài map là vật cản
        }

        // Gọi phương thức isObstacle() của lớp này (hoặc lớp con override)
        // isObstacle sẽ kiểm tra Tile và Bomb...
        return isObstacle(gx, gy);
    }


    // Cập nhật vị trí lưới dựa trên vị trí pixel (làm tròn để lấy ô gần nhất)
    // Phương thức này được gọi ở cuối update() sau khi di chuyển pixel đã xảy ra.

    public void updateGridPosition() {
        // Cập nhật gridX, gridY từ x, y của Entity
        this.gridX = (int) Math.round(this.x / Sprite.SCALED_SIZE);
        this.gridY = (int) Math.round(this.y / Sprite.SCALED_SIZE);
    }


    // Cập nhật animation dựa trên hướng di chuyển mới
    // Đã sửa lỗi sử dụng walkDownAnimation
    protected void updateAnimationForDirection(Direction dir) {
        Animation targetAnimation = null;
        Direction animationDir = dir; // Hướng dùng để chọn animation

        // Nếu hướng là NONE, dùng hướng cuối cùng không phải NONE để chọn animation đứng yên
        if (dir == Direction.NONE) {
            animationDir = lastNonNoneDirection;
        } else {
            // Nếu đang di chuyển theo hướng nào đó, cập nhật lastNonNoneDirection
            lastNonNoneDirection = dir;
        }

        switch (animationDir) {
            case LEFT:  targetAnimation = walkLeftAnimation; break;
            case RIGHT: targetAnimation = walkRightAnimation; break;
            case UP:
            case DOWN:
                // Enemy cơ bản không có animation UP/DOWN riêng
                // Giữ animation ngang cuối cùng dựa trên lastNonNoneDirection
                if (lastNonNoneDirection == Direction.LEFT) {
                    targetAnimation = walkLeftAnimation;
                } else if (lastNonNoneDirection == Direction.RIGHT) {
                    targetAnimation = walkRightAnimation;
                } else {
                    // Fallback nếu lastNonNoneDirection vẫn là UP/DOWN (ví dụ mới spawn)
                    targetAnimation = walkRightAnimation; // Mặc định
                }
                break;
            case NONE:
                // Trường hợp này xử lý khi lastNonNoneDirection vẫn là NONE (ví dụ ngay sau constructor setRandomDirection không tìm được hướng)
                targetAnimation = walkRightAnimation; // Mặc định
                break;
        }

        // Chỉ đổi animation nếu animation mới khác animation hiện tại và không null
        if (targetAnimation != null && currentAnimation != targetAnimation) {
            currentAnimation = targetAnimation;
            // animationTimer = 0; // Không reset timer khi chuyển giữa các animation di chuyển/idle để chúng mượt mà
        }
        // Reset timer animation chỉ khi chết hoặc bắt đầu/kết thúc di chuyển nếu animation loop không mượt
        // Logic này có thể cần tinh chỉnh tùy vào sprite sheet cụ thể

    }


    // --- Helper kiểm tra xem một ô lưới có phải là vật cản không ---
    // Lớp con có thể override phương thức này để thay đổi loại vật cản
    // Phương thức này được gọi bởi isObstacleAtPixel và checkMovementCollision
    protected boolean isObstacle(int gX, int gY) {
        // Kiểm tra Tile
        Tile tile = map.getTile(gX, gY);
        // Enemy cơ bản bị chặn bởi Wall và Brick
        if (tile != null && (tile.getType() == TileType.WALL || tile.getType() == TileType.BRICK)) {
            return true; // Tường hoặc Gạch là vật cản
        }

        // Kiểm tra Bomb (Enemy cơ bản bị chặn bởi Bomb)
        if (gameManager != null && gameManager.isBombAtGrid(gX, gY)) {
            return true; // Có Bom là vật cản
        }

        // TODO: Kiểm tra Enemy khác nếu cần (thường Enemy có thể đi xuyên nhau)

        return false; // Không phải vật cản
    }


    // --- Phương thức để lớp con định nghĩa logic chọn hướng đi ---
    // Phương thức này sẽ được gọi bởi update() và sẽ đặt currentDirection + isMoving
    protected abstract void calculateNextMove();

    /**
     * Chọn một hướng ngẫu nhiên khả thi (mà Enemy có thể bắt đầu đi vào).
     * Sử dụng canMoveTowards() để kiểm tra.
     * Đặt currentDirection và isMoving.
     * Được gọi bởi handleBlockedMovement hoặc calculateNextMove của lớp con.
     */
    protected void setRandomDirection() {
        List<Direction> directions = new ArrayList<>(Arrays.asList(Direction.UP, Direction.DOWN, Direction.LEFT, Direction.RIGHT));
        Collections.shuffle(directions); // Xáo trộn ngẫu nhiên

        // Thử từng hướng đã xáo trộn
        for (Direction dir : directions) {
            if (canMoveTowards(dir)) { // Chỉ chọn hướng có thể bắt đầu đi được (sử dụng canMoveTowards đã cải tiến)
                currentDirection = dir;
                isMoving = true;
                // Cập nhật lastNonNoneDirection và animation
                if (dir != Direction.NONE) lastNonNoneDirection = dir;
                updateAnimationForDirection(currentDirection); // Cập nhật animation
                // Reset bộ đếm kẹt khi chủ động đổi hướng
                consecutiveBlocks = 0;
                lastAttemptedDirection = Direction.NONE;
                //System.out.println("ENEMY DEBUG: setRandomDirection() chose: " + currentDirection); // Log
                return; // Tìm thấy hướng mới, thoát
            }
        }
        // Không tìm thấy hướng nào khả thi để bắt đầu đi từ vị trí hiện tại
        currentDirection = Direction.NONE;
        isMoving = false;
        updateAnimationForDirection(Direction.NONE); // Chuyển sang animation đứng yên
        //System.out.println("ENEMY DEBUG: setRandomDirection() failed to find a walkable direction."); // Log
        consecutiveBlocks = 0; // Reset bộ đếm kẹt ngay cả khi không tìm được hướng
        lastAttemptedDirection = Direction.NONE;
    }


    // --- Phương thức render chung ---
    @Override // Override từ Entity
    public void render(GraphicsContext gc) {
        // Không vẽ nếu không còn active (đã bị xóa khỏi game)
        if (!isActive()) return; // Sử dụng cờ active từ Entity

        // Nếu đang chết và animation chết đã xong, không vẽ nữa (Bombeman sẽ xóa nó khỏi list)
        if (!isAlive && (deadAnimation == null || dyingTimer >= TIME_TO_DIE)) {
            setActive(false); // <-- Đặt active = false khi animation chết xong (Bomberman sẽ dùng cờ này để xóa)
            return;
        }


        if (gc == null) return;

        Image currentImage = null;
        // Nếu đang trong quá trình chết, ưu tiên animation chết
        if (!isAlive && deadAnimation != null) {
            // Sử dụng timer animation chung để lấy frame
            Sprite currentSpriteFrame = deadAnimation.getFrame(animationTimer);
            if (currentSpriteFrame != null) {
                currentImage = currentSpriteFrame.getFxImage();
            }
            // Đảm bảo currentAnimation là animation chết khi đang chết
            currentAnimation = deadAnimation;
        } else {
            // Nếu còn sống, sử dụng animation di chuyển/đứng yên hiện tại
            if (currentAnimation != null) {
                Sprite currentSpriteFrame = currentAnimation.getFrame(animationTimer); // Dùng timer animation chung
                if (currentSpriteFrame != null) {
                    currentImage = currentSpriteFrame.getFxImage();
                }
            }
        }


        // Fallback nếu không có animation hoặc frame lỗi
        if (currentImage == null) {
            if (isAlive) {
                currentImage = Sprite.balloom_left1.getFxImage(); // Ảnh mặc định khi sống (Balloom)
            } else {
                currentImage = Sprite.mob_dead1.getFxImage(); // Ảnh mặc định khi chết
            }
        }

        if (currentImage != null) {
            // Vẽ với offset Y của UI panel (Sẽ sửa sau khi refactor rendering)
            // Tạm thời giữ nguyên để hiển thị đúng
            gc.drawImage(currentImage, x, y + Bomberman.UI_PANEL_HEIGHT, Sprite.SCALED_SIZE, Sprite.SCALED_SIZE); // Sử dụng x, y từ Entity
        }
    }

    // --- Phương thức xử lý khi Enemy chết ---
    public void die() {
        if (!isAlive) return; // Chỉ chết một lần

        System.out.println("Enemy died at (" + gridX + ", " + gridY + "). Score: " + scoreValue);
        isAlive = false; // Đánh dấu là không còn sống game play
        isMoving = false; // Dừng di chuyển ngay lập tức
        currentDirection = Direction.NONE; // Reset hướng
        dyingTimer = 0; // Bắt đầu đếm thời gian chết
        animationTimer = 0; // Reset timer animation để bắt đầu animation chết từ đầu
        currentAnimation = deadAnimation; // Chuyển sang animation chết (Nếu deadAnimation là null, render sẽ dùng fallback)

        // Thông báo cho gameManager để cộng điểm
        if (gameManager != null) {
            gameManager.addScore(this.scoreValue);
            // gameManager.enemyDied(this); // Có thể cần phương thức này để quản lý portal
        }
        // TODO: Phát âm thanh Enemy chết
    }

    // --- Getters ---
    public boolean isAlive() { return isAlive; } // Getter cho trạng thái sống game play
    // Getters cho pixelX, pixelY, gridX, gridY đã có trong Entity
    // public double getPixelX() { return x; } // Đã có getX() trong Entity
    // public double getPixelY() { return y; } // Đã có getY() trong Entity
    // public int getGridX() { return gridX; } // Đã có getGridX() trong Entity
    // public int getGridY() { return gridY; } // Đã có getGridY() trong Entity

    public double getDyingTimer() { return dyingTimer; }
    public double getTimeToDie() { return TIME_TO_DIE; } // Cần getter này để Bomberman xóa Entity


    public boolean isMoving() { return isMoving; } // Getter cho isMoving
    public Direction getCurrentDirection() { return currentDirection; } // Getter cho currentDirection

    // Getter cho cờ active từ Entity
    // public boolean isActive() { return active; } // Đã có trong Entity

}