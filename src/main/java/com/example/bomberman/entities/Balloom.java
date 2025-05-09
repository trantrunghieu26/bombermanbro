package com.example.bomberman.entities;

import com.example.bomberman.Bomberman;
import com.example.bomberman.Map.Map;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite;



public class Balloom extends Enemy {

    // --- Hằng số cho Balloom ---
    private static final double BALLOOM_SPEED = 50.0;
    private static final int BALLOOM_SCORE = 100;
    // Xác suất đổi hướng ngẫu nhiên mỗi frame, ngay cả khi không bị chặn
    private static final double RANDOM_CHANGE_PROBABILITY = 0.005; // Ví dụ: 0.5% cơ hội mỗi frame

    /**
     * Constructor cho Balloom.
     */
    public Balloom(int startGridX, int startGridY, Map map, Bomberman gameManager) {
        // Gọi constructor của lớp cha (Enemy) với các giá trị của Balloom
        super(startGridX, startGridY, BALLOOM_SPEED, BALLOOM_SCORE, map, gameManager);

        // --- Khởi tạo Animations cho Balloom ---
        double frameDuration = 0.3; // Thời gian mỗi frame animation (có thể điều chỉnh)
        // Balloom chỉ có animation trái/phải
        walkLeftAnimation = new Animation(frameDuration, true, Sprite.balloom_left1, Sprite.balloom_left2, Sprite.balloom_left3);
        walkRightAnimation = new Animation(frameDuration, true, Sprite.balloom_right1, Sprite.balloom_right2, Sprite.balloom_right3);
        // Animation chết: Bắt đầu bằng sprite balloom_dead, sau đó là các sprite mob_dead chung
        deadAnimation = new Animation(TIME_TO_DIE / 3.0, false, // Chia thời gian cho 3 frame chết (nếu dùng 3 sprite)
                Sprite.balloom_dead,
                Sprite.mob_dead1,
                Sprite.mob_dead2); // Có thể dùng 3 sprite mob_dead1-3

        // Animation ban đầu được đặt bởi setRandomDirection() trong Enemy constructor
    }

    @Override
    protected void calculateNextMove() {
        // Nếu Balloom không di chuyển (vừa bị chặn hoặc vừa spawn)
        if (!isMoving) {
            // Gọi hàm setRandomDirection của lớp Enemy. Hàm này đã được cải tiến để
            // chọn một hướng ngẫu nhiên mà CÓ THỂ đi được (không bị chặn ngay lập tức).
            setRandomDirection();
        }

        else {
            // Thêm xác suất đổi hướng ngẫu nhiên ngay cả khi đang đi
            if (random.nextDouble() < RANDOM_CHANGE_PROBABILITY) {
                setRandomDirection(); // Chọn hướng ngẫu nhiên mới (nó sẽ chọn hướng đi được)
            }

        }

    }

}