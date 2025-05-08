package com.example.bomberman.Map;

import com.example.bomberman.Bomberman;
import com.example.bomberman.Controller;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
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
                    case '+': // valuableBomb
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


    public void render(Controller gameManager) {
        GraphicsContext gc = gameManager.gc;
        if (mapData == null || tileGrid == null || gc == null) {
            System.err.println("Error rendering map: Map data, tile grid, or GC is null.");
            return;
        }

        int rows = mapData.getRows();
        int cols = mapData.getCols();

        // Lấy trạng thái portal một lần ngoài vòng lặp để tối ưu nhẹ
        boolean isPortalCurrentlyActive = (gameManager != null && gameManager.isPortalActivated());
        // Lấy thời gian game một lần
        double gameTime = (gameManager != null) ? gameManager.getElapsedTime() : 0;

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                // Kiểm tra biên tileGrid
                if (i >= 0 && i < tileGrid.length && tileGrid[i] != null && j >= 0 && j < tileGrid[i].length) {
                    Tile currentTile = tileGrid[i][j];

                    if (currentTile != null) {
                        Sprite currentSprite = currentTile.getSprite();
                        if (currentSprite != null) {
                            Image imageToDraw = currentSprite.getFxImage();
                            if (imageToDraw != null) {
                                double px = currentTile.getGridX() * Sprite.SCALED_SIZE;
                                // Tọa độ Y đã bao gồm offset (nếu có)
                                double py = currentTile.getGridY() * Sprite.SCALED_SIZE + Bomberman.UI_PANEL_HEIGHT;

                                // --- KIỂM TRA VÀ XỬ LÝ PORTAL ---
                                if (currentTile.getType() == TileType.PORTAL && isPortalCurrentlyActive) {
                                    // Portal đang active -> làm nó nhấp nháy alpha
                                    // Tính alpha dựa trên sin của thời gian
                                    // Bạn có thể điều chỉnh số 5.0 (tần số nháy) và 0.35 (biên độ nháy)
                                    double alpha = 0.65 + 0.35 * Math.sin(gameTime * 5.0);
                                    // Giới hạn alpha trong khoảng hợp lệ (0.0 đến 1.0) đề phòng lỗi làm tròn
                                    alpha = Math.max(0.0, Math.min(1.0, alpha));

                                    // Lưu lại alpha hiện tại của gc
                                    double originalAlpha = gc.getGlobalAlpha();
                                    // Đặt alpha mới
                                    gc.setGlobalAlpha(alpha);
                                    // Vẽ Portal với alpha mới
                                    gc.drawImage(imageToDraw, px, py);
                                    // QUAN TRỌNG: Khôi phục lại alpha ban đầu để không ảnh hưởng các hình vẽ sau
                                    gc.setGlobalAlpha(originalAlpha);
                                } else {
                                    // Vẽ Tile bình thường (không phải Portal active)
                                    gc.drawImage(imageToDraw, px, py);
                                }
                                // ---------------------------------
                            }
                        } else if (currentTile.getType() == TileType.EMPTY) {
                            // Xử lý vẽ nền cho ô trống nếu cần
                        }
                    }
                } else {
                    System.err.println("Warning: tileGrid access out of bounds at row " + i + ", col " + j + " during render.");
                }
            }
        }
    } // Kết thúc render()

    public boolean isValidGrid(int gridX, int gridY) {
        return gridX >= 0 && gridX < mapData.getCols() &&
                gridY >= 0 && gridY < mapData.getRows();
    }

    // Các phương thức khác của lớp Map...
    public int getRows() { return mapData.getRows(); }
    public int getCols() { return mapData.getCols(); }
    public int getLevel() { return this.mapData.getLevel(); }
    public MapData getMapData() { return this.mapData; }
}