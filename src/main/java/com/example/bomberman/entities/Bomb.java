package com.example.bomberman.entities;

import com.example.bomberman.Bomberman;
import com.example.bomberman.Map.Map;
import com.example.bomberman.Map.Tile;
import com.example.bomberman.Map.TileType;
import com.example.bomberman.entities.Items.Item;
import com.example.bomberman.graphics.Sprite;
import com.example.bomberman.graphics.Animation;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Lớp đại diện cho một quả bom
public class Bomb {

    // Vị trí theo Pixel
    private double pixelX;
    private double pixelY;

    // Vị trí theo lưới
    private int gridX; // Cột
    private int gridY; // Hàng

    // --- Thuộc tính Bom ---
    private double timeToExplode = 2.0; // Thời gian đếm ngược để nổ (giây).
    private double explosionTimer = 0; // Biến đếm thời gian đã trôi qua kể từ khi đặt bom
    private int flameLength; // Bán kính nổ (lấy từ Player khi đặt)
    private Object owner; // Tham chiếu đến Player đã đặt quả bom này (quan trọng cho logic sau này)
    //--- đá bom---
    private boolean isKicked = false;
    private Direction kickDirection = Direction.NONE;
    private double kickSpeed = 100;// bo cung duoc
    public static final double KICK_SPEED_CONSTANT = 200.0;

    private Bomberman gameManager;
    // Trạng thái của Bom
    private boolean exploded = false; // Cờ cho biết bom đã nổ chưa
    private boolean active = true; // Cờ cho biết bom còn hoạt động không (chưa nổ và chưa biến mất)

    // --- Animation attributes ---
    // Animation đếm ngược của bom
    private Animation countdownAnimation;
    // TODO: Thêm animation nổ (explosionAnimation) nếu muốn bom có hiệu ứng nổ riêng trước khi tạo Flame

    private Animation currentAnimation; // Animation đang chạy hiện tại
    private double animationTimer = 0; // Dùng để theo dõi thời gian đã trôi qua cho animation hiện tại

    // Tham chiếu đến đối tượng Map (cần cho logic nổ để kiểm tra Tile)
    private Map map;

    // --- Danh sách các đối tượng Flame được tạo ra bởi vụ nổ này ---
    private List<Flame> generatedFlames = new ArrayList<>();




    // Constructor
    public Bomb(int startGridX, int startGridY, int flameLength, Player owner, Map map,Bomberman gameManager) {
        this.gridX = startGridX;
        this.gridY = startGridY;
        // Đặt bom vào góc trên bên trái của ô lưới
        this.pixelX = gridX * Sprite.SCALED_SIZE;
        this.pixelY = gridY * Sprite.SCALED_SIZE;
        this.gameManager=gameManager;

        this.flameLength = flameLength;
        this.owner = owner; // Lưu Player đã đặt bom
        this.map = map; // Lưu tham chiếu đến Map

        // --- Initialize animations ---
        // Animation đếm ngược của bom (nhấp nháy giữa 3 sprite)
        // Thời gian hiển thị mỗi frame cho animation đếm ngược
        double countdownFrameDuration = 0.2; // Điều chỉnh giá trị này
        countdownAnimation = new Animation(countdownFrameDuration, true, Sprite.bomb, Sprite.bomb_1, Sprite.bomb_2); // Lặp lại

        // Bắt đầu với animation đếm ngược
        currentAnimation = countdownAnimation;

    }

    // --- Phương thức được gọi bởi Vòng lặp Game (AnimationTimer) ---

    // Phương thức cập nhật trạng thái mỗi frame
    // In class Bomb
    public void update(double deltaTime) {
        if (!active) {
            return; // Nếu bom không còn hoạt động, không làm gì cả
        }

        boolean stoppedKickingThisFrame = false; // Biến cục bộ để theo dõi xem có nên dừng đá trong frame này không

        if (isKicked) {
            double deltaPixelX = 0;
            double deltaPixelY = 0;

            // Tính toán thay đổi vị trí dựa trên hướng và tốc độ đá
            switch (kickDirection) {
                case UP: deltaPixelY = -kickSpeed * deltaTime; break;
                case DOWN: deltaPixelY = kickSpeed * deltaTime; break;
                case LEFT: deltaPixelX = -kickSpeed * deltaTime; break;
                case RIGHT: deltaPixelX = kickSpeed * deltaTime; break;
                case NONE:
                    // Trường hợp này không nên xảy ra thường xuyên nếu startKicking đúng,
                    // nhưng để an toàn thì đánh dấu dừng lại
                    stoppedKickingThisFrame = true;
                    break;
            }

            // Chỉ kiểm tra va chạm nếu bom vẫn đang được lệnh đá (hướng khác NONE)
            if (!stoppedKickingThisFrame) {
                double nextPixelX = pixelX + deltaPixelX;
                double nextPixelY = pixelY + deltaPixelY;

                // --- KIỂM TRA CÁC LOẠI VA CHẠM CÓ THỂ CHẶN BOM ---

                // 1. Kiểm tra va chạm với Tile (Tường, Gạch)
                boolean tileCollision = checkCollisionWithTiles(nextPixelX, nextPixelY);
                if (tileCollision) {
                    stoppedKickingThisFrame = true; // Dừng lại do va chạm Tile
                    System.out.println("Bomb stopped kicking due to tile collision at (" + gridX + ", " + gridY + ")");
                }

                // 2. Kiểm tra va chạm với Bom khác (chỉ nếu chưa bị chặn bởi Tile)
                if (!stoppedKickingThisFrame && gameManager != null) { // Đảm bảo gameManager không null
                    List<Bomb> otherBombs = gameManager.getBombs(); // Lấy danh sách bom từ GameManager
                    for (Bomb otherBomb : otherBombs) {
                        // Bỏ qua chính nó và các bom không active
                        if (otherBomb == this || !otherBomb.isActive()) {
                            continue;
                        }

                        // Kiểm tra va chạm AABB (vị trí tiếp theo của bom này vs vị trí hiện tại của bom khác)
                        double bombSize = Sprite.SCALED_SIZE;
                        double otherBombLeft = otherBomb.getPixelX();
                        double otherBombRight = otherBomb.getPixelX() + bombSize;
                        double otherBombTop = otherBomb.getPixelY();
                        double otherBombBottom = otherBomb.getPixelY() + bombSize;

                        double nextLeft = nextPixelX;
                        double nextRight = nextPixelX + bombSize;
                        double nextTop = nextPixelY;
                        double nextBottom = nextPixelY + bombSize;

                        boolean overlap = nextRight > otherBombLeft && nextLeft < otherBombRight &&
                                nextBottom > otherBombTop && nextTop < otherBombBottom;

                        if (overlap) {
                            stoppedKickingThisFrame = true; // Dừng lại do va chạm Bom khác
                            System.out.println("Bomb stopped kicking due to collision with another bomb at (" + otherBomb.getGridX() + ", " + otherBomb.getGridY() + ")");
                            break; // Đã tìm thấy va chạm, không cần kiểm tra bom khác nữa
                        }
                    }
                }

                // --- KIỂM TRA VA CHẠM KHÔNG CHẶN BOM ---

                // 3. Kiểm tra va chạm với Item (không làm dừng bom)
                if (gameManager != null) { // Vẫn kiểm tra va chạm Item dù bom có bị dừng hay không
                    List<Item> itemsOnMap = gameManager.getItems(); // Lấy danh sách Item từ GameManager
                    for (Item item : itemsOnMap) {
                        if (!item.isActive()) {
                            continue;
                        }

                        // Kiểm tra va chạm AABB (vị trí tiếp theo của bom này vs vị trí hiện tại của item)
                        double bombSize = Sprite.SCALED_SIZE; // Giả sử item cùng kích thước
                        double itemLeft = item.getPixelX();
                        double itemRight = item.getPixelX() + bombSize;
                        double itemTop = item.getPixelY();
                        double itemBottom = item.getPixelY() + bombSize;

                        double nextLeft = nextPixelX;
                        double nextRight = nextPixelX + bombSize;
                        double nextTop = nextPixelY;
                        double nextBottom = nextPixelY + bombSize;

                        boolean overlap = nextRight > itemLeft && nextLeft < itemRight &&
                                nextBottom > itemTop && nextTop < itemBottom;

                        if (overlap) {

                        }
                    }
                }

                if (!stoppedKickingThisFrame) {
                    // Nếu không có va chạm nào chặn lại, CẬP NHẬT vị trí pixel
                    pixelX = nextPixelX;
                    pixelY = nextPixelY;
                    // Cập nhật vị trí lưới dựa trên vị trí pixel mới
                    // Math.round thường cho kết quả mượt hơn floor/ceil khi chuyển từ pixel sang grid
                    gridX = (int) Math.round(pixelX / Sprite.SCALED_SIZE);
                    gridY = (int) Math.round(pixelY / Sprite.SCALED_SIZE);
                }
                // Nếu stoppedKickingThisFrame là true, không làm gì cả (vị trí không đổi)

            } // Kết thúc khối if (!stoppedKickingThisFrame) để kiểm tra va chạm

            // Nếu bom bị dừng lại trong frame này (do va chạm hoặc hướng NONE)
            if (stoppedKickingThisFrame) {
                isKicked = false; // Đặt lại trạng thái không bị đá
                kickDirection = Direction.NONE; // Reset hướng đá
                kickSpeed = 0; // Đặt tốc độ về 0 để chắc chắn nó dừng lại
                // Optional: Thêm logic neo bom sát cạnh vật cản nếu muốn (phức tạp hơn)
            }
            // Bom vẫn tiếp tục đếm giờ nổ ngay cả khi bị đá hoặc đã dừng lại

        } // Kết thúc khối if(isKicked)

        // Tăng thời gian đếm ngược (Luôn thực hiện dù có bị đá hay không)
        explosionTimer += deltaTime;

        // --- Cập nhật trạng thái animation ---
        if (currentAnimation != null) {
            animationTimer += deltaTime;
        }

        // --- Kiểm tra xem đã đến lúc nổ chưa ---
        if (explosionTimer >= timeToExplode && !exploded) {
            explode(); // Kích hoạt vụ nổ
        }

        // ... (Phần xử lý sau khi nổ nếu có, ví dụ đợi animation nổ kết thúc) ...
    }

    // Phương thức được gọi khi bom nổ
    private void explode() {
        if (exploded) return;

        exploded = true;
        active = false; // Đặt active = false ngay khi nổ để Bomberman có thể xử lý

        isKicked=false;
        kickDirection=Direction.NONE;
        kickSpeed=0;

        System.out.println("Bomb at (" + gridX + ", " + gridY + ") exploded! Flame length: " + flameLength); // Log tạm thời

        if (gameManager != null) { // Kiểm tra gameManager không null trước khi gọi
            gameManager.playExplosionSound();
        }
        // --- Logic tạo các đối tượng Flame lan tỏa ra các hướng ---

        // Tạo Flame ở tâm vụ nổ (vị trí của bom)
        generatedFlames.clear();

        generatedFlames.add(new Flame(gridX, gridY, 0)); // flameType 0 = center

        // Các hướng lan tỏa: Lên, Xuống, Trái, Phải
        int[] dx = {0, 0, -1, 1}; // Thay đổi cột (X) cho Trái, Phải
        int[] dy = {-1, 1, 0, 0}; // Thay đổi hàng (Y) cho Lên, Xuống
        int[] flameTypes = {5, 6, 3, 4}; // Loại ngọn lửa cuối cho mỗi hướng (Up, Down, Left, Right)
        int[] middleFlameTypes = {2, 2, 1, 1}; // Loại ngọn lửa giữa cho mỗi hướng (Up, Down, Left, Right)


        for (int i = 0; i < 4; i++) { // Duyệt qua 4 hướng
            int currentDx = dx[i];
            int currentDy = dy[i];
            int endType = flameTypes[i];
            int middleType = middleFlameTypes[i];

            for (int l = 1; l <= flameLength; l++) { // Lan tỏa theo chiều dài ngọn lửa
                int nextGridX = gridX + currentDx * l;
                int nextGridY = gridY + currentDy * l;

                // Kiểm tra biên bản đồ
                if (nextGridX < 0 || nextGridX >= map.getCols() || nextGridY < 0 || nextGridY >= map.getRows()) {
                    break; // Dừng lan tỏa nếu ra ngoài biên bản đồ
                }

                Tile tile = map.getTile(nextGridX, nextGridY);

                // Nếu gặp Tường (#), dừng lan tỏa theo hướng này
                if (tile != null && tile.getType() == TileType.WALL) {
                    break;
                }

                // Xác định loại ngọn lửa (giữa hay cuối)
                int currentFlameType = middleType;
                // Nếu là đoạn cuối cùng hoặc gặp Gạch, đây là ngọn lửa cuối
                if (l == flameLength || (tile != null && tile.getType() == TileType.BRICK)) {
                    currentFlameType = endType;
                }

                generatedFlames.add(new Flame(nextGridX, nextGridY, currentFlameType));

                if (tile != null && tile.getType() == TileType.BRICK) {
                    map.brickHitByFlame(nextGridX, nextGridY); // Gọi phương thức mới trong Map
                    break; // Dừng lan tỏa sau khi gặp gạch
                }

            }
        }

        // Sau khi tạo Flame, bom gốc (đối tượng Bomb này) sẽ biến mất sau khi animation nổ kết thúc
        // active = false; // Điều này đã được thực hiện ở đầu phương thức explode()
    }
    public void triggerExplosion() {
        // Chỉ kích nổ nếu bom đang đếm ngược (active và chưa exploded)
        if (this.active && !this.exploded) {
            System.out.println("Chain reaction triggered for bomb at (" + gridX + ", " + gridY + ")");

            explode(); // Gọi trực tiếp để nổ ngay trong frame này
        }
    }
    // phương thức sút bom
    public void startKicking(Direction direction,double speed){
        // Chỉ cho phép đá bom nếu nó chưa nổ và đang active
        if(!isExploded() && this.active){
            this.isKicked=true;
            this.kickDirection=direction;
            this.kickSpeed=speed;
            System.out.println("Bomb at (" + gridX + ", " + gridY + ") is kicked in direction " + direction + " with speed " + speed); // Log
            // TODO: Phát âm thanh đá bom
        }
        // TODO: Thêm phương thức stopKicking() nếu cần dừng đá bằng lệnh từ ngoài
// public void stopKicking() { this.isKicked = false; this.kickDirection = Direction.NONE; }
    }
    private boolean checkCollisionWithTiles(double checkPixelX, double checkPixelY) {
        double bombSize = Sprite.SCALED_SIZE;
        // Kiểm tra va chạm bằng cách lấy 4 góc của hộp va chạm Bom tại vị trí mới
        // Trừ đi 1 pixel để đảm bảo điểm lấy mẫu nằm TRONG ô pixel cuối cùng
        double topLeftX = checkPixelX;
        double topLeftY = checkPixelY;
        double topRightX = checkPixelX + bombSize - 1;
        double topRightY = checkPixelY;
        double bottomLeftX = checkPixelX;
        double bottomLeftY = checkPixelY + bombSize - 1;
        double bottomRightX = checkPixelX + bombSize - 1;
        double bottomRightY = checkPixelY + bombSize - 1;

        // Chuyển đổi các tọa độ pixel của các góc sang tọa độ lưới
        // Sử dụng Math.floor để lấy ô lưới mà góc đó nằm trong
        int gridX1 = (int) Math.floor(topLeftX / bombSize);
        int gridY1 = (int) Math.floor(topLeftY / bombSize);
        int gridX2 = (int) Math.floor(topRightX / bombSize);
        int gridY2 = (int) Math.floor(topRightY / bombSize);
        int gridX3 = (int) Math.floor(bottomLeftX / bombSize);
        int gridY3 = (int) Math.floor(bottomLeftY / bombSize);
        int gridX4 = (int) Math.floor(bottomRightX / bombSize);
        int gridY4 = (int) Math.floor(bottomRightY / bombSize);

        // Tập hợp các ô lưới cần kiểm tra (loại bỏ trùng lặp)
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

            // Lấy Tile từ Map, kiểm tra biên bản đồ trước khi lấy Tile
            Tile tile = null;
            if (map != null && checkGridX >= 0 && checkGridX < map.getCols() && checkGridY >= 0 && checkGridY < map.getRows()) {
                tile = map.getTile(checkGridX, checkGridY);
            } else {
                // Nếu điểm va chạm nằm ngoài biên bản đồ, coi như va chạm
                return true;
            }

            // Kiểm tra va chạm với các Tile không thể đi qua (Tường, Gạch)
            if (tile != null) {
                // Bom nên va chạm (dừng lại) với Tường và Gạch
                if (tile.getType() == TileType.WALL || tile.getType() == TileType.BRICK) {
                    return true; // Va chạm được phát hiện
                }
            }
        }

        return false; // Không có va chạm với Tile không đi qua được
    }
    // Phương thức được gọi bởi Vòng lặp Game để vẽ Bom
    public void render(GraphicsContext gc) {
        if (!active) {
            return; // Không vẽ nếu bom không còn hoạt động
        }

        Image currentImage = null;
        if (currentAnimation != null) {
            Sprite currentSpriteFrame = currentAnimation.getFrame(animationTimer);
            if (currentSpriteFrame != null) {
                currentImage = currentSpriteFrame.getFxImage();
            }
        } else {
                  currentImage = Sprite.bomb.getFxImage();
        }

        if (currentImage == null) {
            currentImage = Sprite.bomb.getFxImage();
        }

        if (currentImage != null) {
            // Vẽ hình ảnh Bom tại vị trí pixel hiện tại
            // Cần điều chỉnh vị trí vẽ nếu sprite có kích thước khác Sprite.SCALED_SIZE
            // hoặc nếu bạn muốn căn giữa sprite.
            // Hiện tại đang vẽ góc trên bên trái tại pixelX, pixelY
            gc.drawImage(currentImage, pixelX, pixelY + Bomberman.UI_PANEL_HEIGHT);
        }

    }

    // --- Getters ---
    public double getPixelX() { return pixelX; }
    public double getPixelY() { return pixelY; } // Phương thức getPixelY() đúng

    public boolean isKicked() {return isKicked; }
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public boolean isExploded() { return exploded; }
    public boolean isActive() { return active; }
    public int getFlameLength() { return flameLength; }
    public Object getOwner() { return owner; }

    public List<Flame> getGeneratedFlames() {
        return generatedFlames;
    }

    public void setActive(boolean active) { this.active = active; }
}