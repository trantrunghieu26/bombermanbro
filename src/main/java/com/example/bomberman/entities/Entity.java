package com.example.bomberman.entities;

import com.example.bomberman.graphics.Sprite; // Import Sprite để sử dụng SCALED_SIZE
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image; // Import Image

// Lớp trừu tượng Entity là lớp cơ sở cho tất cả các thực thể trong game
// Các thực thể như Player, Enemy, Bomb, Flame, Item sẽ kế thừa từ lớp này.
public abstract class Entity {

    // --- Thuộc tính vị trí và kích thước ---
    // Tọa độ pixel thực trên màn hình
    protected double x;
    protected double y;

    // Tọa độ lưới (grid) trên bản đồ
    protected int gridX;
    protected int gridY;

    // Kích thước của thực thể (thường bằng kích thước của Sprite)
    protected int width = Sprite.SCALED_SIZE;
    protected int height = Sprite.SCALED_SIZE;

    // --- Thuộc tính trạng thái ---
    // Trạng thái hoạt động của thực thể. Nếu false, thực thể sẽ bị xóa khỏi danh sách quản lý.
    protected boolean active = true;

    // --- Hình ảnh/Sprite của thực thể ---
    protected Image img;

    // Constructor
    // Khởi tạo thực thể tại vị trí lưới (gridX, gridY) với một hình ảnh ban đầu
    public Entity(int gridX, int gridY, Image img) {
        this.gridX = gridX;
        this.gridY = gridY;
        // Tính toán tọa độ pixel dựa trên tọa độ lưới
        this.x = gridX * Sprite.SCALED_SIZE;
        this.y = gridY * Sprite.SCALED_SIZE;
        this.img = img;
    }

    // --- Phương thức trừu tượng update ---
    // Tất cả các lớp con phải triển khai phương thức này để cập nhật trạng thái của chúng theo thời gian.
    public abstract void update(double deltaTime);

    // --- Phương thức trừu tượng render ---
    // Tất cả các lớp con phải triển khai phương thức này để vẽ chúng lên màn hình.
    public abstract void render(GraphicsContext gc);

    // --- Các phương thức Getter ---
    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isActive() {
        return active;
    }

    // --- Các phương thức Setter ---
    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    // Phương thức để đặt trạng thái hoạt động
    public void setActive(boolean active) {
        this.active = active;
    }

    // TODO: Thêm các phương thức chung khác nếu cần (ví dụ: getBoundingBox() để lấy hộp va chạm)
    // public Rectangle2D getBoundingBox() {
    //     return new Rectangle2D(x, y, width, height);
    // }
}
