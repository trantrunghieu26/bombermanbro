package com.example.bomberman.entities;

import com.example.bomberman.Bomberman;
import com.example.bomberman.Map.Map;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite;

// Lớp Balloom - Kẻ địch đơn giản nhất
public class Balloom extends Enemy {

    // --- Hằng số cho Balloom ---
    private static final double BALLOOM_SPEED = 50.0; // Tốc độ chậm hơn Oneal
    private static final int BALLOOM_SCORE = 100;   // Điểm thấp hơn Oneal

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
        deadAnimation = new Animation(TIME_TO_DIE / 4.0, false, // Chia thời gian cho 4 frame chết
                Sprite.balloom_dead,
                Sprite.mob_dead1,
                Sprite.mob_dead2,
                Sprite.mob_dead3);

        // Animation ban đầu được quyết định bởi findInitialClearDirection() trong constructor Enemy
        // Hoặc có thể đặt mặc định ở đây nếu muốn
        // currentAnimation = walkLeftAnimation;
    }

    /**
     * Override phương thức tính toán nước đi cho Balloom.
     * Logic của Balloom rất đơn giản:
     * 1. Nếu nó không di chuyển (isMoving = false), có nghĩa là nó vừa bị chặn
     *    hoặc vừa mới bắt đầu. Trong trường hợp này, nó sẽ chọn một hướng ngẫu nhiên mới
     *    (sử dụng hàm setRandomDirection đã được cải tiến của lớp Enemy).
     * 2. Nếu nó đang di chuyển, nó sẽ KHÔNG làm gì cả trong hàm này. Nó sẽ tiếp tục
     *    đi theo hướng hiện tại cho đến khi bị chặn bởi logic trong phương thức move()
     *    của lớp Enemy. Khi bị chặn, move() sẽ đặt isMoving = false, và ở lần update
     *    tiếp theo, calculateNextMove() sẽ rơi vào trường hợp 1 để chọn hướng mới.
     *
     * (Tùy chọn: Có thể thêm một xác suất nhỏ để Balloom tự đổi hướng ngẫu nhiên ngay cả khi không bị chặn,
     *  để tránh việc nó đi qua lại mãi trên một đoạn đường dài).
     */
    @Override
    protected void calculateNextMove() {
        // Nếu Balloom không di chuyển (vừa bị chặn hoặc vừa spawn)
        if (!isMoving) {
            // Gọi hàm setRandomDirection của lớp Enemy. Hàm này đã được cải tiến để
            // chọn một hướng ngẫu nhiên mà CÓ THỂ đi được (không bị chặn ngay lập tức).
            // Nếu không có hướng nào đi được, nó sẽ đặt currentDirection = NONE.
            //System.out.println("BALLOOM DEBUG: Not moving, calling setRandomDirection."); // Log (tùy chọn)
            setRandomDirection();
        }
        // Nếu đang di chuyển (isMoving = true), không cần làm gì ở đây.
        // Cứ để nó đi tiếp theo currentDirection. Việc xử lý khi bị chặn
        // đã nằm trong phương thức move() của lớp Enemy (nó sẽ gọi handleBlockedMovement
        // và cuối cùng có thể dẫn đến isMoving = false).

        // --- Tùy chọn: Thêm xác suất đổi hướng ngẫu nhiên khi đang đi ---
        /*
        else { // Đang di chuyển
            if (random.nextDouble() < 0.01) { // Ví dụ: 1% cơ hội đổi hướng mỗi lần update
                //System.out.println("BALLOOM DEBUG: Randomly changing direction while moving."); // Log
                setRandomDirection();
            }
        }
        */
    }

    // Balloom không cần override các phương thức khác như move(), checkMovementCollision(), die(), render()
    // vì nó sử dụng trực tiếp các phương thức đã được triển khai (và cải tiến) trong lớp Enemy.
}