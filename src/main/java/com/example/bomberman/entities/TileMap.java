package com.example.bomberman.entities;

import com.example.bomberman.map.MapLoader;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.io.IOException;


public class TileMap {
    private static final int TILE_SIZE = 32;
    private char[][] map;

    public TileMap(String filePath) throws IOException {
        loadMap(filePath);
    }

    private void loadMap(String filePath) throws IOException {
        MapLoader mapLoader = new MapLoader(filePath);
        map = mapLoader.getMap();
    }

    public void draw(GraphicsContext gc) {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                switch (map[i][j]) {
                    case '#':
                        gc.setFill(Color.GRAY); // Tường
                        gc.fillRect(j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        break;
                    case '*':
                        gc.setFill(Color.RED); // Vật phẩm hoặc chướng ngại vật
                        gc.fillRect(j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        break;
                    case '1':
                    case '2':
                        gc.setFill(Color.GREEN); // Kẻ thù
                        gc.fillRect(j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        break;
                    case 'x':
                        gc.setFill(Color.YELLOW); // Bom
                        gc.fillRect(j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        break;
                    case 'f':
                        gc.setFill(Color.ORANGE); // Cửa thoát
                        gc.fillRect(j * TILE_SIZE, i * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                        break;
                    default:
                        // Không vẽ 'p' và khoảng trống (' ')
                        break;
                }
            }
        }
    }

    public int getWidth() {
        return map.length > 0 ? map[0].length : 0;
    }

    public int getHeight() {
        return map.length;
    }

    // Tìm vị trí của 'p'
    public double[] findPlayerPosition() {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                if (map[i][j] == 'p') {
                    return new double[]{j * TILE_SIZE, i * TILE_SIZE};
                }
            }
        }
        // Nếu không tìm thấy 'p', trả về vị trí mặc định
        return new double[]{TILE_SIZE, TILE_SIZE};
    }

    // Kiểm tra va chạm với tường
    public boolean isWall(double x, double y) {
        int tileX = (int) (x / TILE_SIZE);
        int tileY = (int) (y / TILE_SIZE);
        // Kiểm tra xem tọa độ có nằm ngoài bản đồ không
        if (tileX < 0 || tileX >= map[0].length || tileY < 0 || tileY >= map.length) {
            return true;
        }
        return map[tileY][tileX] == '#';
    }
}