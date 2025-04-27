package com.example.bomberman.entities.Items; // Đặt trong package entities

import com.example.bomberman.entities.Player;
import com.example.bomberman.graphics.Sprite;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

// Lớp cơ sở ABSTRACT cho tất cả các loại vật phẩm trong game
// Không thể tạo đối tượng trực tiếp từ lớp này
public abstract class Item {

    protected double pixelX;
    protected double pixelY;
    protected int gridX; // Vị trí lưới X
    protected int gridY; // Vị trí lưới Y

    protected Sprite sprite; // Sprite để vẽ vật phẩm
    protected boolean active = true; // Trạng thái hoạt động (có còn tồn tại trên bản đồ không)

    // TODO: Thêm animation cho Item nếu cần (ví dụ: nhấp nháy)
    // protected Animation animation;
    // protected double animationTimer = 0;


    // Constructor
    public Item(int gridX, int gridY, Sprite sprite) {
        this.gridX = gridX;
        this.gridY = gridY;
        // Tính toán vị trí pixel từ vị trí lưới
        this.pixelX = gridX * Sprite.SCALED_SIZE;
        this.pixelY = gridY * Sprite.SCALED_SIZE;

        this.sprite = sprite; // Gán sprite cho vật phẩm

        // TODO: Khởi tạo animation nếu có
        // if (this.sprite != null) {
        //    this.animation = new Animation(...); // Tạo animation từ sprite
        // }
    }

    // Phương thức cập nhật trạng thái (ví dụ: animation, kiểm tra hết hạn)
    // Phương thức này có thể có triển khai mặc định hoặc là abstract
    // Hiện tại giữ là concrete để các lớp con có thể gọi super.update()
    public void update(double deltaTime) {
        if (!active) {
            return;
        }

        // TODO: Cập nhật animation timer
        // if (animation != null) {
        //    animationTimer += deltaTime;
        //    // TODO: Kiểm tra khi animation kết thúc nếu Item có thời gian tồn tại giới hạn
        // }

        // TODO: Logic kiểm tra va chạm với Player (thường xử lý ở Bomberman)
    }

    // Phương thức vẽ vật phẩm
    // Phương thức này có thể có triển khai mặc định hoặc là abstract
    // Hiện tại giữ là concrete để các lớp con có thể gọi super.render()
    public void render(GraphicsContext gc) {
        if (!active) {
            return; // Không vẽ nếu không còn active
        }

        Image currentImage = null;
        // TODO: Lấy frame hiện tại từ animation nếu có
        // if (animation != null) {
        //    Sprite currentSpriteFrame = animation.getFrame(animationTimer);
        //    if (currentSpriteFrame != null) {
        //       currentImage = currentSpriteFrame.getFxImage();
        //    }
        // }

        // Nếu không có animation hoặc animation chưa sẵn sàng, dùng sprite mặc định
        if (currentImage == null && sprite != null) {
            currentImage = sprite.getFxImage();
        }

        // Vẽ hình ảnh vật phẩm tại vị trí pixel
        if (currentImage != null) {
            // Cần điều chỉnh vị trí vẽ nếu sprite có kích thước khác Sprite.SCALED_SIZE
            // hoặc nếu bạn muốn căn giữa sprite.
            // Hiện tại đang vẽ góc trên bên trái tại pixelX, pixelY
            gc.drawImage(currentImage, pixelX, pixelY);
        }
    }

    // --- Phương thức áp dụng hiệu ứng khi Player nhặt được Item ---
    // Phương thức này là ABSTRACT - BẮT BUỘC các lớp con phải triển khai
    public abstract void applyEffect(Player player);


    // --- Getters ---
    public double getPixelX() { return pixelX; }
    public double getPixelY() { return pixelY; }
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public boolean isActive() { return active; }
    // TODO: Getters khác

    // --- Setters ---
    public void setActive(boolean active) { this.active = active; }
    // TODO: Setters khác
}
