package com.example.bomberman.Map;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import com.example.bomberman.graphics.Sprite;
import com.example.bomberman.Bomberman; // Import lớp Bomberman

public class Map {
    private MapData mapData;
    private Tile[][] tileGrid;
    private Bomberman gameManager; // Thêm tham chiếu đến Bomberman

    // --- Constructor đã sửa đổi: Nhận thêm tham chiếu đến Bomberman ---
    public Map(MapData mapData, Bomberman gameManager) {
        this.mapData = mapData;
        this.gameManager = gameManager; // Lưu tham chiếu
        initializeTiles();
    }

    private void initializeTiles() {
        int rows = mapData.getRows();
        int cols = mapData.getCols();
        char[][] charMap = mapData.getMap();

        tileGrid = new Tile[rows][cols];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char mapChar = ' ';

                // Thêm kiểm tra biên khi truy cập charMap
                if (i < charMap.length && charMap[i] != null && j < charMap[i].length) {
                    mapChar = charMap[i][j];
                } else {
                    System.err.println("Warning: Map data mismatch or unexpected data at row " + i + ", col " + j + ". Using default Grass tile.");
                }

                // Kiểm tra biên khi gán vào tileGrid (mặc dù vòng lặp đảm bảo)
                if (i < tileGrid.length && tileGrid[i] != null && j < tileGrid[i].length) {
                    // Sử dụng Tile.createTileFromChar để tạo Tile từ ký tự
                    tileGrid[i][j] = Tile.createTileFromChar(j, i, mapChar);
                } else {
                    System.err.println("Severe Error: tileGrid initialization logic error at row " + i + ", col " + j + ".");
                }
            }
        }
    }

    public Tile getTile(int gridX, int gridY) {
        // Kiểm tra biên khi truy cập bản đồ dựa trên kích thước khai báo
        if (gridX >= 0 && gridX < mapData.getCols() && gridY >= 0 && gridY < mapData.getRows()) {
            // Kiểm tra biên khi truy cập mảng tileGrid thực tế
            if (gridY >= 0 && gridY < tileGrid.length && tileGrid[gridY] != null && gridX >= 0 && gridX < tileGrid[gridY].length) {
                return tileGrid[gridY][gridX]; // Truy cập mảng 2D theo [row][col] = [gridY][gridX]
            } else {
                System.err.println("Warning: tileGrid access out of bounds at grid (" + gridX + ", " + gridY + ") during getTile.");
                return null; // Trả về null nếu ngoài biên tileGrid
            }
        }
        return null; // Trả về null nếu ngoài biên bản đồ
    }

    // --- Phương thức setTile đã sửa đổi để xử lý phá hủy gạch và tạo item ---
    // Phương thức này giờ chỉ được gọi bởi Bomberman SAU KHI animation gạch vỡ kết thúc
    public void setTile(int gridX, int gridY, Tile newTile) {
        // Kiểm tra biên khi gán vào bản đồ dựa trên kích thước khai báo
        if (gridX >= 0 && gridX < mapData.getCols() && gridY >= 0 && gridY < mapData.getRows()) {
            // Lấy ô Tile hiện tại trước khi thay thế (sử dụng getTile đã có kiểm tra biên)
            // Tile oldTile = getTile(gridX, gridY); // Không cần kiểm tra oldTile ở đây nữa

            // Kiểm tra biên khi gán vào tileGrid thực tế
            if (gridY >= 0 && gridY < tileGrid.length && tileGrid[gridY] != null && gridX >= 0 && gridX < tileGrid[gridY].length) {
                tileGrid[gridY][gridX] = newTile; // Thay thế ô Tile

                // --- Logic xử lý khi phá hủy gạch đã chuyển sang Bomberman ---
                // Map chỉ chịu trách nhiệm thay đổi Tile khi được yêu cầu
                // Logic tạo item ngẫu nhiên cũng nằm trong Bomberman
            } else {
                System.err.println("Warning: tileGrid set out of bounds at grid (" + gridX + ", " + gridY + ").");
            }
        }
    }

    // --- Phương thức mới để thông báo cho Bomberman khi một ô gạch bị ngọn lửa chạm vào ---
    // Phương thức này sẽ được gọi từ Bomb.explode() khi ngọn lửa chạm gạch
    public void brickHitByFlame(int gridX, int gridY) {
        // Kiểm tra biên
        if (gridX >= 0 && gridX < mapData.getCols() && gridY >= 0 && gridY < mapData.getRows()) {
            Tile tile = getTile(gridX, gridY);
            // Kiểm tra xem ô đó có phải là gạch không và chưa bị phá hủy
            if (tile != null && tile.getType() == TileType.BRICK) {
                System.out.println("Flame hit brick at (" + gridX + ", " + gridY + "). Notifying game manager."); // Log

                // --- Thông báo cho Bomberman biết gạch đã bị phá hủy ---
                // Bomberman sẽ tạo animation và sau đó mới gọi setTile
                if (gameManager != null) {
                    gameManager.brickDestroyed(gridX, gridY); // Gọi phương thức mới trong Bomberman
                } else {
                    System.err.println("Error: gameManager is null in Map.brickHitByFlame!");
                }
            }
        }
    }


    public void render(GraphicsContext gc) {
        int rows = mapData.getRows();
        int cols = mapData.getCols();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Kiểm tra biên khi truy cập tileGrid để vẽ
                if (i < tileGrid.length && tileGrid[i] != null && j < tileGrid[i].length) {
                    Tile currentTile = tileGrid[i][j];

                    // Kiểm tra null trước khi lấy Sprite và vẽ
                    if (currentTile != null && currentTile.getSprite() != null) {
                        double px = currentTile.getGridX() * Sprite.SCALED_SIZE;
                        double py = currentTile.getGridY() * Sprite.SCALED_SIZE;
                        gc.drawImage(currentTile.getSprite().getFxImage(), px, py);
                    } else if (currentTile != null && currentTile.getType() == TileType.EMPTY) {
                        // Nếu là ô EMPTY nhưng không có sprite (có thể là lỗi),
                        // có thể vẽ một màu nền tạm thời để debug
                        // gc.setFill(Color.LIGHTGREEN);
                        // gc.fillRect(j * Sprite.SCALED_SIZE, i * Sprite.SCALED_SIZE, Sprite.SCALED_SIZE, Sprite.SCALED_SIZE);
                    }
                } else {
                    System.err.println("Warning: tileGrid access out of bounds at row " + i + ", col " + j + " during render.");
                }
            }
        }

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 20));
        gc.fillText("LEVEL: " + mapData.getLevel() + " (" + rows + "x" + cols + ")", 10, 20);
    }

    public int getRows() { return mapData.getRows(); }
    public int getCols() { return mapData.getCols(); }
    public int getLevel() { return mapData.getLevel(); }
}
