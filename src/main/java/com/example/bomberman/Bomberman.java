package com.example.bomberman;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer; // Import AnimationTimer


public class Bomberman extends Application {

    @Override
    public void start(Stage primaryStage) {

        Controller con = new Controller();
        con.controllerActive();
        
        // Cài đặt window
        primaryStage.setTitle("Bomberman Game - Level " + con.mapData.getLevel());
        primaryStage.setScene(con.scene);
        primaryStage.setResizable(false); // Không cho resize để tránh lỗi canvas
        primaryStage.show();

        // --- 4. Bắt đầu Vòng lặp Game (AnimationTimer) ---
        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdateTime = 0; // Thời điểm của lần cập nhật trước (nanoseconds)

            @Override
            public void handle(long now) {
                if (lastUpdateTime == 0) {
                    lastUpdateTime = now;
                    return; // Bỏ qua frame đầu tiên để tránh deltaTime lớn
                }

                // Tính toán thời gian trôi qua giữa các frame (đơn vị: giây)
                double deltaTime = (now - lastUpdateTime) / 1_000_000_000.0;
                lastUpdateTime = now;

                // --- Vòng lặp Update ---
                con.updateForAll(deltaTime);

                // *** KIỂM TRA VA CHẠM SAU KHI CẬP NHẬT VỊ TRÍ CỦA TẤT CẢ THỰC THỂ ***
                con.checkCollisions();


                // --- Loại bỏ các thực thể đã đánh dấu removed (nếu cần thêm logic) ---
                // removeEntities(); // Hiện tại đã remove trực tiếp trong Iterator

                // TODO: Kiểm tra điều kiện thắng/thua game

                con.renderForAll();
            }
        };

        timer.start(); // Bắt đầu vòng lặp game
    }

    public static void main(String[] args) {
        launch(args);
    }
}
