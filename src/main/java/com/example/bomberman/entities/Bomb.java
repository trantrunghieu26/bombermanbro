package com.example.bomberman.entities;

import com.example.bomberman.Map.Tile;
import com.example.bomberman.Map.TileType;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;

// Lớp đại diện cho quả bom
public class Bomb extends Entity {

    private double timer = 2.5; // Thời gian bom nổ (giây). Điều chỉnh giá trị này.
    private int flameLength; // Chiều dài ngọn lửa khi nổ
    private Animation bombAnimation;
    private Animation bombAniAni;
    private double animationTime = 0; // Thời gian đã trôi qua cho animation bom

    private boolean exploded = false; // Cờ đánh dấu bom đã nổ chưa
    private boolean kick = false; // Cờ đánh dấu bom đã được kích nổ chưa.
    // private boolean playedExplosionAnimation = false; // Tạm thời không dùng cờ này

    private Player placer; // Tham chiếu đến Player đã đặt bom


    // Constructor
    // *** THÊM tham số Player placer vào constructor ***
    public Bomb(int gridX, int gridY, int flameLength, Player placer) {
        // Bom đứng yên, sử dụng sprite mặc định ban đầu, vị trí dựa trên ô lưới
        super(gridX, gridY, Sprite.bomb);
        this.flameLength = flameLength;
        this.placer = placer; // Lưu tham chiếu đến Player

        // Khởi tạo animation cho bom (nhấp nháy trước khi nổ)
        // Sử dụng các sprite bomb, bomb_1, bomb_2 trong Sprite.java
        double frameDuration = 0.2; // Thời gian hiển thị mỗi frame animation bom
        this.bombAniAni = new Animation(frameDuration, true,
                Sprite.bomb, Sprite.bomb_1, Sprite.bomb_2, Sprite.bomb_1); // Animation lặp lại
        this.bombAnimation = new Animation(frameDuration, false, Sprite.bomb_1);

        // TODO: Khởi tạo animation cho hiệu ứng nổ sau khi bom nổ (nếu bom có animation riêng khi nổ)
        // private Animation explosionAnimation;
    }


    public void triggerExplosionAt(int startGridX, int startGridY, int flameLength, Player placer) {
        System.out.println("Triggering explosion at (" + startGridX + ", " + startGridY + ") with length " + this.flameLength);

        // --- Tạo ngọn lửa TÂM VỤ NỔ ---
        this.placer.getController().addFlame(new Flame(startGridX, startGridY, Flame.FlameType.CENTER));

        // --- Lan tỏa ngọn lửa theo 4 hướng ---
        int[] dx = {0, 0, 1, -1}; // Hướng: Lên, Xuống, Phải, Trái
        int[] dy = {-1, 1, 0, 0};

        for (int i = 0; i < 4; i++) { // Duyệt 4 hướng
            int currentX = startGridX;
            int currentY = startGridY;

            for (int j = 1; j <= this.flameLength; j++) { // Lan tỏa từng bước một
                currentX += dx[i];
                currentY += dy[i];

                if (!this.placer.map.isValidGrid(currentX, currentY)) {
                    break; // Ra ngoài bản đồ
                }

                Tile currentTile = this.placer.map.getTile(currentX, currentY);
                if (currentTile == null) {
                    break; // Lỗi logic
                }

                // *** KIỂM TRA VA CHẠM VỚI CÁC THỰC THỂ ĐỘNG KHÁC TRÊN ĐƯỜNG ĐI CỦA LỬA ***
                boolean hitDynamicEntity = false;

                // Kiểm tra va chạm với Bombs khác (kích nổ dây chuyền)
                List<List<Bomb>> listOfBombLists = this.placer.getController().getBto();
                for (List<Bomb> bombList : listOfBombLists) {
                    for (Bomb otherBomb : bombList) {
                        // Chỉ kiểm tra bomb chưa nổ và ở vị trí hiện tại của ngọn lửa
                        if (!otherBomb.isExploded() && otherBomb.getGridX() == currentX && otherBomb.getGridY() == currentY) {
                            // Kích nổ bomb khác
                            System.out.println("Flame hit another bomb at (" + currentX + ", " + currentY + "). Triggering chain reaction.");
                            // Gọi triggerExplosionAt() cho bomb đó. Cần tìm cách gọi từ Bomberman hoặc cho Bomb tự gọi
                            // otherBomb.triggerExplosion(); // TODO: Cần phương thức public triggerExplosion() trong Bomb
                            // Tạm thời gọi lại trigger cho bomb khác.
                            otherBomb.setKick(true);
                            // Ngọn lửa này dừng lan tỏa theo hướng này
                        }
                    }
                }
                if (hitDynamicEntity) break; // Dừng lan tỏa theo hướng này nếu va chạm với Bomb khác

                // TODO: Kiểm tra va chạm với Enemies
                // Lửa đi xuyên qua Enemy nhưng Enemy bị tiêu diệt.
                // Tuy nhiên, trong logic hiện tại, va chạm Flame-Enemy được xử lý ở checkCollisions() sau khi tất cả update xong.
                // Có thể xử lý ở đây để dừng lan tỏa nếu game rules yêu cầu lửa dừng khi giết quái vật (không phổ biến).
                // Tạm thời bỏ qua kiểm tra Enemy ở đây, chỉ xử lý ở checkCollisions().

                // TODO: Kiểm tra va chạm với Items
                // Lửa đi xuyên qua Item nhưng Item bị phá hủy.
                // Tương tự Enemy, xử lý ở checkCollisions().

                // Kiểm tra loại Tile tĩnh
                if (currentTile.getType() == TileType.WALL) {
                    break; // Gặp tường cố định, dừng
                }

                if (currentTile.getType() == TileType.BRICK || currentTile.getType() == TileType.BOMB) {
                    // Gặp gạch có thể phá hủy - Lửa sẽ dừng tại đây
                    // Note: Việc phá hủy gạch thực tế sẽ xảy ra trong checkCollisions() khi ngọn lửa tồn tại trên ô này.
                    // Logic ở đây chỉ là tạo đầu mút và dừng lan tỏa theo hướng này.
                    this.placer.getController().addFlame(new Flame(currentX, currentY, getFlameEndType(i)));
                    break; // Dừng lan tỏa
                }

                // Nếu là EMPTY hoặc PORTAL và không va chạm với thực thể động dừng lại (như Bomb khác)
                boolean isEnd = (j == flameLength); // Là ô cuối cùng theo độ dài
                int nextX = currentX + dx[i];
                int nextY = currentY + dy[i];
                Tile nextTile = placer.map.isValidGrid(nextX, nextY) ? placer.map.getTile(nextX, nextY) : null;
                boolean nextIsWallOrBrickorBomb = (nextTile != null && (nextTile.getType() == TileType.WALL || nextTile.getType() == TileType.BRICK || nextTile.getType() == TileType.BOMB));
                // TODO: Kiểm tra ô tiếp theo có Bomb khác không (để vẽ đầu mút đúng)
                // for (Bomb otherBomb : bombs) {
                //     if (!otherBomb.isExploded() && otherBomb.getGridX() == nextX && otherBomb.getGridY() == nextY) {
                //         nextIsOtherBomb = true;
                //         break;
                //     }
                // }


                // Nếu là ô cuối cùng theo độ dài HOẶC ô tiếp theo là vật cản (Tường, Gạch) HOẶC ô tiếp theo có Bomb khác
                if (isEnd || nextIsWallOrBrickorBomb) {
                    this.placer.getController().addFlame(new Flame(currentX, currentY, this.getFlameEndType(i))); // Tạo đầu mút lửa
                } else {
                    this.placer.getController().addFlame(new Flame(currentX, currentY, this.getFlameSegmentType(i))); // Tạo đoạn giữa
                }
            }
        }
    }


    // Phương thức update được gọi mỗi frame
    public void update(double deltaTime) {

        animationTime += deltaTime; // Tăng thời gian animation đã trôi qua
        //nếu bị kích nổ.
        if (kick) {
            // Giảm thời gian đếm ngược của bom
            this.timer -= deltaTime;

            if (this.timer <= 0.0 && !exploded) {
                // Thời gian hết, bom nổ
                exploded = true;
                System.out.println("Bomb timer finished at (" + gridX + ", " + gridY + ")"); // Log để debug

                // *** KHI BOM NỔ, YÊU CẦU triggerExplosionAt KÍCH HOẠT VỤ NỔ LỬA ***
                    this.triggerExplosionAt(gridX, gridY, flameLength, placer); // Truyền cả placer để xử lý điểm/item

                // *** KHI BOM NỔ, TĂNG SỐ BOM CỦA NGƯỜI ĐẶT BOM LÊN ***
                if (this.placer != null && this.placer.getBombs().contains(this)) {
                    this.placer.increaseBombNumber(1); // Tăng số bom cho người đặt
                    System.out.println("Player at (" + placer.getGridX() + ", " + placer.getGridY() + ") can place " + placer.getBombNumber() + " more bombs.");
                }


                // Sau khi kích hoạt nổ lửa, bom này có thể được đánh dấu để loại bỏ ngay lập tức
                // (Hiệu ứng lửa sẽ do các đối tượng Flame quản lý)
                this.remove(); // Đánh dấu bom để loại bỏ khỏi danh sách active
            }

            // Cập nhật sprite bom CHỈ KHI CHƯA NỔ
            if (!exploded) {
                this.sprite = this.bombAniAni.getFrame(animationTime);
            } else {
                return;
                // Nếu đã nổ, sprite bom không còn hiển thị nữa (nó sẽ bị remove)
                // Hoặc bạn có thể vẽ một frame cuối cùng của bom nổ nếu muốn
                // this.sprite = Sprite.bomb_exploded;
            }
        } else {
            if (!exploded) {
                this.sprite = bombAnimation.getFrame(animationTime);
            } else {
                return;
            }
        }
    }

    // Phương thức render được gọi mỗi frame để vẽ bom
    @Override
    public void render(GraphicsContext gc) {
        // Chỉ vẽ nếu có sprite và chưa bị đánh dấu loại bỏ
        // Sau khi nổ (exploded=true), nó sẽ bị remove nên không vẽ nữa
        if (sprite != null && !removed) {
            gc.drawImage(this.sprite.getFxImage(), pixelX, pixelY);
        }
    }

    // --- Getters ---
    public boolean isExploded() {
        return exploded;
    }

    public int getFlameLength() {
        return flameLength;
    }

    // Phương thức trả về vị trí lưới (cần cho kiểm tra trùng lặp khi đặt bom)
    public int getGridX() { return gridX; }
    public int getGridY() { return gridY; }

    // Getter cho Placer
    public Player getPlacer() {
        return placer;
    }

    public boolean getKick() { return kick; }
    public void setKick(boolean kick) { this.kick = kick; }

    private Flame.FlameType getFlameEndType(int directionIndex) {
        switch (directionIndex) {
            case 0: return Flame.FlameType.VERTICAL_END_UP;
            case 1: return Flame.FlameType.VERTICAL_END_DOWN;
            case 2: return Flame.FlameType.HORIZONTAL_END_RIGHT;
            case 3: return Flame.FlameType.HORIZONTAL_END_LEFT;
            default: return Flame.FlameType.CENTER;
        }
    }

    private Flame.FlameType getFlameSegmentType(int directionIndex) {
        switch (directionIndex) {
            case 0: case 1: return Flame.FlameType.VERTICAL; // Lên, Xuống
            case 2: case 3: return Flame.FlameType.HORIZONTAL; // Phải, Trái
            default: return Flame.FlameType.CENTER;
        }
    }

    // TODO: Các phương thức khác nếu cần
}