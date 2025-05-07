package com.example.bomberman.entities; // Đặt trong package entities hoặc một package riêng cho hiệu ứng

import com.example.bomberman.Bomberman;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

// Lớp đại diện cho một hiệu ứng animation tạm thời tại một vị trí cụ thể
// Ví dụ: animation gạch vỡ, animation quái vật chết...
public class TemporaryAnimation {

    private double pixelX;
    private double pixelY;
    private int gridX; // Vị trí lưới X
    private int gridY; // Vị trí lưới Y

    private Animation animation; // Animation để hiển thị
    private double animationTimer = 0; // Thời gian đã trôi qua cho animation này

    private boolean active = true; // Cờ cho biết animation còn chạy không

    // Constructor
    public TemporaryAnimation(int gridX, int gridY, Animation animation) {
        this.gridX = gridX;
        this.gridY = gridY;
        // Tính toán vị trí pixel từ vị trí lưới
        this.pixelX = gridX * Sprite.SCALED_SIZE;
        this.pixelY = gridY * Sprite.SCALED_SIZE;

        this.animation = animation;

        // Animation tạm thời thường không lặp lại
        if (animation != null && animation.isLooping()) {
            System.err.println("Warning: TemporaryAnimation created with a looping animation.");
        }
    }

    // Cập nhật trạng thái animation
    public void update(double deltaTime) {
        if (!active) {
            return;
        }

        if (animation != null) {
            animationTimer += deltaTime;
            // Kiểm tra xem animation đã kết thúc chưa
            if (animation.isFinished(animationTimer)) {
                active = false; // Đặt active = false khi animation kết thúc
            }
        } else {
            // Nếu không có animation, đặt active = false ngay lập tức
            active = false;
        }
    }

    // Vẽ frame hiện tại của animation
    public void render(GraphicsContext gc) {
        if (!active) {
            return; // Không vẽ nếu không còn active
        }

        if (animation != null) {
            Image currentImage = null;
            Sprite currentSpriteFrame = animation.getFrame(animationTimer);
            if (currentSpriteFrame != null) {
                currentImage = currentSpriteFrame.getFxImage();
            }

            if (currentImage != null) {
                // Vẽ hình ảnh animation tại vị trí pixel
                gc.drawImage(currentImage, pixelX, pixelY + Bomberman.UI_PANEL_HEIGHT);
            }
        }
    }

    // --- Getters ---
    public boolean isActive() {
        return active;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }
    // TODO: Getters khác nếu cần (ví dụ: để lấy loại animation)
}
