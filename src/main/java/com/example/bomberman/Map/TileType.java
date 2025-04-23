package com.example.bomberman.Map; // Đảm bảo cùng package

public enum TileType {
    EMPTY(' '),     // Ô trống (bao gồm cả cỏ mặc định)
    WALL('#'),      // Tường cố định
    BRICK('*'),     // Gạch (khối có thể phá hủy)
    PORTAL('x');    // Cửa ra/vào

    private final char mapChar; // Lưu trữ ký tự tương ứng trong file map

    TileType(char mapChar) {
        this.mapChar = mapChar;
    }

    public char getMapChar() {
        return mapChar;
    }

    // Phương thức tiện ích để lấy TileType từ ký tự
    public static TileType fromChar(char ch) {
        for (TileType type : values()) {
            if (type.getMapChar() == ch) {
                return type;
            }
        }
        // Xử lý các ký tự động (người chơi, quái vật, item)
        // Mặc định coi ô dưới các thực thể này là EMPTY
        switch (ch) {
            case 'p': // Player start
            case '1': // Enemy Balloom
            case '2': // Enemy Oneal
            case 'b': // Powerup Bombs
            case 'f': // Powerup Flames
            case 's': // Powerup Speed
            case 'l': // Powerup Flamepass
                return EMPTY; // Các thực thể này nằm trên ô trống
            default:
                // Có thể ném ngoại lệ hoặc trả về một loại mặc định khác nếu gặp ký tự lạ
                System.err.println("Unknown tile character: " + ch + ". Defaulting to EMPTY.");
                return EMPTY;
        }
    }
}