package com.example.bomberman.Map;

import java.io.*;
import java.util.*;

public class MapData {
    private int level;
    private int rows;
    private int cols;
    private char[][] map;

    public MapData(int level) {
        this.level = level;
        loadMapFromFile("res/map/level" + level + ".txt");
    }

    private void loadMapFromFile(String path) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String[] tokens = br.readLine().split(" ");
            level = Integer.parseInt(tokens[0]);
            rows = Integer.parseInt(tokens[1]);
            cols = Integer.parseInt(tokens[2]);

            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }

            map = new char[rows][cols];
            for (int i = 0; i < rows; i++) {
                map[i] = lines.get(i).toCharArray();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public char getAt(int row, int col) {
        return map[row][col];
    }

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
}
