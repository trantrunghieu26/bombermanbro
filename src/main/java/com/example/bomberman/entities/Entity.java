package com.example.bomberman.entities;

import com.example.bomberman.Controller;
import com.example.bomberman.Map.Map;
import com.example.bomberman.Map.Tile;
import com.example.bomberman.Map.TileType;
import com.example.bomberman.graphics.Sprite;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.HashSet;
import java.util.Set;

public abstract class Entity {

    //           //      Attributes         ///        ///

    // Vị trí theo pixel trên màn hình
    protected double pixelX;
    protected double pixelY;

    // Vị trí theo ô lưới trên bản đồ (có thể tính từ pixelX, pixelY)
    protected int gridX;
    protected int gridY;

    // Kích thước của thực thể (thường bằng kích thước của Sprite)
    protected int width = Sprite.SCALED_SIZE;
    protected int height = Sprite.SCALED_SIZE;

    // Sprite hiện tại của thực thể để vẽ
    protected Sprite sprite;

    // Cờ đánh dấu thực thể có cần loại bỏ khỏi game không
    protected boolean removed = false;

    protected Map map;

    //     //     Methods       //                   //

    public Entity(int gridX, int gridY, Map map) {
        this.gridX = gridX;
        this.gridY = gridY;

        this.pixelX = gridX * Sprite.SCALED_SIZE;
        this.pixelY = gridY * Sprite.SCALED_SIZE;
        this.map = map;
    }

    public Entity(int gridX, int gridY, Sprite sprite) {
        this.gridX = gridX;
        this.gridY = gridY;

        this.pixelX = gridX * Sprite.SCALED_SIZE;
        this.pixelY = gridY * Sprite.SCALED_SIZE;
        this.sprite = sprite;
    }

    public boolean checkCollision(double checkPixelX, double checkPixelY) {
        double playerSize = Sprite.SCALED_SIZE;
        // --- TINH CHỈNH GIÁ TRỊ BUFFER NÀY ---
        // Thử giảm giá trị này nếu nhân vật bị trễ khi bắt đầu di chuyển
        double buffer = 6.0; // Thử 2.0, 1.0, 0.5, 0.1

        // Tính toán tọa độ pixel của 4 góc hộp va chạm Player tại vị trí checkPixelX, checkPixelY
        // Các điểm này cần nằm bên trong hộp va chạm Player
        double topLeftX = checkPixelX + buffer;
        double topLeftY = checkPixelY + buffer;
        double topRightX = checkPixelX + playerSize - buffer;
        double topRightY = checkPixelY + buffer;
        double bottomLeftX = checkPixelX + buffer;
        double bottomLeftY = checkPixelY + playerSize - buffer;
        double bottomRightX = checkPixelX + playerSize - buffer;
        double bottomRightY = checkPixelY + playerSize - buffer;

        // Chuyển đổi các tọa độ pixel của các góc sang tọa độ lưới
        // Sử dụng Math.floor để đảm bảo chúng ta kiểm tra ô mà góc đó nằm trong
        int gridX1 = (int) Math.floor(topLeftX / playerSize);
        int gridY1 = (int) Math.floor(topLeftY / playerSize);

        int gridX2 = (int) Math.floor(topRightX / playerSize);
        int gridY2 = (int) Math.floor(topRightY / playerSize);

        int gridX3 = (int) Math.floor(bottomLeftX / playerSize);
        int gridY3 = (int) Math.floor(bottomLeftY / playerSize);

        int gridX4 = (int) Math.floor(bottomRightX / playerSize);
        int gridY4 = (int) Math.floor(bottomRightY / playerSize);

        // Tập hợp các ô lưới cần kiểm tra (loại bỏ các ô trùng lặp)
        Set<String> tilesToCheck = new HashSet<>();
        tilesToCheck.add(gridX1 + "," + gridY1);
        tilesToCheck.add(gridX2 + "," + gridY2);
        tilesToCheck.add(gridX3 + "," + gridY3);
        tilesToCheck.add(gridX4 + "," + gridY4);

        // Kiểm tra từng ô lưới trong danh sách
        for (String tileCoord : tilesToCheck) {
            String[] coords = tileCoord.split(",");
            int checkGridX = Integer.parseInt(coords[0]);
            int checkGridY = Integer.parseInt(coords[1]);

            // Lấy Tile từ Map, kiểm tra biên
            Tile tile = map.getTile(checkGridX, checkGridY);

            // Nếu ô Tile tồn tại và KHÔNG thể đi qua được, thì CÓ va chạm
            if (tile != null && !tile.isWalkable()) {
                // TODO: Handle collision with special Tile types (e.g., Portal)
                if (tile.getType() == TileType.PORTAL) {
                    // Logic to check if conditions are met to enter Portal
                    // If not met, treat as not walkable
                    // return true;
                    // If conditions ARE met, treat as walkable
                    // continue;
                } else {
                    // Collision with Wall or Brick
                    return true; // Collision detected
                }
            }
        }

        return false; // No collision
    }

    public void render(GraphicsContext gc) {
        // Chỉ vẽ nếu có sprite và chưa bị loại bỏ
        if (sprite != null && !removed) {
            // Vẽ Sprite tại vị trí pixel hiện tại
            gc.drawImage(sprite.getFxImage(), pixelX, pixelY);
        }
        // TODO: Có thể thêm logic debug như vẽ hộp va chạm ở đây (chỉ trong chế độ debug)
    }

    // --- Getters ---
    public double getPixelX() { return pixelX; }
    public double getPixelY() { return pixelY; }
    // Tính toán và trả về vị trí lưới dựa trên vị trí pixel hiện tại (làm tròn)
    public int getGridX() { return (int) Math.round(pixelX / Sprite.SCALED_SIZE); }
    public int getGridY() { return (int) Math.round(pixelY / Sprite.SCALED_SIZE); }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public Sprite getSprite() { return sprite; }
    public boolean isRemoved() { return removed; }
    public void setPixelX(double x) { this.pixelX = x; }
    public void setPixelY(double y) { this.pixelY = y; }
    // Phương thức đánh dấu thực thể cần loại bỏ ở cuối vòng lặp update
    public void remove() {
        removed = true;
    }
    public void setRemoved(boolean removed) { this.removed = removed; }
    public Map getMap() { return this.map; }
}
