package com.example.bomberman.entities;

import com.example.bomberman.graphics.Sprite; // Cần import Sprite
import com.example.bomberman.graphics.Animation; // Cần import Animation
import javafx.scene.canvas.GraphicsContext; // Cần GraphicsContext để vẽ
import javafx.scene.image.Image; // Cần Image để vẽ

// Lớp đại diện cho một phần của ngọn lửa vụ nổ
public class Flame {

    // Vị trí theo Pixel
    private double pixelX;
    private double pixelY;

    // Vị trí theo lưới
    private int gridX; // Cột
    private int gridY; // Hàng

    // --- Thuộc tính Flame ---
    // Loại ngọn lửa (tâm, ngang, dọc, cuối...)
    // Sử dụng các giá trị số để đại diện cho loại ngọn lửa
    // 0 = center, 1 = horizontal, 2 = vertical, 3 = end_left, 4 = end_right, 5 = end_up, 6 = end_down
    private int flameType;

    // Thời gian tồn tại của ngọn lửa (giây). Điều chỉnh giá trị này.
    private double timeToLive = 0.5; // Ngọn lửa tồn tại trong 0.5 giây
    private double liveTimer = 0; // Biến đếm thời gian đã trôi qua kể từ khi tạo

    // Trạng thái hoạt động của ngọn lửa
    private boolean active = true; // Cờ cho biết ngọn lửa còn hoạt động không

    // --- Animation attributes ---
    // Animation cho từng loại ngọn lửa
    private Animation centerAnimation;
    private Animation horizontalAnimation;
    private Animation verticalAnimation;
    private Animation endLeftAnimation;
    private Animation endRightAnimation;
    private Animation endUpAnimation;
    private Animation endDownAnimation;

    private Animation currentAnimation; // Animation đang chạy hiện tại
    private double animationTimer = 0; // Dùng để theo dõi thời gian đã trôi qua cho animation hiện tại

    // Constructor
    public Flame(int startGridX, int startGridY, int flameType) {
        this.gridX = startGridX;
        this.gridY = startGridY;
        // Đặt Flame vào góc trên bên trái của ô lưới
        this.pixelX = startGridX * Sprite.SCALED_SIZE;
        this.pixelY = startGridY * Sprite.SCALED_SIZE;

        this.flameType = flameType;

        // --- Initialize animations ---
        // Thời gian hiển thị mỗi frame cho animation ngọn lửa
        double frameDuration = 0.1; // Điều chỉnh giá trị này
        boolean loop = false; // Animation ngọn lửa không lặp lại, chỉ chạy một lần rồi biến mất

        // Sử dụng các sprite từ Sprite.java của bạn
        centerAnimation = new Animation(frameDuration, loop, Sprite.bomb_exploded, Sprite.bomb_exploded1, Sprite.bomb_exploded2);
        horizontalAnimation = new Animation(frameDuration, loop, Sprite.explosion_horizontal, Sprite.explosion_horizontal1, Sprite.explosion_horizontal2);
        verticalAnimation = new Animation(frameDuration, loop, Sprite.explosion_vertical, Sprite.explosion_vertical1, Sprite.explosion_vertical2);
        endLeftAnimation = new Animation(frameDuration, loop, Sprite.explosion_horizontal_left_last, Sprite.explosion_horizontal_left_last1, Sprite.explosion_horizontal_left_last2);
        endRightAnimation = new Animation(frameDuration, loop, Sprite.explosion_horizontal_right_last, Sprite.explosion_horizontal_right_last1, Sprite.explosion_horizontal_right_last2);
        endUpAnimation = new Animation(frameDuration, loop, Sprite.explosion_vertical_top_last, Sprite.explosion_vertical_top_last1, Sprite.explosion_vertical_top_last2);
        endDownAnimation = new Animation(frameDuration, loop, Sprite.explosion_vertical_down_last, Sprite.explosion_vertical_down_last1, Sprite.explosion_vertical_down_last2);


        // Chọn animation ban đầu dựa trên loại ngọn lửa
        setAnimationBasedOnType();

        // TODO: Phát âm thanh ngọn lửa (nếu muốn âm thanh riêng cho từng phần)
    }

    // Phương thức chọn animation dựa trên flameType
    private void setAnimationBasedOnType() {
        switch (flameType) {
            case 0: currentAnimation = centerAnimation; break; // Center
            case 1: currentAnimation = horizontalAnimation; break; // Horizontal
            case 2: currentAnimation = verticalAnimation; break; // Vertical
            case 3: currentAnimation = endLeftAnimation; break; // End Left
            case 4: currentAnimation = endRightAnimation; break; // End Right
            case 5: currentAnimation = endUpAnimation; break; // End Up
            case 6: currentAnimation = endDownAnimation; break; // End Down
            default: currentAnimation = centerAnimation; // Mặc định là tâm
        }
    }


    // --- Phương thức được gọi bởi Vòng lặp Game (AnimationTimer) ---

    // Phương thức cập nhật trạng thái mỗi frame
    public void update(double deltaTime) {
        if (!active) {
            return; // Nếu ngọn lửa không còn hoạt động, không làm gì cả
        }

        // Tăng thời gian tồn tại
        liveTimer += deltaTime;

        // --- Cập nhật trạng thái animation ---
        if (currentAnimation != null) {
            animationTimer += deltaTime;
            // Kiểm tra xem animation đã kết thúc chưa (vì animation ngọn lửa không lặp)
            if (currentAnimation.isFinished(animationTimer)) {
                active = false; // Đặt active = false khi animation kết thúc
            }
        } else {
            // Nếu không có animation, ngọn lửa sẽ biến mất sau timeToLive
            if (liveTimer >= timeToLive) {
                active = false;
            }
        }

        // TODO: Kiểm tra va chạm với các thực thể khác (Player, Enemy, Bomb, Item)
        // Logic này có thể ở đây hoặc ở lớp quản lý game (Bomberman)
        // if (active) { // Chỉ kiểm tra va chạm nếu ngọn lửa còn active
        //    checkCollisionWithEntities(); // Cần phương thức này
        // }
    }

    // TODO: Phương thức kiểm tra va chạm với các thực thể khác
    // private void checkCollisionWithEntities() {
    //     // Cần truy cập danh sách các thực thể từ Bomberman
    //     // Duyệt qua danh sách Player, Enemies, Items, Bombs
    //     // Kiểm tra xem hộp va chạm của Flame có giao với hộp va chạm của thực thể khác không
    //     // Nếu có va chạm, xử lý tương tác (ví dụ: gọi entity.takeDamage(), entity.explode() cho bomb...)
    // }


    // Phương thức được gọi bởi Vòng lặp Game để vẽ Flame
    public void render(GraphicsContext gc) {
        if (!active) {
            return; // Không vẽ nếu ngọn lửa không còn hoạt động
        }

        // --- Get current animation frame to draw ---
        Image currentImage = null;
        if (currentAnimation != null) {
            Sprite currentSpriteFrame = currentAnimation.getFrame(animationTimer);
            if (currentSpriteFrame != null) {
                currentImage = currentSpriteFrame.getFxImage();
            }
        } else {
            // Fallback: Nếu không có animation, dùng sprite mặc định (ví dụ: tâm nổ)
            currentImage = Sprite.bomb_exploded.getFxImage();
        }

        // Fallback cuối cùng
        if (currentImage == null) {
            currentImage = Sprite.bomb_exploded.getFxImage();
        }


        if (currentImage != null) {
            // Vẽ hình ảnh Flame tại vị trí pixel hiện tại
            // Cần điều chỉnh vị trí vẽ nếu sprite có kích thước khác Sprite.SCALED_SIZE
            // hoặc nếu bạn muốn căn giữa sprite.
            // Hiện tại đang vẽ góc trên bên trái tại pixelX, pixelY
            gc.drawImage(currentImage, pixelX, pixelY);
        }

        // TODO: Logic vẽ các hiệu ứng khác của Flame
    }

    // --- Getters ---
    public double getPixelX() { return pixelX; }
    public double getPixelY() { return pixelY; }
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }
    public boolean isActive() { return active; }
    public int getFlameType() { return flameType; }
    // TODO: Getters cho các thuộc tính khác

    // TODO: Setter để đặt active = false (ví dụ khi va chạm với thực thể khác cần xóa ngay)
    // public void setActive(boolean active) { this.active = active; }
}
