package com.example.bomberman.graphics;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

/**
 * Tất cả sprite (hình ảnh game) được lưu trữ vào một ảnh duy nhất
 * Class này giúp lấy ra các sprite riêng từ 1 ảnh chung duy nhất đó
 */
public class SpriteSheet {

    private final String _path;
    public final int SIZE;
    public int[] pixels;
    public BufferedImage image;


    public static SpriteSheet tiles = new SpriteSheet("res/textures/classic1.png", 256);
    public static SpriteSheet tiles1 = new SpriteSheet("res/textures/TEXTURE.png", 256);
    public static SpriteSheet tiles2 = new SpriteSheet("res/textures/classic2.png", 512);


    public SpriteSheet(String path, int size) {
        _path = path;
        SIZE = size;
        pixels = new int[SIZE * SIZE];
        load();
    }

    private void load() {
        try {
            image = ImageIO.read(new java.io.File(_path));
            int w = image.getWidth();
            int h = image.getHeight();
            image.getRGB(0, 0, w, h, pixels, 0, w);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}