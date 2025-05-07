package com.example.bomberman.entities;

import com.example.bomberman.Bomberman;
import com.example.bomberman.Map.Map;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite;

import java.util.Random;

// Lớp Balloom - Kẻ địch đơn giản nhất
public class Balloom extends Enemy {

    // --- Hằng số cho Balloom ---
    private static final double BALLOOM_SPEED = 50.0; // Tốc độ chậm hơn Oneal
    private static final int BALLOOM_SCORE = 100;   // Điểm thấp hơn Oneal
    // Xác suất đổi hướng ngẫu nhiên mỗi frame, ngay cả khi không bị chặn
    private static final double RANDOM_CHANGE_PROBABILITY = 0.005; // Ví dụ: 0.5% cơ hội mỗi frame

    /**
     * Constructor cho Balloom.
     */
    public Balloom(int startGridX, int startGridY, Map map, Bomberman gameManager) {
        // Gọi constructor của lớp cha (Enemy) với các giá trị của Balloom
        super(startGridX, startGridY, BALLOOM_SPEED, BALLOOM_SCORE, map, gameManager);

        // --- Khởi tạo Animations cho Balloom ---
        double frameDuration = 0.25; // Thời gian mỗi frame animation (có thể điều chỉnh)
        // Balloom chỉ có animation trái/phải
        walkLeftAnimation = new Animation(frameDuration, true, Sprite.balloom_left1, Sprite.balloom_left2, Sprite.balloom_left3);
        walkRightAnimation = new Animation(frameDuration, true, Sprite.balloom_right1, Sprite.balloom_right2, Sprite.balloom_right3);
        // Animation chết: Bắt đầu bằng sprite balloom_dead, sau đó là các sprite mob_dead chung
        deadAnimation = new Animation(TIME_TO_DIE / 3.0, false, // Chia thời gian cho 3 frame chết (nếu dùng 3 sprite)
                Sprite.balloom_dead,
                Sprite.mob_dead1,
                Sprite.mob_dead2); // Có thể dùng 3 sprite mob_dead1-3

        // Animation ban đầu được đặt bởi setRandomDirection() trong Enemy constructor
        // currentAnimation = walkLeftAnimation; // Không cần đặt ở đây nếu Enemy constructor gọi setRandomDirection()
    }

    /**
     * Override phương thức tính toán nước đi cho Balloom.
     * Logic của Balloom:
     * 1. Nếu nó không di chuyển (vừa bị chặn hoặc mới bắt đầu), nó chọn hướng ngẫu nhiên khả thi.
     * 2. Nếu nó đang di chuyển, có một xác suất nhỏ để nó tự đổi hướng ngẫu nhiên.
     *    Nếu không đổi hướng ngẫu nhiên, nó sẽ tiếp tục đi theo hướng hiện tại.
     */
    @Override
    protected void calculateNextMove() {
        // Nếu Balloom không di chuyển (vừa bị chặn hoặc vừa spawn)
        if (!isMoving) {
            // Gọi hàm setRandomDirection của lớp Enemy. Hàm này đã được cải tiến để
            // chọn một hướng ngẫu nhiên mà CÓ THỂ đi được (không bị chặn ngay lập tức).
            setRandomDirection();
            // System.out.println("BALLOOM DEBUG: Not moving, called setRandomDirection. New direction: " + currentDirection); // Log
        }
        // Nếu đang di chuyển (isMoving = true)
        else {
            // Thêm xác suất đổi hướng ngẫu nhiên ngay cả khi đang đi
            if (random.nextDouble() < RANDOM_CHANGE_PROBABILITY) {
                //System.out.println("BALLOOM DEBUG: Randomly changing direction while moving."); // Log
                setRandomDirection(); // Chọn hướng ngẫu nhiên mới (nó sẽ chọn hướng đi được)
            }
            // Nếu không đổi hướng ngẫu nhiên, nó sẽ tiếp tục đi theo currentDirection
            // Việc xử lý khi bị chặn sẽ nằm trong phương thức move() và handleBlockedMovement()
        }
        // Note: currentDirection và isMoving đã được set bởi setRandomDirection()
        // hoặc giữ nguyên từ frame trước. move() sẽ sử dụng các giá trị này.
    }

    // Balloom không cần override các phương thức khác như move(), checkMovementCollision(), die(), render()
    // vì nó sử dụng trực tiếp các phương thức đã được triển khai (và cải tiến) trong lớp Enemy.
    // isObstacle() của Enemy base class là đủ cho Balloom.
}