package com.example.bomberman.Map;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import com.example.bomberman.graphics.Sprite; // Đảm bảo Sprite.java có thể truy cập

public class Map {
    private MapData mapData;
    private Tile[][] tileGrid; // Thêm mảng Tile

    public Map(MapData mapData) {
        this.mapData = mapData;
        initializeTiles(); // Khởi tạo mảng Tile
    }

    private void initializeTiles() {
        int rows = mapData.getRows();
        int cols = mapData.getCols();
        char[][] charMap = mapData.getMap(); // Lấy mảng ký tự từ MapData [cite: 25]

        tileGrid = new Tile[rows][cols]; // Khởi tạo mảng Tile

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char mapChar = charMap[i][j];
                // Tạo đối tượng Tile từ ký tự và tọa độ lưới (j là X, i là Y)
                tileGrid[i][j] = Tile.createTileFromChar(j, i, mapChar);

                // TODO: Xử lý các thực thể động (người chơi, quái vật, item) ở đây hoặc ở một lớp quản lý thực thể khác.
                // Các ký tự 'p', '1', '2', 'b', 'f', 's', 'l' cho biết VỊ TRÍ ban đầu của thực thể đó.
                // Bạn sẽ tạo các đối tượng Player, Enemy, Item TẠI VỊ TRÍ (j, i) này
                // và thêm chúng vào các danh sách quản lý thực thể trong game state.
                // Ô (Tile) tại vị trí đó vẫn sẽ là EMPTY.
            }
        }
    }

    public Tile getTile(int gridX, int gridY) {
        // Kiểm tra biên
        if (gridX >= 0 && gridX < mapData.getCols() && gridY >= 0 && gridY < mapData.getRows()) {
            return tileGrid[gridY][gridX]; // Truy cập mảng 2D theo [row][col] = [gridY][gridX]
        }
        return null; // Trả về null nếu ngoài biên
    }

    // Có thể cần setter nếu muốn thay thế hoàn toàn một ô
    public void setTile(int gridX, int gridY, Tile newTile) {
        if (gridX >= 0 && gridX < mapData.getCols() && gridY >= 0 && gridY < mapData.getRows()) {
            tileGrid[gridY][gridX] = newTile;
        }
    }


    public void render(GraphicsContext gc) {
        // Bắt đầu phương thức render

        int rows = mapData.getRows();
        int cols = mapData.getCols();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Tile currentTile = tileGrid[i][j]; // Lấy Tile từ mảng TileGrid

                // Tính toán vị trí vẽ pixel từ tọa độ lưới của Tile
                double px = currentTile.getGridX() * Sprite.SCALED_SIZE;
                double py = currentTile.getGridY() * Sprite.SCALED_SIZE;

                // Vẽ Sprite của Tile
                if (currentTile.getSprite() != null) {
                    gc.drawImage(currentTile.getSprite().getFxImage(), px, py);
                }

                // TODO: Logic vẽ các thực thể (người chơi, quái vật, bom, lửa, item)
                // Đảm bảo logic này nằm TRONG phương thức render
            }
        }

        // --- Phần vẽ thông tin level ---
        // Đảm bảo các dòng này nằm BÊN TRONG phương thức render
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", 20));
        // Sử dụng mapData.getRows() và mapData.getCols() để lấy kích thước,
        // hoặc có thể dùng trực tiếp rows và cols biến cục bộ như bạn đang làm.
        gc.fillText("LEVEL: " + mapData.getLevel() + " (" + rows + "x" + cols + ")", 10, 20);


    }

    public boolean isValidGrid(int gridX, int gridY) {
        return gridX >= 0 && gridX < mapData.getCols() &&
                gridY >= 0 && gridY < mapData.getRows();
    }

    // Các phương thức khác của lớp Map...
    public int getRows() { return mapData.getRows(); }
    public int getCols() { return mapData.getCols(); }
    public int getLevel() { return mapData.getLevel(); }
}