package com.example.bomberman;

import com.example.bomberman.entities.Enemy;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer; // Import AnimationTimer
import com.example.bomberman.Map.*;
import com.example.bomberman.graphics.Sprite;
import com.example.bomberman.entities.Player; // Import lớp Player
import com.example.bomberman.Input.InputHandler; // Import lớp InputHandler

import java.util.ArrayList;
import java.util.List;


public class Bomberman extends Application {

    private Map gameMap; // Giữ tham chiếu đến Map
    private Player player; // Giữ tham chiếu đến Player
    private GraphicsContext gc; // Giữ tham chiếu đến GraphicsContext
    private Canvas canvas; // Giữ tham chiếu đến Canvas

    // TODO: Thêm các danh sách quản lý thực thể khác (Enemies, Bombs, Flames, Items)
    private List<Enemy> enemies = new ArrayList<>();
    // private List<Bomb> bombs = new ArrayList<>();
    // ...

    @Override
    public void start(Stage primaryStage) {
        // --- 1. Tải bản đồ và Khởi tạo Map ---
        MapData mapData = new MapData(1); // Load bản đồ level 1
        gameMap = new Map(mapData); // Lưu vào thuộc tính của lớp

        // Tính kích thước canvas dựa vào map size
        int canvasWidth = mapData.getCols() * Sprite.SCALED_SIZE;
        int canvasHeight = mapData.getRows() * Sprite.SCALED_SIZE;

        // Tạo canvas với size đúng map
        canvas = new Canvas(canvasWidth, canvasHeight);
        gc = canvas.getGraphicsContext2D(); // Lưu vào thuộc tính của lớp

        // --- 2. Tìm vị trí bắt đầu của Player và Khởi tạo Player ---
        int playerStartX = -1, playerStartY = -1;
        char[][] charMap = mapData.getMap();
        for (int i = 0; i < mapData.getRows(); i++) {
            for (int j = 0; j < mapData.getCols(); j++) {
                if (charMap[i][j] == 'p') {
                    playerStartX = j; // Cột là X
                    playerStartY = i; // Hàng là Y
                    // TODO: Sau khi tìm thấy 'p', bạn cũng cần tạo các thực thể khác ('1', '2', 'b', ...) ở đây
                    // và thêm chúng vào các danh sách quản lý thực thể tương ứng.
                } else if (charMap[i][j] == '1') {
                    Enemy e = new Enemy(j, i, gameMap);
                    this.enemies.add(e);
                }
            }
        }

        if (playerStartX != -1 && playerStartY != -1) {
            player = new Player(playerStartX, playerStartY, gameMap); // Tạo đối tượng Player
        } else {
            System.err.println("Error: Player start position ('p') not found in map data!");
            // Xử lý lỗi: thoát ứng dụng, hiển thị thông báo, ...
            return; // Không thể tiếp tục nếu không có Player
        }


        // --- 3. Thiết lập Scene, Stage và InputHandler ---
        Group root = new Group(canvas);
        Scene scene = new Scene(root); // Tạo Scene

        // Tạo InputHandler và gắn nó vào Scene và Player
        InputHandler inputHandler = new InputHandler(scene, player);


        // Cài đặt window
        primaryStage.setTitle("Bomberman Game - Level " + mapData.getLevel());
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // Không cho resize để tránh lỗi canvas
        primaryStage.show();

        // --- 4. Bắt đầu Vòng lặp Game (AnimationTimer) ---
        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdateTime = 0; // Thời điểm của lần cập nhật trước (nanoseconds)

            @Override
            public void handle(long now) { // now là thời điểm hiện tại (nanoseconds)
                if (lastUpdateTime == 0) {
                    lastUpdateTime = now;
                    return; // Bỏ qua frame đầu tiên để tránh deltaTime lớn
                }

                // Tính toán thời gian trôi qua giữa các frame (đơn vị: giây)
                double deltaTime = (now - lastUpdateTime) / 1_000_000_000.0;
                lastUpdateTime = now;

                // --- Vòng lặp Update ---
                // Cập nhật trạng thái của tất cả các thực thể
                player.update(deltaTime);
                for (Enemy e : enemies) {
                    e.update(deltaTime);
                }
                // TODO: Gọi update cho các thực thể khác (enemies, bombs, flames, items)

                // --- Vòng lặp Render ---
                // Xóa toàn bộ Canvas trước khi vẽ lại
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

                // Vẽ bản đồ nền
                gameMap.render(gc);

                // Vẽ các thực thể động (Player, Enemies, Bombs, Flames, Items) SAU khi vẽ map
                player.render(gc);
                for (Enemy e : enemies) {
                    e.render(gc);
                }
                // TODO: Gọi render cho các thực thể khác

                // TODO: Vẽ các yếu tố UI (điểm số, thời gian, ...)
            }
        };

        timer.start(); // Bắt đầu vòng lặp game
    }

    public static void main(String[] args) {
        launch(args);
    }
}
