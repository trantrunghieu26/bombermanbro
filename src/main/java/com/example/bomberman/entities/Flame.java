package com.example.bomberman.entities;

import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite; // Import lớp Sprite
import javafx.scene.canvas.GraphicsContext;

// Lớp đại diện cho một phần của ngọn lửa bom nổ
public class Flame extends Entity {

    // Enum để định nghĩa các loại ngọn lửa khác nhau
    public enum FlameType {
        CENTER,        // Tâm vụ nổ
        HORIZONTAL,    // Đoạn ngang
        VERTICAL,    // Đoạn dọc
        HORIZONTAL_END_RIGHT, // Đầu mút ngang phải
        HORIZONTAL_END_LEFT,  // Đầu mút ngang trái
        VERTICAL_END_DOWN,    // Đầu mút dọc dưới
        VERTICAL_END_UP       // *** SỬA TÊN Ở ĐÂY: Đầu mút dọc trên ***
    }

    private FlameType type;
    private Animation flameAnimation;
    private double animationTime = 0; // Thời gian đã trôi qua cho animation lửa
    private double lifespan = 0.5; // Thời gian ngọn lửa tồn tại (giây). Điều chỉnh giá trị này.

    // Constructor
    public Flame(int gridX, int gridY, FlameType type) {
        // Vị trí dựa trên ô lưới, sprite ban đầu sẽ được set trong setAnimation
        super(gridX, gridY, Sprite.nul); // sprite ban đầu có thể là null hoặc một sprite mặc định
        this.type = type;

        // Thiết lập animation dựa trên loại ngọn lửa
        setAnimationForType(type);

        // Đảm bảo sprite ban đầu được gán sau khi animation được set
        if (flameAnimation != null) {
            this.sprite = flameAnimation.getFrame(0); // Lấy frame đầu tiên
            // Cập nhật lifespan dựa trên animationDuration nếu animation không lặp
            if (!flameAnimation.isLooping()) { // Cần getter isLooping trong Animation.java
                this.lifespan = flameAnimation.getTotalDuration();
            }
        } else {
            // Fallback nếu không có animation
            System.err.println("Warning: No animation set for FlameType " + type);
            this.sprite = Sprite.bomb_exploded; // Sử dụng sprite nổ bom mặc định
            this.lifespan = 0.5; // Lifespan mặc định nếu không có animation
        }
    }

    // Phương thức helper để thiết lập animation dựa trên loại ngọn lửa
    private void setAnimationForType(FlameType type) {
        double frameDuration = 0.1; // Thời gian hiển thị mỗi frame lửa (giây)
        // Các animation lửa thường không lặp lại
        boolean loop = false;

        switch (type) {
            case CENTER:
                flameAnimation = new Animation(frameDuration, loop,
                        Sprite.bomb_exploded, Sprite.bomb_exploded1, Sprite.bomb_exploded2);
                break;
            case HORIZONTAL:
                flameAnimation = new Animation(frameDuration, loop,
                        Sprite.explosion_horizontal, Sprite.explosion_horizontal1, Sprite.explosion_horizontal2);
                break;
            case VERTICAL:
                flameAnimation = new Animation(frameDuration, loop,
                        Sprite.explosion_vertical, Sprite.explosion_vertical1, Sprite.explosion_vertical2);
                break;
            // *** SỬA ĐỔI: Sử dụng các sprite ĐÃ CÓ SẴN trong Sprite.java cho đầu mút ***
            case HORIZONTAL_END_RIGHT:
                flameAnimation = new Animation(frameDuration, loop,
                        Sprite.explosion_horizontal_right_last, Sprite.explosion_horizontal_right_last1, Sprite.explosion_horizontal_right_last2);
                break;
            case HORIZONTAL_END_LEFT:
                flameAnimation = new Animation(frameDuration, loop,
                        Sprite.explosion_horizontal_left_last, Sprite.explosion_horizontal_left_last1, Sprite.explosion_horizontal_left_last2);
                break;
            case VERTICAL_END_DOWN:
                flameAnimation = new Animation(frameDuration, loop,
                        Sprite.explosion_vertical_down_last, Sprite.explosion_vertical_down_last1, Sprite.explosion_vertical_down_last2);
                break;
            case VERTICAL_END_UP:
                // *** SỬA TÊN SPRITE TỪ _up_last THÀNH _top_last ***
                flameAnimation = new Animation(frameDuration, loop,
                        Sprite.explosion_vertical_top_last, Sprite.explosion_vertical_top_last1, Sprite.explosion_vertical_top_last2);
                break;
            default:
                flameAnimation = null; // Không có animation cho loại không xác định
                break;
        }
        // Cập nhật lifespan của lửa dựa trên animationDuration nếu animation không lặp
        if (flameAnimation != null && !loop) { // Kiểm tra !looping() thay vì !loop
            this.lifespan = flameAnimation.getTotalDuration();
        } else if (flameAnimation != null && loop) {
            // Nếu animation lặp (trường hợp hiếm cho lửa nổ), đặt lifespan cố định
            this.lifespan = 0.5; // Giá trị mặc định hoặc điều chỉnh
        } else {
            this.lifespan = 0.5; // Lifespan mặc định nếu không có animation
        }
    }


    // Phương thức update được gọi mỗi frame
    public void update(double deltaTime) {
        // Tích lũy thời gian animation
        animationTime += deltaTime;

        // Cập nhật sprite dựa trên thời gian animation
        if (flameAnimation != null) {
            this.sprite = flameAnimation.getFrame(animationTime);
        }

        // Kiểm tra nếu thời gian tồn tại đã hết
        if (animationTime >= lifespan) {
            remove(); // Đánh dấu ngọn lửa cần được loại bỏ
        }

        // TODO: Xử lý va chạm của ngọn lửa với các thực thể khác (Player, Enemy, Brick, Item)
        // Logic này có thể ở đây hoặc ở lớp quản lý game state / va chạm
    }

    // Phương thức render được gọi mỗi frame để vẽ ngọn lửa
    @Override
    public void render(GraphicsContext gc) {
        // Chỉ vẽ nếu có sprite và chưa bị đánh dấu loại bỏ
        // Lưu ý: Sau khi remove(), isRemoved sẽ true, không cần kiểm tra sprite null nữa ở đây
        if (!removed) {
            // Đảm bảo có sprite để vẽ
            if (sprite != null) {
                // Vẽ Sprite hiện tại của ngọn lửa
                // Đảm bảo lửa được vẽ tại vị trí pixel chính xác tương ứng với ô lưới
                // pixelX và pixelY đã được tính trong constructor Entity
                gc.drawImage(this.sprite.getFxImage(), pixelX, pixelY);
            } else {
                System.err.println("Warning: Flame entity without sprite at (" + gridX + ", " + gridY + ")");
            }
        }
    }

    // --- Getters ---
    public FlameType getType() {
        return type;
    }
    // Cần các getter cho vị trí lưới để kiểm tra va chạm
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }


    // TODO: Getters khác nếu cần (ví dụ: trạng thái active)
}