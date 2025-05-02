package com.example.bomberman.graphics;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.Random;

public class Particle {
    private double x, y;
    private double vy; // Tốc độ rơi
    private double size;
    private Color color;
    private double initialX; // Lưu vị trí X ban đầu để có thể tạo hiệu ứng gió nhẹ
    private double canvasWidth, canvasHeight; // Lưu kích thước canvas
    private Random random = new Random();

    public Particle(double canvasWidth, double canvasHeight) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        reset(); // Khởi tạo vị trí và thuộc tính
    }

    // Đặt lại hạt khi nó ra khỏi màn hình hoặc khi khởi tạo
    private void reset() {
        x = random.nextDouble() * canvasWidth; // Vị trí X ngẫu nhiên
        y = -random.nextDouble() * 50; // Bắt đầu từ trên màn hình một chút
        initialX = x;
        vy = 15 + random.nextDouble() * 30; // Tốc độ rơi ngẫu nhiên (pixel/giây)
        size = 1 + random.nextDouble() * 2; // Kích thước ngẫu nhiên
        // Màu bụi/tro (xám hoặc nâu nhạt với alpha ngẫu nhiên nhẹ)
        int grayTone = 100 + random.nextInt(80);
        double alpha = 0.5 + random.nextDouble() * 0.4;
        color = Color.rgb(grayTone, grayTone, grayTone, alpha);
    }

    public void update(double deltaTime, double screenHeight) {
        y += vy * deltaTime; // Cập nhật vị trí Y

        // Hiệu ứng gió nhẹ (tùy chọn)
         x = initialX + Math.sin(y * 0.01) * 10; // Lắc lư nhẹ theo Y

        // Nếu hạt rơi ra khỏi màn hình, đặt lại vị trí
        if (y > screenHeight + size) {
            reset();
            y = -size; // Đặt lại ngay trên đỉnh
        }
    }

    public void render(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillOval(x - size / 2, y - size / 2, size, size); // Vẽ hạt bụi hình tròn
    }
}
