    package com.example.bomberman.graphics;

    import javafx.scene.image.*;
    import javafx.scene.image.Image; // Import rõ ràng Image của JavaFX
    import javafx.scene.image.WritableImage; // Import rõ ràng WritableImage
    import javafx.scene.image.PixelWriter; // Import rõ ràng PixelWriter
    import javafx.scene.image.PixelReader; // Import rõ ràng PixelReader
    import javafx.scene.image.ImageView; // Import rõ ràng ImageView


    public class Sprite {

        public static final int DEFAULT_SIZE = 16;
        public static final int SCALED_SIZE = DEFAULT_SIZE * 2;
        private static final int TRANSPARENT_COLOR = 0xffff00ff;
        public final int SIZE;
        private int x, y;
        public int[] pixels;
        protected int realWidth;
        protected int realHeight;
        private SpriteSheet sheet;

        // --- Thêm thuộc tính để lưu trữ JavaFX Image đã được tạo sẵn ---
        private Image fxImage = null;


        /*
        |--------------------------------------------------------------------------
        | Board sprites
        |--------------------------------------------------------------------------
         */
        public static Sprite grass = new Sprite(SCALED_SIZE, 6, 0, SpriteSheet.tiles2, 32, 32);
        public static Sprite brick = new Sprite(SCALED_SIZE, 7, 0, SpriteSheet.tiles2, 32, 32);
        public static Sprite wall = new Sprite(SCALED_SIZE, 5, 0, SpriteSheet.tiles2, 32, 32);
        public static Sprite portal = new Sprite(DEFAULT_SIZE, 0, 11, SpriteSheet.tiles, 14, 14);
        public static Sprite realportal = new Sprite(DEFAULT_SIZE, 4, 0, SpriteSheet.tiles, 14, 14);
        public static Sprite sign1 = new Sprite(DEFAULT_SIZE, 6, 0, SpriteSheet.tiles, 16, 16);

        public static Sprite sign2= new Sprite(DEFAULT_SIZE, 6, 1, SpriteSheet.tiles, 16, 16);
        /*
        |--------------------------------------------------------------------------
        | Bomber Sprites
        |--------------------------------------------------------------------------
         */
        public static Sprite player_up = new Sprite(SCALED_SIZE, 0, 0, SpriteSheet.tiles2, 12, 16);
        public static Sprite player_down = new Sprite(SCALED_SIZE, 2, 0, SpriteSheet.tiles2, 12, 15);
        public static Sprite player_left = new Sprite(SCALED_SIZE, 3, 0, SpriteSheet.tiles2, 10, 15);
        public static Sprite player_right = new Sprite(SCALED_SIZE, 1, 0, SpriteSheet.tiles2, 10, 16);

        public static Sprite player_up_1 = new Sprite(SCALED_SIZE, 0, 1, SpriteSheet.tiles2, 12, 16);
        public static Sprite player_up_2 = new Sprite(SCALED_SIZE, 0, 2, SpriteSheet.tiles2, 12, 15);

        public static Sprite player_down_1 = new Sprite(SCALED_SIZE, 2, 1, SpriteSheet.tiles2, 12, 15);
        public static Sprite player_down_2 = new Sprite(SCALED_SIZE, 2, 2, SpriteSheet.tiles2, 12, 16);

        public static Sprite player_left_1 = new Sprite(SCALED_SIZE, 3, 1, SpriteSheet.tiles2, 11, 16);
        public static Sprite player_left_2 = new Sprite(SCALED_SIZE, 3, 2, SpriteSheet.tiles2, 12 ,16);

        public static Sprite player_right_1 = new Sprite(SCALED_SIZE, 1, 1, SpriteSheet.tiles2, 11, 16);
        public static Sprite player_right_2 = new Sprite(SCALED_SIZE, 1, 2, SpriteSheet.tiles2, 12, 16);

        public static Sprite player_dead1 = new Sprite(SCALED_SIZE, 4, 2, SpriteSheet.tiles2, 14, 16);
        public static Sprite player_dead2 = new Sprite(SCALED_SIZE, 5, 2, SpriteSheet.tiles2, 13, 15);
        public static Sprite player_dead3 = new Sprite(SCALED_SIZE, 6, 2, SpriteSheet.tiles2, 16, 16);



        /*
        |--------------------------------------------------------------------------
        | Character
        |--------------------------------------------------------------------------
         */
        //BALLOM
        public static Sprite balloom_left1 = new Sprite(DEFAULT_SIZE, 9, 0, SpriteSheet.tiles, 16, 16);
        public static Sprite balloom_left2 = new Sprite(DEFAULT_SIZE, 9, 1, SpriteSheet.tiles, 16, 16);
        public static Sprite balloom_left3 = new Sprite(DEFAULT_SIZE, 9, 2, SpriteSheet.tiles, 16, 16);

        public static Sprite balloom_right1 = new Sprite(DEFAULT_SIZE, 10, 0, SpriteSheet.tiles, 16, 16);
        public static Sprite balloom_right2 = new Sprite(DEFAULT_SIZE, 10, 1, SpriteSheet.tiles, 16, 16);
        public static Sprite balloom_right3 = new Sprite(DEFAULT_SIZE, 10, 2, SpriteSheet.tiles, 16, 16);

        public static Sprite balloom_dead = new Sprite(DEFAULT_SIZE, 9, 3, SpriteSheet.tiles, 16, 16);

        //ONEAL
        public static Sprite oneal_left1 = new Sprite(DEFAULT_SIZE, 11, 0, SpriteSheet.tiles, 16, 16);
        public static Sprite oneal_left2 = new Sprite(DEFAULT_SIZE, 11, 1, SpriteSheet.tiles, 16, 16);
        public static Sprite oneal_left3 = new Sprite(DEFAULT_SIZE, 11, 2, SpriteSheet.tiles, 16, 16);

        public static Sprite oneal_right1 = new Sprite(DEFAULT_SIZE, 12, 0, SpriteSheet.tiles, 16, 16);
        public static Sprite oneal_right2 = new Sprite(DEFAULT_SIZE, 12, 1, SpriteSheet.tiles, 16, 16);
        public static Sprite oneal_right3 = new Sprite(DEFAULT_SIZE, 12, 2, SpriteSheet.tiles, 16, 16);

        public static Sprite oneal_dead = new Sprite(DEFAULT_SIZE, 11, 3, SpriteSheet.tiles, 16, 16);

        //Doll
        public static Sprite coin_left1 = new Sprite(DEFAULT_SIZE, 13, 0, SpriteSheet.tiles, 16, 16);
        public static Sprite coin_left2 = new Sprite(DEFAULT_SIZE, 13, 1, SpriteSheet.tiles, 16, 16);
        public static Sprite coin_left3 = new Sprite(DEFAULT_SIZE, 13, 2, SpriteSheet.tiles, 16, 16);

        public static Sprite doll_left1 = new Sprite(DEFAULT_SIZE, 13, 0, SpriteSheet.tiles, 16, 16);
        public static Sprite doll_left2 = new Sprite(DEFAULT_SIZE, 13, 1, SpriteSheet.tiles, 16, 16);
        public static Sprite doll_left3 = new Sprite(DEFAULT_SIZE, 13, 2, SpriteSheet.tiles, 16, 16);
        public static Sprite doll_right1 = new Sprite(DEFAULT_SIZE, 14, 0, SpriteSheet.tiles, 16, 16);
        public static Sprite doll_right2 = new Sprite(DEFAULT_SIZE, 14, 1, SpriteSheet.tiles, 16, 16);
        public static Sprite doll_right3 = new Sprite(DEFAULT_SIZE, 14, 2, SpriteSheet.tiles, 16, 16);
        public static Sprite doll_dead = new Sprite(DEFAULT_SIZE, 13, 3, SpriteSheet.tiles, 16, 16);



        ///////////////
        public static Sprite ghost_left1 = new Sprite(DEFAULT_SIZE, 6, 5, SpriteSheet.tiles, 16, 16);
        public static Sprite ghost_left2 = new Sprite(DEFAULT_SIZE, 6, 6, SpriteSheet.tiles, 16, 16);
        public static Sprite ghost_left3 = new Sprite(DEFAULT_SIZE, 6, 7, SpriteSheet.tiles, 16, 16);
        public static Sprite ghost_right1 = new Sprite(DEFAULT_SIZE, 7, 5, SpriteSheet.tiles, 16, 16);
        public static Sprite ghost_right2 = new Sprite(DEFAULT_SIZE, 7, 6, SpriteSheet.tiles, 16, 16);
        public static Sprite ghost_right3 = new Sprite(DEFAULT_SIZE, 7, 7, SpriteSheet.tiles, 16, 16);
        public static Sprite ghost_dead = new Sprite(DEFAULT_SIZE, 6, 8, SpriteSheet.tiles, 16, 16);



        public static Sprite coin = new Sprite(DEFAULT_SIZE, 13, 3, SpriteSheet.tiles, 16, 16);

        //Minvo
        public static Sprite minvo_left1 = new Sprite(DEFAULT_SIZE, 8, 5, SpriteSheet.tiles, 16, 16);
        public static Sprite minvo_left2 = new Sprite(DEFAULT_SIZE, 8, 6, SpriteSheet.tiles, 16, 16);
        public static Sprite minvo_left3 = new Sprite(DEFAULT_SIZE, 8, 7, SpriteSheet.tiles, 16, 16);

        public static Sprite minvo_right1 = new Sprite(DEFAULT_SIZE, 9, 5, SpriteSheet.tiles, 16, 16);
        public static Sprite minvo_right2 = new Sprite(DEFAULT_SIZE, 9, 6, SpriteSheet.tiles, 16, 16);
        public static Sprite minvo_right3 = new Sprite(DEFAULT_SIZE, 9, 7, SpriteSheet.tiles, 16, 16);

        public static Sprite minvo_dead = new Sprite(DEFAULT_SIZE, 8, 8, SpriteSheet.tiles, 16, 16);

        //Kondoria
        public static Sprite kondoria_left1 = new Sprite(DEFAULT_SIZE, 10, 5, SpriteSheet.tiles, 16, 16);
        public static Sprite kondoria_left2 = new Sprite(DEFAULT_SIZE, 10, 6, SpriteSheet.tiles, 16, 16);
        public static Sprite kondoria_left3 = new Sprite(DEFAULT_SIZE, 10, 7, SpriteSheet.tiles, 16, 16);

        public static Sprite kondoria_right1 = new Sprite(DEFAULT_SIZE, 11, 5, SpriteSheet.tiles, 16, 16);
        public static Sprite kondoria_right2 = new Sprite(DEFAULT_SIZE, 11, 6, SpriteSheet.tiles, 16, 16);
        public static Sprite kondoria_right3 = new Sprite(DEFAULT_SIZE, 11, 7, SpriteSheet.tiles, 16, 16);

        public static Sprite kondoria_dead = new Sprite(DEFAULT_SIZE, 10, 8, SpriteSheet.tiles, 16, 16);

        //ALL
        public static Sprite mob_dead1 = new Sprite(DEFAULT_SIZE, 15, 0, SpriteSheet.tiles, 16, 16);
        public static Sprite mob_dead2 = new Sprite(DEFAULT_SIZE, 15, 1, SpriteSheet.tiles, 16, 16);
        public static Sprite mob_dead3 = new Sprite(DEFAULT_SIZE, 15, 2, SpriteSheet.tiles, 16, 16);

        /*
        |--------------------------------------------------------------------------
        | Bomb Sprites
        |--------------------------------------------------------------------------
         */
        public static Sprite bomb = new Sprite(DEFAULT_SIZE, 0, 3, SpriteSheet.tiles, 15, 15);
        public static Sprite bomb_1 = new Sprite(DEFAULT_SIZE, 1, 3, SpriteSheet.tiles, 13, 15);
        public static Sprite bomb_2 = new Sprite(DEFAULT_SIZE, 2, 3, SpriteSheet.tiles, 12, 14);

        /*
        |--------------------------------------------------------------------------
        | FlameSegment Sprites
        |--------------------------------------------------------------------------
         */
        public static Sprite bomb_exploded = new Sprite(DEFAULT_SIZE, 0, 4, SpriteSheet.tiles, 16, 16);
        public static Sprite bomb_exploded1 = new Sprite(DEFAULT_SIZE, 0, 5, SpriteSheet.tiles, 16, 16);
        public static Sprite bomb_exploded2 = new Sprite(DEFAULT_SIZE, 0, 6, SpriteSheet.tiles, 16, 16);

        public static Sprite explosion_vertical = new Sprite(DEFAULT_SIZE, 1, 5, SpriteSheet.tiles, 16, 16);
        public static Sprite explosion_vertical1 = new Sprite(DEFAULT_SIZE, 2, 5, SpriteSheet.tiles, 16, 16);
        public static Sprite explosion_vertical2 = new Sprite(DEFAULT_SIZE, 3, 5, SpriteSheet.tiles, 16, 16);

        public static Sprite explosion_horizontal = new Sprite(DEFAULT_SIZE, 1, 7, SpriteSheet.tiles, 16, 16);
        public static Sprite explosion_horizontal1 = new Sprite(DEFAULT_SIZE, 1, 8, SpriteSheet.tiles, 16, 16);
        public static Sprite explosion_horizontal2 = new Sprite(DEFAULT_SIZE, 1, 9, SpriteSheet.tiles, 16, 16);

        public static Sprite explosion_horizontal_left_last = new Sprite(DEFAULT_SIZE, 0, 7, SpriteSheet.tiles, 16, 16);
        public static Sprite explosion_horizontal_left_last1 = new Sprite(DEFAULT_SIZE, 0, 8, SpriteSheet.tiles, 16, 16);
        public static Sprite explosion_horizontal_left_last2 = new Sprite(DEFAULT_SIZE, 0, 9, SpriteSheet.tiles, 16, 16);

        public static Sprite explosion_horizontal_right_last = new Sprite(DEFAULT_SIZE, 2, 7, SpriteSheet.tiles, 16, 16);
        public static Sprite explosion_horizontal_right_last1 = new Sprite(DEFAULT_SIZE, 2, 8, SpriteSheet.tiles, 16, 16);
        public static Sprite explosion_horizontal_right_last2 = new Sprite(DEFAULT_SIZE, 2, 9, SpriteSheet.tiles, 16, 16);

        public static Sprite explosion_vertical_top_last = new Sprite(DEFAULT_SIZE, 1, 4, SpriteSheet.tiles, 16, 16);
        public static Sprite explosion_vertical_top_last1 = new Sprite(DEFAULT_SIZE, 2, 4, SpriteSheet.tiles, 16, 16);
        public static Sprite explosion_vertical_top_last2 = new Sprite(DEFAULT_SIZE, 3, 4, SpriteSheet.tiles, 16, 16);

        public static Sprite explosion_vertical_down_last = new Sprite(DEFAULT_SIZE, 1, 6, SpriteSheet.tiles, 16, 16);
        public static Sprite explosion_vertical_down_last1 = new Sprite(DEFAULT_SIZE, 2, 6, SpriteSheet.tiles, 16, 16);
        public static Sprite explosion_vertical_down_last2 = new Sprite(DEFAULT_SIZE, 3, 6, SpriteSheet.tiles, 16, 16);

        /*
        |--------------------------------------------------------------------------
        | Brick FlameSegment
        |--------------------------------------------------------------------------
         */
        public static Sprite brick_exploded = new Sprite(DEFAULT_SIZE, 7, 1, SpriteSheet.tiles, 16, 16);
        public static Sprite brick_exploded1 = new Sprite(DEFAULT_SIZE, 7, 2, SpriteSheet.tiles, 16, 16);
        public static Sprite brick_exploded2 = new Sprite(DEFAULT_SIZE, 7, 3, SpriteSheet.tiles, 16, 16);

        /*
        |--------------------------------------------------------------------------
        | Powerups
        |-------------------------------------------------------------------------
         */
        public static Sprite powerupBombs = new Sprite(DEFAULT_SIZE, 0, 10, SpriteSheet.tiles, 16, 16);
        public static Sprite powerupFlames = new Sprite(DEFAULT_SIZE, 1, 10, SpriteSheet.tiles, 16, 16);
        public static Sprite powerupSpeed = new Sprite(DEFAULT_SIZE, 2, 10, SpriteSheet.tiles, 16, 16);
        public static Sprite powerupWallpass = new Sprite(DEFAULT_SIZE, 3, 10, SpriteSheet.tiles, 16, 16);
        public static Sprite powerupLife = new Sprite(DEFAULT_SIZE, 4, 10, SpriteSheet.tiles, 16, 16);
        public static Sprite powerupKickBomb = new Sprite(DEFAULT_SIZE, 5, 10, SpriteSheet.tiles, 16, 16);
        public static Sprite powerupFlamepass = new Sprite(DEFAULT_SIZE, 6, 10, SpriteSheet.tiles, 16, 16);


        public Sprite(int size, int x, int y, SpriteSheet sheet, int rw, int rh) {
            SIZE = size;
            pixels = new int[SIZE * SIZE];
            this.x = x * SIZE;
            this.y = y * SIZE;
            this.sheet = sheet;
            realWidth = rw;
            realHeight = rh;
            load();
            // --- Tạo JavaFX Image một lần duy nhất sau khi load pixel ---
            createFxImage();
        }

        public Sprite(int size, int color) {
            SIZE = size;
            pixels = new int[SIZE * SIZE];
            setColor(color);
            // --- Tạo JavaFX Image một lần duy nhất sau khi set color ---
            createFxImage();
        }

        private void setColor(int color) {
            for (int i = 0; i < pixels.length; i++) {
                pixels[i] = color;
            }
        }

        private void load() {
            for (int y = 0; y < SIZE; y++) {
                for (int x = 0; x < SIZE; x++) {
                    pixels[x + y * SIZE] = sheet.pixels[(x + this.x) + (y + this.y) * sheet.SIZE];
                }
            }
        }

        // --- Phương thức mới để tạo và lưu trữ JavaFX Image ---
        private void createFxImage() {
            WritableImage wr = new WritableImage(SIZE, SIZE);
            PixelWriter pw = wr.getPixelWriter();
            for (int x = 0; x < SIZE; x++) {
                for (int y = 0; y < SIZE; y++) {

                    if ( pixels[x + y * SIZE] == TRANSPARENT_COLOR) {
                        pw.setArgb(x, y, 0); // Set alpha to 0 for transparent color
                    }
                    else {
                        pw.setArgb(x, y, pixels[x + y * SIZE]);
                    }
                }
            }

            Image input = new ImageView(wr).getImage();
            double scaleFactor;
            if (this.SIZE == SCALED_SIZE) {
                scaleFactor = 1.0;
                this.fxImage = input;
            } else if (this.SIZE > 0) {
                scaleFactor = (double)SCALED_SIZE / this.SIZE;
                this.fxImage = resample(input, scaleFactor);
            } else {
                this.fxImage = input; // Hoặc trả về ảnh lỗi
            }
        }
        // --- Sửa phương thức getFxImage() để trả về Image đã tạo sẵn ---
        public Image getFxImage() {
            return this.fxImage; // Trả về Image đã được tạo và lưu trữ
        }

        private Image resample(Image input, double scaleFactor) {
            final int W = (int) input.getWidth();
            final int H = (int) input.getHeight();
            final int S = (int) Math.round(scaleFactor);

            WritableImage output = new WritableImage(
                    W * S,
                    H * S
            );

            PixelReader reader = input.getPixelReader();
            PixelWriter writer = output.getPixelWriter();

            for (int y = 0; y < H; y++) {
                for (int x = 0; x < W; x++) {
                    final int argb = reader.getArgb(x, y);
                    for (int dy = 0; dy < S; dy++) {
                        for (int dx = 0; dx < S; dx++) {
                            writer.setArgb(x * S + dx, y * S + dy, argb);
                        }
                    }
                }
            }
            return output;
        }

        // --- Phương thức movingSprite không còn cần thiết cho animation chính ---
        // Có thể xóa hoặc giữ lại nếu dùng cho mục đích khác rất đơn giản
        public static Sprite movingSprite(Sprite normal, Sprite x1, Sprite x2, int animate, int time) {
            // Logic cũ
            int calc = animate % time;
            int diff = time / 3;

            if(calc < diff) {
                return normal;
            }

            if(calc < diff * 2) {
                return x1;
            }

            return x2;
        }

        public static Sprite movingSprite(Sprite x1, Sprite x2, int animate, int time) {
            // Logic cũ
            int diff = time / 2;
            return (animate % time > diff) ? x1 : x2;
        }


        public int getSize() {
            return SIZE;
        }

        public int getPixel(int i) {
            return pixels[i];
        }

        // TODO: Các phương thức khác nếu có
    }
