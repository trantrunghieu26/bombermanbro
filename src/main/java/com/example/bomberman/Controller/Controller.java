package com.example.bomberman;

import com.example.bomberman.Input.InputHandler;
import com.example.bomberman.Map.Map;
import com.example.bomberman.Map.MapData;
import com.example.bomberman.Map.Tile;
import com.example.bomberman.Map.TileType;
import com.example.bomberman.entities.*;
import com.example.bomberman.graphics.Sprite;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Controller {

    //               //    attributes        //        //

    public MapData mapData;

    // Tính kích thước canvas dựa vào map size
    public int canvasWidth;
    public int canvasHeight;
    public int playerStartX = -1;
    public int playerStartY = -1;

    private Map gameMap;
    private GraphicsContext gc;
    private Canvas canvas;
    private List<Bomb> availableBomb;
    private List<List<Bomb>> bto;

    // Danh sách quản lý thực thể
    private Player player;
    private List<Flame> flames = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>(); // *** THÊM: Danh sách quản lý quái vật ***
    // TODO: private List<Item> items = new ArrayList<>();

    private Group root;
    public Scene scene;

    //             //         Method             //     //

    public Controller() {
        mapData = new MapData(11);
        gameMap = new Map(mapData);

        char[][] charMap = this.mapData.getMap();

        // Duyệt map lần đầu để tìm vị trí player
        for (int i = 0; i < mapData.getRows(); i++) {
            for (int j = 0; j < mapData.getCols(); j++) {
                if (charMap[i][j] == 'p') {
                    this.playerStartX = j;
                    this.playerStartY = i;
                    break; // Tìm thấy player, thoát vòng lặp trong
                }
            }
            if (this.playerStartX != -1) {
                break; // Tìm thấy player, thoát vòng lặp ngoài
            }
        }
    }

    public void controllerActive() {

        if (this.playerStartX != -1) {
            player = new Player(this.playerStartX, this.playerStartY, this.getMap(), this);
        } else {
            System.err.println("Error: Player start position ('p') not found in map data!");
        }

        availableBomb = new ArrayList<>();
        bto = new ArrayList<>();
        bto.add(availableBomb);

        // Tính kích thước canvas dựa vào map size
        canvasWidth = mapData.getCols() * Sprite.SCALED_SIZE;
        canvasHeight = mapData.getRows() * Sprite.SCALED_SIZE;

        // Tạo canvas với size đúng map
        canvas = new Canvas(canvasWidth, canvasHeight);
        gc = canvas.getGraphicsContext2D(); // Lưu vào thuộc tính của lớp
        root = new Group(canvas);
        scene = new Scene(root);

        // khởi tạo các đối tượng thực thể //
        char[][] charMap = this.mapData.getMap();

        for (int i = 0; i < mapData.getRows(); i++) {
            for (int j = 0; j < mapData.getCols(); j++) {
                switch (charMap[i][j]) {
                    case '1':
                        this.enemies.add(new Balloom(j, i, gameMap));
                        break;
                    case '2':
                        this.enemies.add(new Oneal(j, i, gameMap));
                        break;
                    case '3':
                        this.enemies.add(new Doll(j, i, gameMap));
                        break;
                    case '4':
                        this.enemies.add(new Ghost(j, i, gameMap));
                        break;
                    case '5':
                        this.enemies.add(new Minvo(j, i, gameMap));
                        break;
                    case '6':
                        this.enemies.add(new Kondoria(j, i, gameMap));
                        break;
                    case '+':
                        if (player != null) {
                            this.availableBomb.add(new Bomb(j, i, 1, player));
                        } else {
                            System.err.println("Error: Player start position ('p') not found in map data!");
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        if (player != null) {
            this.bto.add(player.getBombs());
            InputHandler inputHandler = new InputHandler(this.scene, player);
        }
    }

    public List<List<Bomb>> getBto() { return this.bto; }

    // *** PHƯƠNG THỨC KIỂM TRA VA CHẠM CHUNG (ĐÃ SỬA ĐỔI VÀ THÊM LOGIC) ***
    public void checkCollisions() {
        // --- Kiểm tra va chạm Player với Enemy ---
        // Duyệt qua danh sách Enemies
        if (player != null && player.isAlive() && !player.isInvincible()) { // Chỉ kiểm tra nếu Player còn sống và không bất tử
            Iterator<Enemy> enemyIterator = this.enemies.iterator();
            while (enemyIterator.hasNext()) {
                Enemy enemy = enemyIterator.next();
                // Chỉ kiểm tra va chạm nếu Enemy còn sống
                if (!enemy.isAlive()) continue;

                // Kiểm tra va chạm giữa Player và Enemy bằng Bounding Box
                if (checkCollision(player.getPixelX(), player.getPixelY(), enemy.getPixelX(), enemy.getPixelY())) {
                    // TODO: Xử lý Player bị trúng đòn bởi Enemy
                    System.out.println("Player collided with Enemy at (" + enemy.getGridX() + ", " + enemy.getGridY() + ")");
                    player.die(); // Tạm thời cho Player chết
                    // TODO: Xử lý game over
                }
            }
        }

        // --- Kiểm tra va chạm Flame với các thực thể khác ---
        Iterator<Flame> flameIterator = this.getFlames().iterator();
        while (flameIterator.hasNext()) {
            Flame flame = flameIterator.next();
            // Chỉ kiểm tra va chạm nếu ngọn lửa đang hoạt động và chưa bị đánh dấu loại bỏ
            if (flame.isRemoved()) continue;

            int flameGridX = flame.getGridX();
            int flameGridY = flame.getGridY();

            // Kiểm tra va chạm giữa Flame và Bounding Box của Player (đã có logic này)
            // Chỉ kiểm tra nếu Player chưa chết VÀ chưa bất tử
            if (player != null && player.isAlive() && !player.isInvincible()) {
                if (checkCollision(player.getPixelX(), player.getPixelY(), flame.getPixelX(), flame.getPixelY())) {
                    System.out.println("Player hit by flame at (" + flameGridX + ", " + flameGridY + ")");
                    player.die(); // Gọi phương thức die() của Player
                }
            }

            // *** THÊM: Kiểm tra va chạm Flame với Enemy ***
            Iterator<Enemy> enemyIterator = enemies.iterator(); // Tạo Iterator mới để tránh ConcurrentModificationException
            while (enemyIterator.hasNext()) {
                Enemy enemy = enemyIterator.next();
                // Chỉ kiểm tra va chạm nếu Enemy còn sống
                if (!enemy.isAlive()) continue;

                if (checkCollision(enemy.getPixelX(), enemy.getPixelY(), flame.getPixelX(), flame.getPixelY())) {
                    // TODO: Xử lý Enemy bị trúng đòn bởi Flame
                    System.out.println("Enemy hit by flame at (" + enemy.getGridX() + ", " + enemy.getGridY() + ")");
                    enemy.takeHit(); // Gọi phương thức takeHit() của Enemy
                    // Sau khi enemy chết, ngọn lửa có thể biến mất hoặc tồn tại hết đời tùy game
                    // Tạm thời để ngọn lửa tồn tại hết vòng đời animation của nó.
                    // flame.remove(); // Nếu muốn lửa biến mất ngay sau khi tiêu diệt quái vật
                }
            }

            // --- Kiểm tra va chạm Flame với Bricks (Gạch phá hủy được) ---
            // Lửa sẽ phá hủy gạch và dừng lại ở đó (logic dừng lan đã ở triggerExplosionAt)
            // Giờ là logic phá hủy gạch khi ngọn lửa đang tồn tại trên ô đó
            // Chỉ phá hủy gạch một lần bởi một ngọn lửa
            Tile tile = gameMap.getTile(flameGridX, flameGridY);
            if (tile != null && (tile.getType() == TileType.BRICK || tile.getType() == TileType.BOMB)) {
                // TODO: Phá hủy gạch (cập nhật tile trên map)
                // Chỉ thay đổi loại Tile nếu nó vẫn là BRICK (tránh phá nhiều lần)
                    System.out.println("Flame destroyed brick at (" + flameGridX + ", " + flameGridY + ")");
                    gameMap.setTile(flameGridX, flameGridY, Tile.createTileFromChar(flameGridX, flameGridY, ' ')); // Thay brick thành EMPTY tile
                    // TODO: Có khả năng tạo Item tại vị trí này sau khi phá hủy gạch
            }

            // TODO: Kiểm tra va chạm Flame với Items
            // TODO: Kiểm tra va chạm Flame với Bombs khác (kích nổ dây chuyền)

        } // Hết vòng lặp Flames


        // TODO: Kiểm tra va chạm Player với Item
        // TODO: Kiểm tra va chạm Player với Bomb (khi Bomb đã đặt và Player muốn đi vào ô đó)
        // TODO: Kiểm tra va chạm Enemy với Bomb (enemy không đi qua bomb)
        // TODO: Kiểm tra va chạm Enemy với Item (enemy không nhặt item)
    }

    // *** PHƯƠNG THỨC HELPER KIỂM TRA VA CHẠM GIỮA HAI HỘP VA CHẠM (Bounding Box) ***
    // Nhận vào pixel X, Y của góc trên bên trái của hai thực thể
    public boolean checkCollision(double x1, double y1, double x2, double y2) {
        // Sử dụng SCALED_SIZE làm kích thước bounding box tạm thời cho tất cả thực thể
        double size1 = Sprite.SCALED_SIZE;
        double size2 = Sprite.SCALED_SIZE;

        // Điều chỉnh kích thước bounding box một chút nếu cần để chính xác hơn (ví dụ: co nhỏ lại)
        // double buffer1 = 4.0; // Buffer cho entity 1
        // double buffer2 = 4.0; // Buffer cho entity 2
        // double effectiveX1 = x1 + buffer1;
        // double effectiveY1 = y1 + buffer1;
        // double effectiveSize1 = size1 - 2 * buffer1;
        // double effectiveX2 = x2 + buffer2;
        // double effectiveY2 = y2 + buffer2;
        // double effectiveSize2 = size2 - 2 * buffer2;

        // Kiểm tra xem hai hộp va chạm có overlap không
        return x1 < x2 + size2 && x1 + size1 > x2 &&
                y1 < y2 + size2 && y1 + size1 > y2;

        // Hoặc với buffer:
        // return effectiveX1 < effectiveX2 + effectiveSize2 && effectiveX1 + effectiveSize1 > effectiveX2 &&
        //        effectiveY1 < effectiveY2 + effectiveSize2 && effectiveY1 + effectiveSize1 > effectiveY2;
    }

    public void updateForAll(double deltaTime) {
        // --- Vòng lặp Update ---
        player.update(deltaTime);

        // Cập nhật Bombs
        Iterator<List<Bomb>> Lomb = this.bto.iterator();
        while (Lomb.hasNext()) {
            List<Bomb> omb = Lomb.next();
            Iterator<Bomb> bombIterator = omb.iterator();
            while (bombIterator.hasNext()) {
                Bomb bomb = bombIterator.next();
                bomb.update(deltaTime);
                if (bomb.isRemoved()) {
                    bombIterator.remove();
                }
            }
        }

        // Cập nhật Flames
        Iterator<Flame> flameIterator = this.getFlames().iterator();
        while (flameIterator.hasNext()) {
            Flame flame = flameIterator.next();
            flame.update(deltaTime);
            if (flame.isRemoved()) {
                flameIterator.remove();
            }
        }


        //  Cập nhật Enemies
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            enemy.update(deltaTime);
            if (enemy.isRemoved()) {
                enemyIterator.remove(); // Loại bỏ quái vật đã chết hoàn toàn
            }
        }

        // TODO: Items update
    }

    public void renderForAll() {
        // --- Vòng lặp Render ---
        this.gc.clearRect(0, 0, this.canvas.getWidth(), this.canvas.getHeight());
        gameMap.render(this.gc);

        // Thứ tự vẽ: Items -> Enemies -> Bombs -> Player -> Flames
        // TODO: Render Items
        // *** THÊM: Render Enemies ***
        for (Enemy enemy : this.enemies) {
            enemy.render(this.gc);
        }

        // Render Bombs
        List<List<Bomb>> listOfBombLists = this.bto;
        for (List<Bomb> bombList : listOfBombLists) {
            for (Bomb bomb : bombList) {
                bomb.render(this.gc);
            }
        }

        // Render Player
        if (!player.isRemoved()) {
            player.render(this.gc);
        }

        // Render Flames
        for (Flame flame : this.getFlames()) {
            flame.render(gc);
        }

        // TODO: Vẽ UI
        // TODO: Vẽ các yếu tố UI (điểm số, thời gian, ...)
    }

    public Map getMap() { return this.gameMap; }

    public void addFlame(Flame flame) {
        this.flames.add(flame);
    }

    public List<Flame> getFlames() {
        return this.flames;
    }

    public Player getPlayer() { return player; }
    public void setPlayer(Player player) { this.player = player; }
}
