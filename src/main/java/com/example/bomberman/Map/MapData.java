package com.example.bomberman.Map;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map; // Import Map
import java.util.HashMap; // Import HashMap
import java.util.Arrays; // Import Arrays

public class MapData {
    private int level;
    private int rows;
    private int cols;
    private char[][] map;

    // --- Thêm cấu trúc dữ liệu để lưu thông tin Item được giấu dưới gạch ---
    // Key: "gridX,gridY", Value: Ký tự Item ('b', 'f', 's', 'l')
    private Map<String, Character> hiddenItems = new HashMap<>();


    public MapData(int level) {
        this.level = level;
        // Sửa đường dẫn file nếu cần thiết để phù hợp với cấu trúc project của bạn
        // Giả định file map nằm trong res/levels
        loadMapFromFile("/map/level" + level + ".txt");
    }

    private void loadMapFromFile(String filePath) {
        StringBuilder fileContent = new StringBuilder();
        try {
            // Sử dụng getResourceAsStream để đọc file từ classpath (thường là thư mục res)
            InputStream is = getClass().getResourceAsStream(filePath);
            if (is == null) {
                // Thử đọc từ đường dẫn tuyệt đối nếu không tìm thấy trong classpath (ít dùng trong game)
                // is = new FileInputStream(filePath); // Cần import FileInputStream
                throw new IllegalArgumentException("Map file not found in classpath: " + filePath);
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            // Đọc dòng đầu tiên: Level, Rows, Cols
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new IllegalArgumentException("Invalid map file format: Missing header line.");
            }
            String[] header = headerLine.trim().split(" ");
            if (header.length != 3) {
                throw new IllegalArgumentException("Invalid map file header format: Expected 3 values (Level Rows Cols). Found: " + headerLine);
            }

            try {
                this.level = Integer.parseInt(header[0]);
                this.rows = Integer.parseInt(header[1]);
                this.cols = Integer.parseInt(header[2]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid map file header: Cannot parse numbers in header: " + headerLine, e);
            }


            // Đọc các dòng bản đồ
            map = new char[rows][cols];
            for (int i = 0; i < rows; i++) {
                String line = reader.readLine();
                if (line == null) {
                    throw new IllegalArgumentException("Invalid map file format: Missing map row " + i);
                }
                // Đảm bảo độ dài dòng khớp với số cột đã khai báo
                if (line.length() > cols) {
                    System.err.println("Warning: Map row " + i + " is longer than declared columns (" + cols + "). Truncating.");
                    line = line.substring(0, cols);
                } else if (line.length() < cols) {
                    // Xử lý nếu dòng ngắn hơn (ví dụ: điền khoảng trắng hoặc báo lỗi)
                    char[] rowChars = line.toCharArray();
                    map[i] = Arrays.copyOf(rowChars, cols); // Điền khoảng trắng vào cuối
                    System.err.println("Warning: Map row " + i + " in file is shorter than declared columns (" + cols + "). Padding with spaces.");
                } else {
                    map[i] = line.toCharArray(); // Độ dài khớp
                }


                for (int j = 0; j < cols; j++) {
                    char mapChar = map[i][j]; // Lấy ký tự sau khi đã xử lý độ dài dòng

                    // --- Lưu thông tin Item nếu gặp ký tự Item ---
                    if (mapChar == 'b' || mapChar == 'f' || mapChar == 's' || mapChar == 'l' || mapChar =='a') {
                        String key = j + "," + i; // gridX,gridY
                        hiddenItems.put(key, mapChar);
                        System.out.println("Found hidden item '" + mapChar + "' at (" + j + ", " + i + ")"); // Log
                    }
                }
            }

            reader.close();
        } catch (IOException e) {
            System.err.println("Error loading map file " + filePath + ": " + e.getMessage());
            e.printStackTrace();
            // Có thể ném RuntimeException để dừng game nếu tải map thất bại
            throw new RuntimeException("Failed to load map file: " + filePath, e);
        } catch (NumberFormatException e) {
            System.err.println("Error parsing map header in file: " + filePath);
            e.printStackTrace();
            throw new RuntimeException("Failed to parse map header: " + filePath, e);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Error parsing map header format in file: " + filePath + ". Expected 'level rows cols'.");
            e.printStackTrace();
            throw new RuntimeException("Failed to parse map header format: " + filePath, e);
        }
    }

    // Phương thức lấy ký tự tại vị trí hàng, cột
    public char getAt(int row, int col) {
        // Thêm kiểm tra biên khi truy cập mảng map
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            return map[row][col];
        } else {
            System.err.println("Warning: Attempted to access map data out of bounds at (" + row + ", " + col + ")");
            return ' '; // Trả về ký tự trống nếu ngoài biên
        }
    }

    // --- Getters ---
    public int getLevel() {
        return level;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public char[][] getMap() {
        return map;
    }

    // --- Getter mới để lấy thông tin Item được giấu ---
    public Map<String, Character> getHiddenItems() {
        return hiddenItems;
    }

    // TODO: Các phương thức khác nếu cần
}
