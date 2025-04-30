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

    // Constructor: Nhận thêm tham chiếu đến Bomberman
    public Map(MapData mapData, Bomberman gameManager) {
        this.mapData = mapData;
        this.gameManager = gameManager; // Lưu tham chiếu
        initializeTiles(); // Gọi phương thức khởi tạo Tile ngay trong constructor
    }

    // --- Phương thức khởi tạo lưới Tile từ MapData (Đã sửa) ---
    private void initializeTiles() {
        int rows = mapData.getRows();
        int cols = mapData.getCols();
        char[][] charMap = mapData.getMap(); // Lấy mảng ký tự bản đồ thô từ MapData

        tileGrid = new Tile[rows][cols]; // Khởi tạo mảng TileGrid với kích thước khai báo

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                char mapChar = ' '; // Ký tự tại vị trí (j, i) trong file map
                char initialTileChar = ' '; // Ký tự đại diện cho loại Tile ban đầu

                // Thêm kiểm tra biên khi truy cập charMap thực tế
                if (i < charMap.length && charMap[i] != null && j < charMap[i].length) {
                    mapChar = charMap[i][j]; // Lấy ký tự nếu chỉ mục hợp lệ
                } else {
                    // Xử lý trường hợp kích thước bản đồ không khớp hoặc dữ liệu không mong muốn
                    System.err.println("Warning: Map data mismatch or unexpected data at row " + i + ", col " + j + ". Using default Grass tile (' ').");
                    mapChar = ' '; // Mặc định là Grass nếu có lỗi dữ liệu
                }

                // --- Xác định loại Tile ban đầu dựa trên ký tự mapChar ---
                // Các ký tự thực thể động và Item ẩn ban đầu sẽ nằm trên Tile nền
                switch (mapChar) {
                    case '#': // Wall
                        initialTileChar = '#';
                        break;
                    case '*': // Brick
                    case 'b': // Bomb Item (ban đầu là Brick)
                    case 'f': // Flame Item (ban đầu là Brick)
                    case 's': // Speed Item (ban đầu là Brick)
                    case 'l': // Life Item (ban đầu là Brick)
                    case 'a': // kickbomb Item (ban đầu là Brick)
                        initialTileChar = '*'; // Tất cả các ký tự này ban đầu sẽ là Brick
                        break;
                    case 'x': // Portal
                        initialTileChar = 'x'; // Portal là một loại Tile riêng
                        break;
                    case 'p': // Player (ban đầu thường trên Grass, nhưng có thể map data đặt trên Brick)
                    case '1': // Enemy (Balloom)
                    case '2': // Enemy (Oneal)
                    case '3': // Enemy (Doll)
                    case '4': // Enemy (Ghost)
                    case '5': // Enemy (Minvo)
                    case '6': // Enemy (Kondoria)
                    case ' ': // Grass (hoặc ký tự khác không phải thực thể động/item/wall/brick/portal)
                    default: // Mặc định là Grass nếu không khớp các ký tự đặc biệt khác
                        initialTileChar = ' '; // Các thực thể động và Item ẩn (nếu không nằm trên Brick) sẽ nằm trên Grass

                        break;
                }


                // --- Sử dụng Tile.createTileFromChar để tạo Tile từ initialTileChar ---
                // Logic này chỉ đơn giản là tạo ra các Tile ban đầu (Wall, Brick, Grass, Portal)
                // dựa trên loại Tile nền được xác định.
                if (i < tileGrid.length && tileGrid[i] != null && j < tileGrid[i].length) {
                    tileGrid[i][j] = Tile.createTileFromChar(j, i, initialTileChar);
                } else {
                    // Trường hợp không mong muốn: tileGrid không được khởi tạo đúng kích thước
                    System.err.println("Severe Error: tileGrid initialization logic error at row " + i + ", col " + j + ".");
                    // Có thể cần xử lý lỗi nghiêm trọng hơn ở đây
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

    // --- Phương thức setTile: Thay đổi Tile tại vị trí lưới (Không xử lý gạch vỡ hay item ở đây) ---
    public void setTile(int gridX, int gridY, Tile newTile) {
        // Kiểm tra biên khi gán vào bản đồ dựa trên kích thước khai báo
        if (gridX >= 0 && gridX < mapData.getCols() && gridY >= 0 && gridY < mapData.getRows()) {
            // Kiểm tra biên khi gán vào tileGrid thực tế
            if (gridY >= 0 && gridY < tileGrid.length && tileGrid[gridY] != null && gridX >= 0 && gridX < tileGrid[gridY].length) {
                tileGrid[gridY][gridX] = newTile; // Thay thế ô Tile
            } else {
                System.err.println("Warning: tileGrid set out of bounds at grid (" + gridX + ", " + gridY + ").");
            }
        }
    }

    // --- Phương thức được gọi từ Bomb.explode() khi ngọn lửa chạm gạch ---
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
                    gameManager.brickDestroyed(gridX, gridY); // Gọi phương thức trong Bomberman
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
                        double py = currentTile.getGridY() * Sprite.SCALED_SIZE+Bomberman.UI_PANEL_HEIGHT;

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


    }

    public int getRows() { return mapData.getRows(); }
    public int getCols() { return mapData.getCols(); }
    public int getLevel() { return mapData.getLevel(); }
}
