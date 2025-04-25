package com.example.bomberman.entities;

import com.example.bomberman.Map.Map; // Cần import lớp Map
import com.example.bomberman.Map.Tile; // Cần import lớp Tile
import com.example.bomberman.Map.TileType; // Cần import lớp TileType
import com.example.bomberman.graphics.Sprite; // Cần import Sprite
import com.example.bomberman.graphics.Animation; // Cần import lớp Animation
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image; // Cần Image để vẽ

import java.util.ArrayList; // Cần ArrayList
import java.util.List; // Cần List

// Lớp đại diện cho một quả bom
public class Bomb {

    // Vị trí theo Pixel
    private double pixelX;
    private double pixelY;

    // Vị trí theo lưới
    private int gridX; // Cột
    private int gridY; // Hàng

    // --- Thuộc tính Bom ---
    private double timeToExplode = 2.0; // Thời gian đếm ngược để nổ (giây). Điều chỉnh giá trị này.
    private double explosionTimer = 0; // Biến đếm thời gian đã trôi qua kể từ khi đặt bom
    private int flameLength; // Bán kính nổ (lấy từ Player khi đặt)
    private Player owner; // Tham chiếu đến Player đã đặt quả bom này (quan trọng cho logic sau này)

    // Trạng thái của Bom
    private boolean exploded = false; // Cờ cho biết bom đã nổ chưa
    private boolean active = true; // Cờ cho biết bom còn hoạt động không (chưa nổ và chưa biến mất)

    // --- Animation attributes ---
    // Animation đếm ngược của bom (thường nhấp nháy)
    private Animation countdownAnimation;
    // TODO: Thêm animation nổ (explosionAnimation) nếu muốn bom có hiệu ứng nổ riêng trước khi tạo Flame

    private Animation currentAnimation; // Animation đang chạy hiện tại
    private double animationTimer = 0; // Dùng để theo dõi thời gian đã trôi qua cho animation hiện tại

    // Tham chiếu đến đối tượng Map (cần cho logic nổ để kiểm tra Tile)
    private Map map;

    // --- Danh sách các đối tượng Flame được tạo ra bởi vụ nổ này ---
    private List<Flame> generatedFlames = new ArrayList<>();


    // Constructor
    public Bomb(int startGridX, int startGridY, int flameLength, Player owner, Map map) {
        this.gridX = startGridX;
        this.gridY = startGridY;
        // Đặt bom vào góc trên bên trái của ô lưới
        this.pixelX = gridX * Sprite.SCALED_SIZE;
        this.pixelY = gridY * Sprite.SCALED_SIZE;

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

        // TODO: Initialize explosion animation if needed
    }

    // --- Phương thức được gọi bởi Vòng lặp Game (AnimationTimer) ---

    // Phương thức cập nhật trạng thái mỗi frame
    public void update(double deltaTime) {
        if (!active) {
            return; // Nếu bom không còn hoạt động, không làm gì cả
        }

        // Tăng thời gian đếm ngược
        explosionTimer += deltaTime;

        // --- Cập nhật trạng thái animation ---
        if (currentAnimation != null) {
            animationTimer += deltaTime;
            // Cập nhật frame animation hiện tại (Bomb chỉ có animation đếm ngược)
            // Sprite currentSpriteFrame = currentAnimation.getFrame(animationTimer); // Lấy frame để vẽ
            // TODO: Nếu có animation nổ, chuyển animation khi nổ
        }

        // --- Kiểm tra xem đã đến lúc nổ chưa ---
        if (explosionTimer >= timeToExplode && !exploded) {
            explode(); // Kích hoạt vụ nổ
        }

        // TODO: Xử lý animation nổ và khi nào bom biến mất hoàn toàn
        // Nếu đã nổ và animation nổ đã kết thúc, đặt active = false để loại bỏ bom
        // if (exploded && explosionAnimation.isFinished(animationTimer)) {
        //    active = false;
        //    // TODO: Thông báo cho Player biết bom đã nổ để tăng lại số bom có thể đặt
        // }
    }

    // Phương thức được gọi khi bom nổ
    private void explode() {
        exploded = true;
        active = false; // Đặt active = false ngay khi nổ để Bomberman có thể xử lý

        // TODO: Chuyển sang animation nổ nếu có
        // currentAnimation = explosionAnimation;
        // animationTimer = 0; // Reset timer cho animation nổ

        System.out.println("Bomb at (" + gridX + ", " + gridY + ") exploded! Flame length: " + flameLength); // Log tạm thời

        // TODO: Phát âm thanh nổ bom

        // --- Logic tạo các đối tượng Flame lan tỏa ra các hướng ---

        // Tạo Flame ở tâm vụ nổ (vị trí của bom)
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

                // --- Tạo đối tượng Flame tại vị trí này ---
                generatedFlames.add(new Flame(nextGridX, nextGridY, currentFlameType));


                // --- Sửa lỗi: Gọi map.brickHitByFlame() thay vì map.setTile() ---
                // Nếu gặp Gạch (*), thông báo cho Map biết gạch đã bị ngọn lửa chạm vào
                if (tile != null && tile.getType() == TileType.BRICK) {
                    map.brickHitByFlame(nextGridX, nextGridY); // Gọi phương thức mới trong Map
                    // TODO: Có thể cần tạo animation gạch vỡ (nếu có) - Logic này sẽ nằm trong Bomberman
                    break; // Dừng lan tỏa sau khi gặp gạch
                }

                // Nếu gặp Portal (x), tạo ngọn lửa nhưng vẫn tiếp tục lan tỏa
                // if (tile != null && tile.getType() == TileType.PORTAL) {
                //    // Tạo Flame tại vị trí Portal
                //    generatedFlames.add(new Flame(nextGridX, nextGridY, currentFlameType)); // Portal có thể cần sprite Flame riêng?
                //    // Vẫn tiếp tục lan tỏa qua Portal
                // }

                // Nếu gặp Item (b, f, s, l), tạo ngọn lửa và vẫn tiếp tục lan tỏa
                // Logic này sẽ được xử lý khi Bomberman duyệt qua danh sách Item và kiểm tra va chạm với Flame
            }
        }

        // Sau khi tạo Flame, bom gốc (đối tượng Bomb này) sẽ biến mất sau khi animation nổ kết thúc
        // active = false; // Điều này đã được thực hiện ở đầu phương thức explode()
    }

    // Phương thức được gọi bởi Vòng lặp Game để vẽ Bom
    public void render(GraphicsContext gc) {
        if (!active) {
            return; // Không vẽ nếu bom không còn hoạt động
        }

        // --- Get current animation frame to draw ---
        Image currentImage = null;
        if (currentAnimation != null) {
            Sprite currentSpriteFrame = currentAnimation.getFrame(animationTimer);
            if (currentSpriteFrame != null) {
                currentImage = currentSpriteFrame.getFxImage();
            }
        } else {
            // Fallback
            currentImage = Sprite.bomb.getFxImage();
        }

        // Fallback cuối cùng
        if (currentImage == null) {
            currentImage = Sprite.bomb.getFxImage();
        }

        if (currentImage != null) {
            // Vẽ hình ảnh Bom tại vị trí pixel hiện tại
            // Cần điều chỉnh vị trí vẽ nếu sprite có kích thước khác Sprite.SCALED_SIZE
            // hoặc nếu bạn muốn căn giữa sprite.
            // Hiện tại đang vẽ góc trên bên trái tại pixelX, pixelY
            gc.drawImage(currentImage, pixelX, pixelY);
        }

        // TODO: Logic vẽ các hiệu ứng khác của Bom (ví dụ: hiệu ứng nổ của bom gốc)
        // Các đối tượng Flame sẽ được vẽ riêng sau này bởi Bomberman
    }

    // --- Getters ---
    public double getPixelX() { return pixelX; }
    public double getPixelY() { return pixelY; } // Phương thức getPixelY() đúng

    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public boolean isExploded() { return exploded; }
    public boolean isActive() { return active; }
    public int getFlameLength() { return flameLength; }
    public Player getOwner() { return owner; }

    // --- Getter mới để lấy danh sách Flame đã tạo ---
    public List<Flame> getGeneratedFlames() {
        return generatedFlames;
    }

    // Setter để đặt active = false khi bom biến mất hoàn toàn
    // Phương thức này giờ được gọi trong explode()
    public void setActive(boolean active) { this.active = active; }
}
