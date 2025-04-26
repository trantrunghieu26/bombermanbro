package com.example.bomberman.entities;

import com.example.bomberman.Map.Map;
import com.example.bomberman.Map.Tile;
import com.example.bomberman.Map.TileType;
import com.example.bomberman.graphics.Sprite;

import java.util.HashSet;
import java.util.Set;

public abstract class Entity {

    protected Map map;

    public Entity(Map map) {
        this.map = map;
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

        // If all checked tiles are walkable, no collision detected
        return false; // No collision
    }
}
