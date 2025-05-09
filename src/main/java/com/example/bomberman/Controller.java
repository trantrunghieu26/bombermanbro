package com.example.bomberman;

import com.example.bomberman.Input.InputHandler;
import com.example.bomberman.Map.Map;
import com.example.bomberman.Map.MapData;
import com.example.bomberman.Map.Tile;
import com.example.bomberman.Map.TileType;
import com.example.bomberman.entities.*;
import com.example.bomberman.entities.Items.*;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite;
import com.example.bomberman.graphics.SpriteSheet;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Controller {

    //               //    attributes        //        //

    private Random random = new Random();
    public View view;
    private int score = 0;
    public static final int UI_PANEL_HEIGHT = 32;
    private java.util.Map<String, Character> hiddenItemsData;
    private final int MAX_LEVEL = 10;
    public String[] menuOptions = {"Start Game", "Settings", "Music: ON"};
    public String[] Animations = {"Animation 0", "Animation 1", "Animation 2"};

    public boolean isMusicOn = true;
    public boolean[] isAnimations = {true, false, false};

    public int selectedOptionIndex = 0;
    public int selectSetting = 0;

    // Tính kích thước canvas dựa vào map size
    public int canvasWidth;
    public int canvasHeight;
    public int playerStartX = -1;
    public int playerStartY = -1;
    private int portalGridX = -1; // Lưu vị trí Portal X
    private int portalGridY = -1;

    private Map gameMap;
    private MapData mapData;
    private boolean portalActivated = false; // Cờ cho biết Portal đã mở chưa
    public GraphicsContext gc;
    public Canvas canvas;
    private double levelTimeRemaining;
    private static final double LEVEL_DURATION_SECONDS = 200.0;

    // Danh sách quản lý thực thể
    private Player player;
    private List<Flame> flames = new ArrayList<>();
    private List<Enemy> enemies = new ArrayList<>(); // *** THÊM: Danh sách quản lý quái vật ***
    private final List<Item> items = new ArrayList<>(); // Danh sách quản lý các đối tượng Item
    private List<Bomb> availableBomb;
    private List<List<Bomb>> bto;
    // TODO: private List<Item> items = new ArrayList<>();

    public Group root;
    public Scene scene;

    //             //         Method             //     //

    public Controller() {
        SpriteSheet.tiles = new SpriteSheet("res/textures/classic1.png", 256);
    }

    public void controllerActive(Bomberman myGame) {
        try {
            System.out.println("Loading level " + myGame.currentLevel + "..."); // Log bắt đầu tải level

            Sprite.updateAllSpritesFromSheet(SpriteSheet.tiles);

            this.view = new View();
            myGame.view = this.view;

            this.score = 0;
            this.levelTimeRemaining = LEVEL_DURATION_SECONDS;
            this.portalActivated = false;
            this.portalGridX = -1;
            this.portalGridY = -1;

            System.out.println("Score, Timer, Portal state reset.");
            System.out.println("Level timer reset to " + LEVEL_DURATION_SECONDS + " seconds.");

            // Xóa các thực thể và animation từ màn chơi trước
            this.bto = new ArrayList<>();
            this.flames = new ArrayList<>();
            this.enemies = new ArrayList<>();
            if (this.items != null) {
                this.items.clear();
            }
            if (view.getTemporaryAnimations() != null) {
                view.getTemporaryAnimations().clear();
            }
            player = null; // Đặt player về null trước khi tạo mới
            this.gameMap = null;
            this.mapData = null;

            this.mapData = new MapData(myGame.currentLevel);
            this.mapData.setLevel(myGame.currentLevel);
            this.gameMap = new Map(mapData);


            // --- 2. Lấy thông tin Item được giấu từ MapData ---
            // hiddenItemsData là java.util.Map
            hiddenItemsData = mapData.getHiddenItems();
            System.out.println("Loaded " + hiddenItemsData.size() + " hidden items from map data."); // Log

            // 3. Cập nhật kích thước canvas
            canvasWidth = mapData.getCols() * Sprite.SCALED_SIZE;
            canvasHeight = mapData.getRows() * Sprite.SCALED_SIZE + UI_PANEL_HEIGHT;
            if (canvas == null) {
                canvas = new Canvas(canvasWidth, canvasHeight);
                gc = canvas.getGraphicsContext2D();
            } else {
                canvas.setWidth(canvasWidth);
                canvas.setHeight(canvasHeight);
            }

            this.root = new Group(this.canvas);
            this.scene = new Scene(this.root);

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

            if (this.playerStartX != -1 && player == null) {
                System.out.println("Creating Player at grid (" + playerStartX + ", " + playerStartY + ")"); // Log

                player = new Player(this.playerStartX, this.playerStartY, this.getMap(), this);
                if (gameMap != null) {
                    gameMap.setTile(playerStartX, playerStartY, Tile.createTileFromChar(playerStartX, playerStartY, ' ')); // ' ' là Grass/EMPTY
                    System.out.println("Tile at (" + playerStartX + ", " + playerStartY + ") changed to EMPTY for Player."); // Log
                } else {
                    System.err.println("Error: gameMap is null while setting Player tile to Grass.");
                }
            } else {
                System.err.println("Error: Player start position ('p') not found in map data!");
            }

            availableBomb = new ArrayList<>();
            bto.add(availableBomb);

            // Danh sách lưu vị trí portal và item ẩn
            java.util.Map<String, Character> hiddenItemsData = new java.util.HashMap<>();

            for (int i = 0; i < mapData.getRows(); i++) {
                for (int j = 0; j < mapData.getCols(); j++) {
                    char ch = charMap[i][j];
                    switch (ch) {
                        case '1': enemies.add(new Balloom(j, i, gameMap)); break;
                        case '2': enemies.add(new Oneal(j, i, gameMap)); break;
                        case '3': enemies.add(new Doll(j, i, gameMap)); break;
                        case '4': enemies.add(new Ghost(j, i, gameMap)); break;
                        case '5': enemies.add(new Minvo(j, i, gameMap)); break;
                        case '6': enemies.add(new Kondoria(j, i, gameMap)); break;
                        case '+':
                            if (player != null) {
                                availableBomb.add(new Bomb(j, i, player.getFlameLength(), player));
                            }
                            break;
                        case 'x':
                            if (portalGridX == -1) {
                                portalGridX = j;
                                portalGridY = i;
                                System.out.println("Portal found at (" + j + ", " + i + ")");
                            }
                            break;
                        case 'b': case 'f': case 's': case 'l': case 'a':
                            hiddenItemsData.put(j + "," + i, ch);
                            System.out.println("Hidden item '" + ch + "' at (" + j + ", " + i + ")");
                            break;
                        default: break;
                    }
                }
            }

            if (player != null) {
                bto.add(player.getBombs());
            }

            System.out.println("Load new game successfully!");

            myGame.view = this.view;

            // Kiểm tra canvas và player không null sau khi loadLevel
            if (this.canvas == null || this.getPlayer() == null) {
                System.err.println("Fatal Error: Game initialization failed during level loading.");
                // TODO: Hiển thị thông báo lỗi cho người dùng và thoát game
                return;
            }

            myGame.getPrimaryStage().setScene(this.scene);

            myGame.inputHandler = new InputHandler(this.scene, this.player, myGame);

            myGame.getPrimaryStage().show();

            System.out.println("load full for game!");


        } catch (Exception e) {
            // Bắt bất kỳ ngoại lệ nào xảy ra trong quá trình tải level
            System.err.println("Error loading level " + myGame.currentLevel + ": " + e.getMessage());
            e.printStackTrace(); // In stack trace để xem chi tiết lỗi
            // TODO: Xử lý lỗi tải level (ví dụ: hiển thị thông báo lỗi cho người chơi, thoát game)
            // Đặt canvas và player về null để start() biết rằng có lỗi
            canvas = null;
            player = null;
            gameMap = null;
            hiddenItemsData = null; // Đảm bảo dữ liệu item cũng bị reset
        }
    }

    public List<List<Bomb>> getBto() { return this.bto; }

    // *** PHƯƠNG THỨC KIỂM TRA VA CHẠM CHUNG (ĐÃ SỬA ĐỔI VÀ THÊM LOGIC) ***
    public void checkCollisions(double deltaTime) {
        // --- Kiểm tra va chạm Player với Enemy ---
        // Duyệt qua danh sách Enemies
        // Chỉ kiểm tra nếu Player còn sống và không bất tử
        if (player != null && player.isAlive() && !player.isInvincible()) {
            Iterator<Enemy> enemyIterator = this.enemies.iterator();
            while (enemyIterator.hasNext()) {
                Enemy enemy = enemyIterator.next();
                // Chỉ kiểm tra va chạm nếu Enemy còn sống
                if (!enemy.isAlive()) continue;

                // Kiểm tra va chạm giữa Player và Enemy bằng Bounding Box
                if (checkCollision(player.getPixelX(), player.getPixelY(), enemy.getPixelX(), enemy.getPixelY()) &&
                    !checkCollision(player.getLastPixelX(deltaTime), player.getLastPixelY(deltaTime), enemy.getLastPixelX(deltaTime), enemy.getLastPixelY(deltaTime))) {
                    System.out.println("Player collided with Enemy at (" + enemy.getGridX() + ", " + enemy.getGridY() + ")");
                    if (this.player.getLives() > 0) {
                        this.player.decreaseLive();
                        return;
                    } else {
                        this.player.die();
                    }
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
                if (checkCollision(player.getPixelX(), player.getPixelY(), flame.getPixelX(), flame.getPixelY()) && flame.getAnimationTime() <= deltaTime) {
                    System.out.println("Player hit by flame at (" + flameGridX + ", " + flameGridY + ")");
                    if (this.player.getLives() > 0) {
                        this.player.decreaseLive();
                        return;
                    } else {
                        this.player.die();
                    }
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
                    enemy.takeHit();
                }
            }

            // --- Kiểm tra va chạm Flame với Bricks (Gạch phá hủy được) ---
            this.brickHitByFlame(flameGridX, flameGridY);
        }
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

    public void updateForAll(double deltaTime, Bomberman myGame) {
        // --- Vòng lặp Update ---

        if (levelTimeRemaining > 0) { // Chỉ đếm ngược nếu thời gian còn > 0
            levelTimeRemaining -= deltaTime;
        }

        if (player != null) {
            player.update(deltaTime);
        }

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
        Iterator<Flame> flameIterator = this.flames.iterator();
        while (flameIterator.hasNext()) {
            Flame flame = flameIterator.next();
            flame.update(deltaTime);
            if (flame.isRemoved()) {
                flameIterator.remove();
            }
        }

        // Cập nhật Items
        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            item.update(deltaTime);
            if (!item.isActive()) {
                iterator.remove();
            }
        }

        view.updateTemporaryAnimations(deltaTime, this);

        if (!portalActivated) {
            //sửa lại điều kiện portal ở đây;
            boolean activationConditionMet = true; // Điều kiện test
            if (activationConditionMet) {
                portalActivated = true;
                System.out.println("Portal Activated!");
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

        handleKickBombTrigger();
        handlePlayerItemCollisions();
        this.checkCollisions(deltaTime);
        handlePortalTransition(myGame);
    }

    public void handlePortalTransition(Bomberman myGame){
        if (player != null && player.isAlive() && portalActivated && portalGridX != -1) {

            // --- Tính toán vị trí TÂM của Player ---
            double playerCenterX = player.getPixelX() + Sprite.SCALED_SIZE / 2.0;
            double playerCenterY = player.getPixelY() + Sprite.SCALED_SIZE / 2.0;

            // --- Xác định ô lưới mà TÂM Player đang ở trong ---
            int playerGridX = (int) Math.floor(playerCenterX / Sprite.SCALED_SIZE);
            int playerGridY = (int) Math.floor(playerCenterY / Sprite.SCALED_SIZE);
            // Lưu ý: Nếu dùng UI Panel, Y của tâm phải trừ offset trước khi chia:
            // int playerGridY = (int) Math.floor((playerCenterY - UI_PANEL_HEIGHT) / Sprite.SCALED_SIZE);

            // --- Thêm Log Debug để xem tọa độ tâm và grid tính từ tâm ---
            if (Math.abs(playerGridX - portalGridX) <= 1 && Math.abs(playerGridY - portalGridY) <= 1) {
                System.out.printf("Portal Check (Center): Player Center Grid(%d, %d) vs Portal Grid(%d, %d) | Player Center Pixel(%.2f, %.2f)\n",
                        playerGridX, playerGridY, portalGridX, portalGridY,
                        playerCenterX, playerCenterY);
            }
            // ---------------------------------------------------------

            // --- So sánh tọa độ lưới tính từ TÂM ---
            if (playerGridX == portalGridX && playerGridY == portalGridY) {
                System.out.println("Player entered portal! (Center Grid match)");
                if (levelTimeRemaining > 0) { // Chỉ cộng nếu còn thời gian
                    int timeBonus = (int)(levelTimeRemaining * 2); // Ví dụ: 2 điểm/giây
                    System.out.println("Time Bonus: " + timeBonus);
                    addScore(timeBonus); // Gọi hàm cộng điểm của Bomberman
                }
                myGame.currentLevel += 1;
                if (myGame.currentLevel <= MAX_LEVEL) {
                    System.out.println("Loading next level: " + myGame.currentLevel);
                    myGame.con.controllerActive(myGame);
                    myGame.view = this.view;
                } else {
                    System.out.println("CONGRATULATIONS! YOU BEAT THE GAME!");
                    if (myGame.getPrimaryStage() != null) {
                        myGame.currentState = GameState.GAME_WON;
                    }
                }
                return;
            }
        } else if (this.player.isRemoved() || this.player == null) {
            myGame.currentState = GameState.GAME_OVER;
        }
    }

    // --- Phương thức để tạo Item tại một vị trí lưới (được gọi sau khi animation gạch vỡ kết thúc) ---
    // Phương thức này giờ nhận vào loại Item cần tạo
    public void spawnItemAt(int gridX, int gridY, char itemTypeChar) {
        // TODO: Triển khai logic tạo đối tượng Item cụ thể dựa trên itemTypeChar
        // và thêm nó vào danh sách items

        Item newItem = null; // Khai báo biến Item mới

        switch (itemTypeChar) {
            case 'b':
                newItem = new BombItem(gridX, gridY); // Tạo đối tượng BombItem
                System.out.println("Spawned Bomb Item at (" + gridX + ", " + gridY + ")");
                break;
            case 'f':
                // TODO: Tạo đối tượng FlameItem
                newItem = new FlameItem(gridX, gridY);
                System.out.println("Spawned Flame Item at (" + gridX + ", " + gridY + ")");
                break;
            case 's':
                // TODO: Tạo đối tượng SpeedItem
                newItem = new SpeedItem(gridX, gridY);
                System.out.println("Spawned Speed Item at (" + gridX + ", " + gridY + ")");
                break;
            case 'l':
                // TODO: Tạo đối tượng LifeItem
                newItem = new LifeItem(gridX, gridY);
                System.out.println("Spawned Life Item at (" + gridX + ", " + gridY + ")");
                break;
            case 'a':
                newItem = new KickBombItem(gridX, gridY);
                System.out.println("Spawned KickBomb Item at (" + gridX + ", " + gridY + ")");
                break;
            default:
                System.err.println("Warning: Unknown item type character '" + itemTypeChar + "' at (" + gridX + ", " + gridY + ")");
                break;
        }

        // Thêm Item mới tạo (nếu có) vào danh sách items
        if (newItem != null) {
            items.add(newItem);
            System.out.println("Item added to list. Total items: " + items.size()); // Log
        }
    }

    // hiệu ứng POrtal
    public double getElapsedTime() {
        // Cách tính đơn giản nhất là lấy tổng thời gian trừ đi thời gian còn lại
        // Đảm bảo không trả về giá trị âm nếu levelTimeRemaining có thể nhỏ hơn 0
        return Math.max(0, LEVEL_DURATION_SECONDS - levelTimeRemaining);
    }

    // --- Phương thức được gọi từ Bomb.explode() khi ngọn lửa chạm gạch ---
    public void brickHitByFlame(int gridX, int gridY) {
        // Kiểm tra biên
        if (gridX >= 0 && gridX < mapData.getCols() && gridY >= 0 && gridY < mapData.getRows()) {
            Tile tile = this.gameMap.getTile(gridX, gridY);
            // Kiểm tra xem ô đó có phải là gạch không và chưa bị phá hủy
            if (tile != null && tile.getType() == TileType.BRICK) {
                System.out.println("Flame hit brick at (" + gridX + ", " + gridY + "). Notifying game manager."); // Log
                System.out.println("Bomberman received notification: Brick destroyed at (" + gridX + ", " + gridY + ")"); // Log
                addScore(10);
                // --- 1. Tạo animation gạch vỡ tại vị trí này ---
                // Sử dụng các sprite brick_exploded, brick_exploded1, brick_exploded2
                // Thời gian animation có thể lấy từ Flame hoặc đặt cố định
                double animationDuration = 0.5; // Ví dụ: animation kéo dài 0.5 giây
                double frameDuration = animationDuration / 3; // Chia đều cho 3 frame

                // Sử dụng lớp Animation đã import
                Animation brickExplosionAnim = new Animation(frameDuration, false,
                        Sprite.brick_exploded, Sprite.brick_exploded1, Sprite.brick_exploded2);

                // Sử dụng lớp TemporaryAnimation đã import
                TemporaryAnimation brickAnim = new TemporaryAnimation(gridX, gridY, brickExplosionAnim);

                // Thêm animation vào danh sách quản lý
                view.getTemporaryAnimations().add(brickAnim);

                // TODO: Phát âm thanh gạch vỡ
            }
        }
    }

    // --- THÊM PHƯƠNG THỨC ĐỂ TĂNG ĐIỂM ---
    public void addScore(int points) {
        if (points > 0) {
            this.score += points;
            System.out.println("Score increased by " + points + ". Total score: " + this.score); // Log (tùy chọn)
        }
    }

    public boolean isBombAtGrid(int gridX, int gridY) {
        for (List<Bomb> thebomb : bto) {
            for (Bomb bomb : thebomb) {
                if (bomb.isRemoved() && bomb.getGridX() == gridX && bomb.getGridY() == gridY) {
                    return true;
                }
            }
        }
        return false;
    }

    public void handleKickBombTrigger(){
        if (player != null && player.kickableBombPending != null && player.kickDirectionPending != Direction.NONE) {
            System.out.println("!!!!!! KICK TRIGGER POINT REACHED (Pending State)! Bomb: (" + player.kickableBombPending.getGridX() + "," + player.kickableBombPending.getGridY() + ") Dir: " + player.kickDirectionPending);

            // Get the bomb and direction stored by checkCollision
            Bomb bombToKick = player.kickableBombPending;
            Direction directionToKick = player.kickDirectionPending;
            // Or fixed value

            // Trigger the kick
            bombToKick.startKicking(directionToKick, Bomb.KICK_SPEED_CONSTANT);

            // IMPORTANT: Reset the pending state immediately after triggering
            player.kickableBombPending = null;
            player.kickDirectionPending = Direction.NONE;

        }

    }

    public void handlePlayerItemCollisions() {
        if (player == null || !player.isAlive()) return; // Không xử lý nếu Player không hợp lệ

        Iterator<Item> itemIterator = items.iterator();
        while (itemIterator.hasNext()) {
            Item item = itemIterator.next();
            if (item.isActive() && player.collidesWith(item)) { // Giả sử Player.collidesWith(item) đã có
                System.out.println("Player collided with Item at (" + item.getGridX() + ", " + item.getGridY() + ")");
                item.applyEffect(player);
                item.setActive(false); // Đánh dấu để xóa (hoặc xử lý hiệu ứng biến mất)
            }
            // Xóa item không active (nên đặt sau vòng lặp hoặc dùng cách an toàn hơn)
            // Tạm thời logic xóa vẫn nằm trong updateItems()
        }
    }

    public void startGame(Bomberman myGame) {
        System.out.println("Starting game...");
        myGame.currentState = GameState.PLAYING;
    }

    public void toggleMusic() {
        isMusicOn = !isMusicOn;
        System.out.println("Music toggled: " + isMusicOn);
        // TODO: Thêm logic bật/tắt nhạc thực tế ở đây
    }


    public void restartGame(Bomberman myGame) {
        System.out.println("Restarting game...");
        myGame.currentLevel = 1;
        this.controllerActive(myGame);
        myGame.currentState = GameState.PLAYING;
    }


    public void chooseSetting(Bomberman myGame) {
        myGame.currentState = GameState.SETTING;
    }

    public void chooseAnimation0(Bomberman myGame) {
        isAnimations[0] = true;
        for (int i = 0; i < isAnimations.length; ++i) {
            if (i == 0) continue;
            isAnimations[i] = false;
        }
        SpriteSheet.tiles = new SpriteSheet("res/textures/classic1.png", 256);
        System.out.println("Set path successfully!");
        this.controllerActive(myGame);
    }

    public void chooseAnimation1(Bomberman myGame) {
        isAnimations[1] = true;
        for (int i = 0; i < isAnimations.length; ++i) {
            if (i == 1) continue;
            isAnimations[i] = false;
        }
        SpriteSheet.tiles = new SpriteSheet("res/textures/TEXTURE.png", 256);
        System.out.println("Set path successfully!");
        this.controllerActive(myGame);
    }

    public void chooseAnimation2(Bomberman myGame) {
        isAnimations[2] = true;
        for (int i = 0; i < isAnimations.length; ++i) {
            if (i == 2) continue;
            isAnimations[i] = false;
        }
        SpriteSheet.tiles = new SpriteSheet("res/textures/Screenshot 2025-05-08 124217.png", 256);
        System.out.println("Set path successfully!");
        this.controllerActive(myGame);
    }

    public boolean isPortalActivated() {
        return portalActivated;
    }

    public Map getMap() { return this.gameMap; }

    public MapData getMapData() { return this.mapData; }

    public void addFlame(Flame flame) {
        this.flames.add(flame);
    }

    public List<Flame> getFlames() {
        return this.flames;
    }

    public Player getPlayer() { return player; }

    public void setPlayer(Player player) { this.player = player; }

    public List<Enemy> getEnemies() {return this.enemies; }

    public java.util.Map<String, Character> getHiddenItemsData() {return this.hiddenItemsData; }

    public double getLevelTimeRemaining() { return this.levelTimeRemaining; }

    public List<Item> getItems() { return this.items; }

    public int getScore() { return this.score; }
    
}
