package com.example.bomberman;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
import com.example.bomberman.Map.*;
import com.example.bomberman.graphics.Sprite;
import com.example.bomberman.graphics.Animation; // Import Animation
import com.example.bomberman.entities.Player;
import com.example.bomberman.Input.InputHandler;
import com.example.bomberman.entities.Direction;
import com.example.bomberman.entities.Bomb;
import com.example.bomberman.entities.Flame;
import com.example.bomberman.entities.TemporaryAnimation; // Import TemporaryAnimation

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Random;


public class Bomberman extends Application {

    private Map gameMap;
    private Player player;
    private GraphicsContext gc;
    private Canvas canvas;

    // --- Danh sách quản lý các thực thể động và hiệu ứng tạm thời ---
    private final List<Bomb> bombs = new ArrayList<>();
    private final List<Flame> flames = new ArrayList<>();
    private final List<Object> enemies = new ArrayList<>(); // TODO: Thay Object bằng Enemy
    private final List<Object> items = new ArrayList<>(); // TODO: Thay Object bằng Item
    // --- Thêm danh sách quản lý các animation tạm thời ---
    private final List<TemporaryAnimation> temporaryAnimations = new ArrayList<>();


    // --- Quản lý màn chơi ---
    private int currentLevel = 1;
    private final int MAX_LEVEL = 10;

    // TODO: UI và Game State
    // private int score = 0;
    // private int lives = 3;
    // private GameState gameState = GameState.PLAYING;

    private InputHandler inputHandler;

    // TODO: Stage reference
    private Stage primaryStage;

    private Random random = new Random();


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        primaryStage.setTitle("Bomberman Game");
        primaryStage.setResizable(false);

        loadLevel(currentLevel);

        if (canvas == null) {
            System.err.println("Fatal Error: Canvas was not created during level loading. Game cannot start.");
            return;
        }
        Group root = new Group(canvas);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        if (player == null) {
            System.err.println("Fatal Error: Player was not created during level loading. Cannot create InputHandler.");
            return;
        }
        inputHandler = new InputHandler(scene, player);


        primaryStage.show();

        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdateTime = 0;

            @Override
            public void handle(long now) {
                if (lastUpdateTime == 0) {
                    lastUpdateTime = now;
                    return;
                }

                double deltaTime = (now - lastUpdateTime) / 1_000_000_000.0;
                lastUpdateTime = now;

                // --- Vòng lặp Update ---
                if (player != null) {
                    player.update(deltaTime);
                }

                updateBombs(deltaTime);
                updateFlames(deltaTime);
                // --- Cập nhật các animation tạm thời ---
                updateTemporaryAnimations(deltaTime);


                // TODO: Update Enemies and Items
                // updateEnemies(deltaTime);
                // updateItems(deltaTime);


                // --- Vòng lặp Render ---
                if (gc != null) {
                    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                } else {
                    System.err.println("Warning: GraphicsContext is null during render.");
                    return;
                }

                if (gameMap != null) {
                    gameMap.render(gc);
                } else {
                    System.err.println("Warning: gameMap is null during render.");
                }


                renderBombs(gc);

                // TODO: Render Items
                // renderItems(gc);

                if (player != null) {
                    player.render(gc);
                } else {
                    System.err.println("Warning: player is null during render.");
                }

                // TODO: Render Enemies
                // renderEnemies(gc);

                renderFlames(gc);

                // --- Vẽ các animation tạm thời (trên cùng) ---
                renderTemporaryAnimations(gc);


                // TODO: Render UI
            }
        };

        timer.start();
    }

    // --- Phương thức để tải một màn chơi cụ thể ---
    private void loadLevel(int levelNumber) {
        try {
            bombs.clear();
            flames.clear();
            enemies.clear();
            items.clear();
            temporaryAnimations.clear(); // Xóa animation cũ khi load màn mới

            MapData mapData = new MapData(levelNumber);
            gameMap = new Map(mapData, this); // Truyền tham chiếu 'this'

            int canvasWidth = mapData.getCols() * Sprite.SCALED_SIZE;
            int canvasHeight = mapData.getRows() * Sprite.SCALED_SIZE;
            if (canvas == null) {
                canvas = new Canvas(canvasWidth, canvasHeight);
                gc = canvas.getGraphicsContext2D();
            } else {
                canvas.setWidth(canvasWidth);
                canvas.setHeight(canvasHeight);
            }

            char[][] charMap = mapData.getMap();
            for (int i = 0; i < mapData.getRows(); i++) {
                for (int j = 0; j < mapData.getCols(); j++) {
                    char mapChar = charMap[i][j];

                    switch (mapChar) {
                        case 'p':
                            player = new Player(j, i, gameMap, this);
                            break;
                        // TODO: Thêm các case để tạo Enemy và Item từ ký tự bản đồ
                        case '1': // Balloom
                            // enemies.add(new Balloom(j, i, gameMap));
                            break;
                        case '2': // Oneal
                            // enemies.add(new Oneal(j, i, gameMap));
                            break;
                        case 'b': // Bomb Item
                            // items.add(new BombItem(j, i));
                            break;
                        case 'f': // Flame Item
                            // items.add(new FlameItem(j, i));
                            break;
                        case 's': // Speed Item
                            // items.add(new SpeedItem(j, i));
                            break;
                        case 'l': // Life Item
                            // items.add(new LifeItem(j, i));
                            break;
                        case '#': // Wall
                        case '*': // Brick
                        case 'x': // Portal
                        case ' ': // Grass
                            // Các Tile tĩnh được Map xử lý
                            break;
                        default:
                            System.err.println("Warning: Unknown character '" + mapChar + "' at grid (" + j + ", " + i + ")");
                            break;
                    }

                    // --- Đặt lại ô Tile tại vị trí thực thể động thành Grass ---
                    // Chỉ làm cho các ký tự đại diện cho THỰC THỂ ĐỘNG
                    if (mapChar != '#' && mapChar != '*' && mapChar != 'x' && mapChar != ' ') {
                        // Không gọi setTile ở đây nữa, vì Map.initializeTiles đã tạo Tile nền
                        // Logic tạo thực thể động và đặt lại Tile nền sẽ được xử lý sau này
                        // nếu bạn muốn làm lại phần loadLevel chi tiết hơn.
                        // Hiện tại, Player được tạo, và ô 'p' ban đầu sẽ vẫn là Grass do Map.initializeTiles
                    }
                }
            }

            if (player == null) {
                System.err.println("Error: Player not created from map data!");
            }

            // Cập nhật InputHandler
            if (inputHandler != null) {
                // inputHandler.setPlayer(player);
            }


        } catch (Exception e) {
            System.err.println("Error loading level " + levelNumber + ": " + e.getMessage());
            e.printStackTrace();
            canvas = null;
            player = null;
            gameMap = null;
        }
    }


    // --- Phương thức để thêm một quả bom mới vào danh sách ---
    public void addBomb(Bomb bomb) {
        bombs.add(bomb);
        System.out.println("Bomb added to list. Total bombs: " + bombs.size());
    }

    // --- Phương thức để thêm các đối tượng Flame vào danh sách ---
    public void addFlames(List<Flame> newFlames) {
        flames.addAll(newFlames);
        System.out.println("Added " + newFlames.size() + " flames. Total flames: " + flames.size());
    }

    // --- Phương thức được gọi từ Map khi một ô gạch bị ngọn lửa chạm vào ---
    public void brickDestroyed(int gridX, int gridY) {
        System.out.println("Bomberman received notification: Brick destroyed at (" + gridX + ", " + gridY + ")"); // Log

        // --- Tạo animation gạch vỡ tại vị trí này ---
        // Sử dụng các sprite brick_exploded, brick_exploded1, brick_exploded2
        // Thời gian animation có thể lấy từ Flame hoặc đặt cố định
        double animationDuration = 0.5; // Ví dụ: animation kéo dài 0.5 giây
        double frameDuration = animationDuration / 3; // Chia đều cho 3 frame

        Animation brickExplosionAnim = new Animation(frameDuration, false,
                Sprite.brick_exploded, Sprite.brick_exploded1, Sprite.brick_exploded2);

        TemporaryAnimation brickAnim = new TemporaryAnimation(gridX, gridY, brickExplosionAnim);

        // Thêm animation vào danh sách quản lý
        temporaryAnimations.add(brickAnim);

        // TODO: Phát âm thanh gạch vỡ
    }


    // --- Phương thức để tạo Item tại một vị trí lưới (được gọi sau khi animation gạch vỡ kết thúc) ---
    public void spawnItemAt(int gridX, int gridY) {
        // TODO: Triển khai logic tạo item ngẫu nhiên hoặc theo cấu hình map
        // Tạm thời tạo ngẫu nhiên một loại item để kiểm tra
        if (random.nextDouble() < 0.3) { // 30% cơ hội tạo item
            int itemType = random.nextInt(4); // 0: Bomb, 1: Flame, 2: Speed, 3: Life
            switch (itemType) {
                case 0:
                    // items.add(new BombItem(gridX, gridY)); // Cần lớp BombItem
                    System.out.println("Spawned Bomb Item at (" + gridX + ", " + gridY + ")");
                    break;
                case 1:
                    // items.add(new FlameItem(gridX, gridY)); // Cần lớp FlameItem
                    System.out.println("Spawned Flame Item at (" + gridX + ", " + gridY + ")");
                    break;
                case 2:
                    // items.add(new SpeedItem(gridX, gridY)); // Cần lớp SpeedItem
                    System.out.println("Spawned Speed Item at (" + gridX + ", " + gridY + ")");
                    break;
                case 3:
                    // items.add(new LifeItem(gridX, gridY)); // Cần lớp LifeItem
                    System.out.println("Spawned Life Item at (" + gridX + ", " + gridY + ")");
                    break;
            }
        }
    }


    // TODO: Thêm phương thức kiểm tra xem có bom tại vị trí lưới cụ thể không (dùng cho Player.placeBomb)
    // public boolean isBombAtGrid(int gridX, int gridY) { ... }


    // --- Phương thức cập nhật tất cả Bom ---
    private void updateBombs(double deltaTime) {
        Iterator<Bomb> iterator = bombs.iterator();
        while (iterator.hasNext()) {
            Bomb bomb = iterator.next();
            bomb.update(deltaTime);

            // --- Xử lý bom đã nổ ---
            // Điều kiện này sẽ đúng khi bomb.explode() được gọi và đặt active = false
            if (!bomb.isActive()) {
                // Lấy danh sách Flames được tạo ra bởi vụ nổ này
                List<Flame> newFlames = bomb.getGeneratedFlames();
                // Thêm các Flames này vào danh sách Flames chính của Bomberman
                addFlames(newFlames);

                // Xóa bom khỏi danh sách
                iterator.remove();
                System.out.println("Bomb removed from list. Total bombs: " + bombs.size());

                // Tăng lại số bom mà Player đã đặt có thể sử dụng
                if (bomb.getOwner() != null) {
                    bomb.getOwner().increaseBombCount();
                }
            }
            // TODO: Xử lý khi bom nổ nhưng animation nổ của bom gốc chưa kết thúc
        }
    }

    // --- Phương thức vẽ tất cả Bom ---
    private void renderBombs(GraphicsContext gc) {
        for (Bomb bomb : bombs) {
            bomb.render(gc);
        }
    }

    // --- Phương thức cập nhật tất cả Flames ---
    private void updateFlames(double deltaTime) {
        Iterator<Flame> iterator = flames.iterator();
        while (iterator.hasNext()) {
            Flame flame = iterator.next();
            flame.update(deltaTime);

            // Nếu ngọn lửa không còn active, xóa khỏi danh sách
            if (!flame.isActive()) {
                iterator.remove();
            }
        }
    }

    // --- Phương thức vẽ tất cả Flames ---
    private void renderFlames(GraphicsContext gc) {
        for (Flame flame : flames) {
            flame.render(gc);
        }
    }

    // --- Phương thức cập nhật tất cả TemporaryAnimation ---
    private void updateTemporaryAnimations(double deltaTime) {
        Iterator<TemporaryAnimation> iterator = temporaryAnimations.iterator();
        while (iterator.hasNext()) {
            TemporaryAnimation anim = iterator.next();
            anim.update(deltaTime); // Cập nhật trạng thái animation

            // Nếu animation đã kết thúc
            if (!anim.isActive()) {
                // --- Xử lý sau khi animation gạch vỡ kết thúc ---
                // Đây là nơi chúng ta thay đổi Tile vĩnh viễn và tạo Item
                // TODO: Cần kiểm tra loại TemporaryAnimation nếu có nhiều loại khác nhau
                // Hiện tại chỉ có animation gạch vỡ, nên ta xử lý luôn

                int gridX = anim.getGridX();
                int gridY = anim.getGridY();

                // Yêu cầu Map thay đổi Tile tại vị trí này thành Grass (EMPTY)
                if (gameMap != null) {
                    gameMap.setTile(gridX, gridY, Tile.createTileFromChar(gridX, gridY, ' ')); // ' ' là Grass/EMPTY
                } else {
                    System.err.println("Error: gameMap is null when finishing brick animation!");
                }


                // Gọi logic tạo Item tại vị trí này
                spawnItemAt(gridX, gridY);


                // Xóa animation khỏi danh sách
                iterator.remove();
                System.out.println("Temporary animation finished and removed at (" + gridX + ", " + gridY + ")"); // Log
            }
        }
    }

    // --- Phương thức vẽ tất cả TemporaryAnimation ---
    private void renderTemporaryAnimations(GraphicsContext gc) {
        for (TemporaryAnimation anim : temporaryAnimations) {
            anim.render(gc);
        }
    }


    // TODO: Thêm các phương thức update/render tương tự cho Enemies, Items


    public static void main(String[] args) {
        launch(args);
    }
}
