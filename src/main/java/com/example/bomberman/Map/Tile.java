package com.example.bomberman.Map; // Đảm bảo cùng package

import com.example.bomberman.graphics.Sprite; // Cần để lấy hình ảnh/sprite cho ô
import javafx.scene.image.Image; // Import Image của JavaFX

public class Tile {
    private int gridX; // Tọa độ cột trong lưới (tương ứng với 'j' khi đọc map)
    private int gridY; // Tọa độ hàng trong lưới (tương ứng với 'i' khi đọc map)
    private TileType type;
    private boolean isDestructible;
    private Sprite sprite; // Lưu Sprite tương ứng để vẽ

    // Constructor
    public Tile(int gridX, int gridY, TileType type) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.type = type;
        // Thiết lập khả năng phá hủy dựa trên loại ô
        this.isDestructible = (type == TileType.BRICK);

        // Gán Sprite ban đầu dựa trên loại ô
        setSpriteForType(type);
    }

    // Phương thức helper để xác định Sprite dựa trên loại Tile
    private void setSpriteForType(TileType type) {
        switch (type) {
            case EMPTY:
                this.sprite = Sprite.grass; // Sử dụng Sprite cỏ cho ô trống
                break;
            case WALL:
                this.sprite = Sprite.wall; // Sử dụng Sprite tường
                break;
            case BRICK:
                this.sprite = Sprite.brick; // Sử dụng Sprite gạch
                break;
            case PORTAL:
                this.sprite = Sprite.portal; // Sử dụng Sprite cửa
                break;
            default:
                this.sprite = Sprite.grass; // Mặc định là cỏ
                break;
        }
    }

    // --- Getters ---

    public int getGridX() {
        return gridX;
    }

    public int getGridY() {
        return gridY;
    }

    public TileType getType() {
        return type;
    }

    public boolean isDestructible() {
        return isDestructible;
    }

    public Sprite getSprite() {
        return sprite;
    }

    // Trả về hình ảnh FX Image để vẽ trên GraphicsContext
    public Image getFxImage() {
        return (this.sprite != null) ? this.sprite.getFxImage() : null;
    }


    // --- Setters (Quan trọng cho logic game) ---

    // Khi một ô bị phá hủy hoặc trạng thái thay đổi
    public void setType(TileType type) {
        this.type = type;
        // Cập nhật lại isDestructible và Sprite khi loại ô thay đổi
        this.isDestructible = (type == TileType.BRICK); // Chỉ gạch là phá hủy được

        // Cập nhật Sprite tương ứng với loại mới
        setSpriteForType(type);

        // TODO: Logic xử lý vật phẩm rơi ra khi BRICK bị phá hủy (* -> EMPTY).
        // Khi setType(TileType.EMPTY) cho một ô từng là BRICK,
        // bạn cần kiểm tra xem có vật phẩm nào (Bomb, Flame, Speed)
        // nên xuất hiện tại vị trí này không. Logic này có thể nằm ở lớp quản lý game state
        // hoặc phương thức xử lý vụ nổ bom, sau khi gọi setType cho các ô bị ảnh hưởng.
    }

    public void setDestructible(boolean destructible) {
        this.isDestructible = destructible;
    }

    // --- Các phương thức tiện ích khác ---

    // Kiểm tra xem thực thể có thể di chuyển qua ô này không
    public boolean isWalkable() {
        return this.type == TileType.EMPTY ||
                this.type == TileType.PORTAL||
                this.type == TileType.BOMB;
        // Lưu ý: Các thực thể động (người chơi, quái vật, item) nằm trên ô EMPTY hoặc PORTAL có thể ảnh hưởng đến việc đi lại.
        // Logic kiểm tra có thực thể khác chiếm ô không sẽ nằm ở lớp quản lý thực thể.
    }

    // Phương thức tĩnh để tạo Tile từ ký tự và tọa độ (sử dụng trong initializeTiles của Map)
    public static Tile createTileFromChar(int gridX, int gridY, char mapChar) {
        // Sử dụng phương thức fromChar của enum để xác định loại
        TileType type = TileType.fromChar(mapChar);

        // TODO: Khi tạo Tile, nếu mapChar là ký tự của thực thể động ('p', '1', '2', 'b', 'f', 's', 'l'),
        // bạn không chỉ tạo Tile (là EMPTY), mà còn cần tạo đối tượng thực thể (Player, Enemy, Item)
        // và thêm nó vào danh sách quản lý các thực thể trong game state của bạn.
        // Việc tạo thực thể động KHÔNG nên nằm trong constructor của Tile.

        // Tạo Tile cơ bản (nền) tại vị trí đó
        return new Tile(gridX, gridY, type); // Constructor sẽ tự thiết lập isDestructible và Sprite
    }

    // Nếu cần biểu diễn thông tin ô dưới dạng String (ví dụ để debug)
    @Override
    public String toString() {
        return "Tile(" + gridX + "," + gridY + ", " + type + ", Destr=" + isDestructible + ")";
    }
}