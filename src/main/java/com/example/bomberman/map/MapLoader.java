package com.example.bomberman.map;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MapLoader {
    private final char[][] map;

    public MapLoader(String path) throws IOException {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Đường dẫn file không được để trống");
        }
        map = loadMap(path);
    }

    private char[][] loadMap(String filePath) throws IOException {
        // Đọc file từ hệ thống file bằng đường dẫn tương đối
        List<String> lines = new ArrayList<>();
        try {
            // Sử dụng Files.readAllLines để đọc file
            lines = Files.readAllLines(Paths.get(filePath));
            // Lọc bỏ các dòng trống
            lines.removeIf(String::isEmpty);
        } catch (IOException e) {
            throw new IOException("Không tìm thấy file: " + filePath + ". Đảm bảo file tồn tại tại đường dẫn được chỉ định.", e);
        }

        if (lines.isEmpty()) {
            throw new IOException("File bản đồ trống: " + filePath);
        }

        return convertToCharArray(lines);
    }

    private char[][] convertToCharArray(List<String> lines) {
        int rows = lines.size();
        int cols = lines.stream().mapToInt(String::length).max().orElse(0);
        char[][] map = new char[rows][cols];
        for (int i = 0; i < rows; i++) {
            String line = lines.get(i);
            for (int j = 0; j < cols; j++) {
                map[i][j] = j < line.length() ? line.charAt(j) : ' ';
            }
        }
        return map;
    }

    public char[][] getMap() {
        return deepCopy(map);
    }

    private char[][] deepCopy(char[][] original) {
        if (original == null) return null;
        char[][] copy = new char[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }
}