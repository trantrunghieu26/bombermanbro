package com.example.bomberman.entities; // Đặt trong package entities

import com.example.bomberman.Map.Map; // Cần import lớp Map cho logic nổ sau này
import com.example.bomberman.graphics.Sprite; // Cần import lớp Sprite
import com.example.bomberman.graphics.Animation; // Cần import lớp Animation
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image; // Cần Image để vẽ

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
        // TODO: Chuyển sang animation nổ nếu có
        // currentAnimation = explosionAnimation;
        // animationTimer = 0; // Reset timer cho animation nổ

        // TODO: Logic tạo các đối tượng Flame lan tỏa ra các hướng
        // Cần truy cập Map để kiểm tra các Tile trên đường lan tỏa (Wall, Brick)
        // Cần truy cập danh sách các thực thể khác (Player, Enemy, Bomb, Item) để kiểm tra va chạm
        System.out.println("Bomb at (" + gridX + ", " + gridY + ") exploded!"); // Log tạm thời

        // TODO: Phát âm thanh nổ bom

        // Sau khi tạo Flame, bom gốc (đối tượng Bomb này) sẽ biến mất sau khi animation nổ kết thúc
        // active = false; // Điều này sẽ được xử lý sau khi animation nổ kết thúc
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

        // TODO: Logic vẽ các hiệu ứng khác của Bom (ví dụ: hiệu ứng nổ)
        // Các đối tượng Flame sẽ được vẽ riêng sau này
    }

    // --- Getters ---
    public double getPixelX() { return pixelX; }
    public double getPixelY() { return pixelY; }
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public boolean isExploded() { return exploded; }
    public boolean isActive() { return active; }
    public int getFlameLength() { return flameLength; }
    public Player getOwner() { return owner; }
    // TODO: Getters cho các thuộc tính khác

    // TODO: Setter để đặt active = false khi bom biến mất hoàn toàn
    // public void setActive(boolean active) { this.active = active; }
}
