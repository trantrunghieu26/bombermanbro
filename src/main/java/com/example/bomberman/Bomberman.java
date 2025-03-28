package com.example.bomberman;

import com.example.bomberman.core.GameLoop;
import com.example.bomberman.entities.Player;
import com.example.bomberman.input.InputHandler;
import com.example.bomberman.entities.TileMap;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Bomberman extends Application {
    private static final int WIDTH = 992;  // 31 ô * 32 pixel
    private static final int HEIGHT = 416; // 13 ô * 32 pixel

    @Override
    public void start(Stage primaryStage) {
        try {
            // Khởi tạo bản đồ với đường dẫn tương đối
            TileMap tileMap = new TileMap("src/main/resources/com/example/bomberman/map.txt");

            // Lấy vị trí của 'p' từ TileMap để khởi tạo Player
            double[] playerPosition = tileMap.findPlayerPosition();
            Player player = new Player(playerPosition[0], playerPosition[1], tileMap);

            // Thiết lập canvas và graphics context
            Canvas canvas = new Canvas(WIDTH, HEIGHT);
            GraphicsContext gc = canvas.getGraphicsContext2D();

            // Tạo scene
            StackPane root = new StackPane(canvas);
            Scene scene = new Scene(root, WIDTH, HEIGHT);

            // Thiết lập xử lý đầu vào và vòng lặp game
            InputHandler inputHandler = new InputHandler(scene, player);
            GameLoop gameLoop = new GameLoop(gc, player, tileMap);
            gameLoop.start();

            // Thiết lập stage
            primaryStage.setScene(scene);
            primaryStage.setTitle("Bomberman Game");
            primaryStage.setResizable(false);
            primaryStage.show();

            // Dừng game khi đóng cửa sổ
            primaryStage.setOnCloseRequest(event -> gameLoop.stop());
        } catch (IOException e) {
            System.err.println("Lỗi khi tải bản đồ: " + e.getMessage());
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR,
                    "Không thể tải file bản đồ: " + e.getMessage() +
                            "\nĐảm bảo file 'map.txt' tồn tại tại đường dẫn: src/main/resources/com/example/bomberman/map.txt"
            );
            alert.showAndWait();
            primaryStage.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}