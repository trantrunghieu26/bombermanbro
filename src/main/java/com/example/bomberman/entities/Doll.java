package com.example.bomberman.entities;

import com.example.bomberman.Bomberman;
import com.example.bomberman.Map.Map;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite;



public class Doll extends Enemy {

    // --- Cập nhật Hằng số cho Doll ---
    // Tốc độ trung bình hoặc tốc độ tham chiếu (không còn là tốc độ cố định)
    // private static final double DOLL_SPEED = 100.0; // Không dùng hằng số này làm tốc độ cố định nữa
    private static final int DOLL_SCORE = 300;

    // --- Hằng số cho tốc độ ngẫu nhiên ---
    private static final double MIN_VARIABLE_SPEED = 70.0; // Tốc độ chậm nhất
    private static final double MAX_VARIABLE_SPEED = 130.0; // Tốc độ nhanh nhất5
    // --- Hằng số cho khoảng thời gian thay đổi tốc độ ngẫu nhiên ---
    private static final double MIN_SPEED_CHANGE_INTERVAL = 1.0; // Thay đổi tốc độ ít nhất sau 1 giây
    private static final double MAX_SPEED_CHANGE_INTERVAL = 3.0; // Thay đổi tốc độ nhiều nhất sau 3 giây

    // --- Thuộc tính quản lý thời gian thay đổi tốc độ ---
    private double speedChangeTimer = 0;
    private double timeToNextSpeedChange = 0; // Thời gian cần chờ cho lần thay đổi tốc độ tiếp theo


    // (Các thuộc tính liên quan đến bom đã bị xóa trong lần sửa trước)

    /**
     * Constructor cho Doll.
     */
    public Doll(int startGridX, int startGridY, Map map, Bomberman gameManager) {
        // Gọi constructor của lớp cha (Enemy) với một tốc độ ban đầu (có thể là trung bình hoặc MIN)
        // Tốc độ thực tế sẽ được đặt ngẫu nhiên ngay sau đó.
        super(startGridX, startGridY, MIN_VARIABLE_SPEED, DOLL_SCORE, map, gameManager);

        // --- Khởi tạo Animations cho Doll ---
        // frameDuration này sẽ cố định, nên chọn giá trị trông tốt nhất với tốc độ TRUNG BÌNH (hoặc khoảng 100)
        double frameDuration = 0.05; // Giữ giá trị đã thử cho tốc độ 100

        walkLeftAnimation = new Animation(frameDuration, true, Sprite.doll_left1, Sprite.doll_left2, Sprite.doll_left3);
        walkRightAnimation = new Animation(frameDuration, true, Sprite.doll_right1, Sprite.doll_right2, Sprite.doll_right3);

        deadAnimation = new Animation(TIME_TO_DIE / 4.0, false, // Chia thời gian cho số frame chết
                Sprite.doll_dead,
                Sprite.mob_dead1,
                Sprite.mob_dead2,
                Sprite.mob_dead3);

        // --- Khởi tạo thời gian thay đổi tốc độ và tốc độ ban đầu ngẫu nhiên ---
        selectNewSpeedAndInterval(); // Chọn tốc độ và khoảng thời gian đầu tiên
        speedChangeTimer = timeToNextSpeedChange; // Đặt timer bắt đầu bằng interval đã chọn
        // Tốc độ đã được đặt trong selectNewSpeedAndInterval()

        // Animation ban đầu được đặt bởi setRandomDirection() trong Enemy constructor
    }

    /**
     * Override phương thức tính toán nước đi cho Doll.
     * Vẫn giữ logic ngẫu nhiên cơ bản.
     */
    @Override
    protected void calculateNextMove() {
        // Logic đơn giản: Nếu không di chuyển HOẶC hướng hiện tại là NONE (vừa bị chặn), chọn hướng ngẫu nhiên mới.
        // Nếu đang di chuyển, nó sẽ tiếp tục đi cho đến khi bị chặn.
        // Phương thức move() của lớp Enemy sẽ xử lý việc dừng lại khi bị chặn
        // và handleBlockedMovement() sẽ cố gắng tìm hướng thay thế hoặc gọi setRandomDirection nếu bị kẹt.
        if (!isMoving || currentDirection == Direction.NONE) {
            setRandomDirection(); // Gọi hàm của lớp Enemy để chọn hướng ngẫu nhiên khả thi

        }
        // Nếu đang di chuyển và không bị chặn, không làm gì ở đây, logic di chuyển và xử lý chặn nằm ở Enemy.move()
    }

    /**
     * Cập nhật trạng thái của Doll mỗi frame.
     * Bổ sung logic thay đổi tốc độ ngẫu nhiên theo thời gian.
     */
    @Override
    public void update(double deltaTime) {
        super.update(deltaTime); // Gọi update của Enemy để xử lý di chuyển, chết...

        if (isAlive) { // Chỉ thay đổi tốc độ khi còn sống
            // Giảm bộ đếm thời gian thay đổi tốc độ
            speedChangeTimer -= deltaTime;

            // Nếu đến lúc thay đổi tốc độ
            if (speedChangeTimer <= 0) {
                selectNewSpeedAndInterval(); // Chọn tốc độ và khoảng thời gian mới
                speedChangeTimer = timeToNextSpeedChange; // Đặt lại timer
                // System.out.println("DOLL DEBUG: Speed changed to " + this.speed + ". Next change in " + this.timeToNextSpeedChange + " seconds."); // Log
            }
        }
    }

    /**
     * Phương thức helper để chọn một tốc độ ngẫu nhiên mới và một khoảng thời gian ngẫu nhiên
     * cho lần thay đổi tốc độ tiếp theo.
     */
    private void selectNewSpeedAndInterval() {
        // Chọn tốc độ ngẫu nhiên trong phạm vi [MIN_VARIABLE_SPEED, MAX_VARIABLE_SPEED]
        this.speed = MIN_VARIABLE_SPEED + random.nextDouble() * (MAX_VARIABLE_SPEED - MIN_VARIABLE_SPEED);

        // Chọn khoảng thời gian ngẫu nhiên cho lần thay đổi tốc độ tiếp theo
        this.timeToNextSpeedChange = MIN_SPEED_CHANGE_INTERVAL + random.nextDouble() * (MAX_SPEED_CHANGE_INTERVAL - MIN_SPEED_CHANGE_INTERVAL);
    }

    // Doll sử dụng isObstacle() mặc định của Enemy, coi WALL và BOMB là vật cản.
    // render() và die() sử dụng logic chung của lớp Enemy.
    // updateGridPosition() cũng sử dụng logic chung của Entity/Enemy.
    // Getters và Setters vẫn giữ nguyên.
}