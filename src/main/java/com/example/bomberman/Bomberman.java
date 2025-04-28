package com.example.bomberman;

import com.example.bomberman.entities.*;
import com.example.bomberman.entities.Items.*;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
import com.example.bomberman.Map.*;

import com.example.bomberman.graphics.Sprite;
import com.example.bomberman.graphics.Animation; // Import lớp Animation

import com.example.bomberman.Input.InputHandler;
// TODO: Import các lớp Item khác khi bạn tạo chúng (FlameItem, SpeedItem, LifeItem)


import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Random;


public class Bomberman extends Application {

    private com.example.bomberman.Map.Map gameMap; // Đây là com.example.bomberman.Map.Map
    private Player player;
    private GraphicsContext gc;
    private Canvas canvas;

    // --- Danh sách quản lý các thực thể động và hiệu ứng tạm thời ---
    private final List<Bomb> bombs = new ArrayList<>();
    private final List<Object> enemies = new ArrayList<>(); // TODO: Thay Object bằng Enemy
    private final List<Item> items = new ArrayList<>(); // Danh sách quản lý các đối tượng Item
    private final List<Flame> flames = new ArrayList<>();
    private final List<TemporaryAnimation> temporaryAnimations = new ArrayList<>();


    // --- Lưu thông tin Item được giấu dưới gạch từ MapData ---
    // Đây là java.util.Map
    private java.util.Map<String, Character> hiddenItemsData;


    // --- Quản lý màn chơi ---
    private int currentLevel = 1;
    private final int MAX_LEVEL = 10;

    // TODO: UI và Game State
    // private int score = 0;
    // private int lives = 3;
    // private GameState gameState = GameState.PLAYING; // Enum GameState (PLAYING, PAUSED, GAME_OVER, LEVEL_CLEARED)

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

        // Kiểm tra canvas và player không null sau khi loadLevel
        if (canvas == null || player == null) {
            System.err.println("Fatal Error: Game initialization failed during level loading.");
            // TODO: Hiển thị thông báo lỗi cho người dùng và thoát game
            return;
        }

        Group root = new Group(canvas);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        // Tạo InputHandler sau khi Scene và Player đã có
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

                // TODO: Kiểm tra trạng thái game (ví dụ: nếu PAUSED thì không update)
                // if (gameState == GameState.PAUSED) {
                //    // Cập nhật UI tạm dừng nếu có
                //    renderUI(gc);
                //    return; // Không update thực thể
                // }


                // --- Vòng lặp Update ---
                // Cập nhật trạng thái của tất cả các thực thể

                // Cập nhật Player (chỉ nếu player không null)
                if (player != null) {
                    player.update(deltaTime);
                }
                // --- KIỂM TRA VA CHẠM GIỮA PLAYER VÀ BOM ĐỂ KÍCH HOẠT ĐÁ BOM ---
                if (player != null && player.kickableBombPending != null && player.kickDirectionPending != Direction.NONE) {
                    System.out.println("!!!!!! KICK TRIGGER POINT REACHED (Pending State)! Bomb: (" + player.kickableBombPending.getGridX() + "," + player.kickableBombPending.getGridY() + ") Dir: " + player.kickDirectionPending);

                    // Get the bomb and direction stored by checkCollision
                    Bomb bombToKick = player.kickableBombPending;
                    Direction directionToKick = player.kickDirectionPending;
                    double kickSpeed = player.getSpeed(); // Or fixed value

                    // Trigger the kick
                    bombToKick.startKicking(directionToKick, kickSpeed);

                    // IMPORTANT: Reset the pending state immediately after triggering
                    player.kickableBombPending = null;
                    player.kickDirectionPending = Direction.NONE;

                    // Optional: Add logic here to slightly push the player back
                    // or adjust their position if needed after initiating the kick,
                    // although checkCollision returning false should allow movement.
                }


                // Cập nhật Bom và xử lý Bom không còn hoạt động
                updateBombs(deltaTime);

                // TODO: Cập nhật các thực thể khác (enemies)
                // updateEnemies(deltaTime);

                // Cập nhật Flames
                updateFlames(deltaTime);

                // Cập nhật TemporaryAnimation (ví dụ: animation gạch vỡ)
                updateTemporaryAnimations(deltaTime);

                // --- Cập nhật Items ---
                updateItems(deltaTime);


                // TODO: Kiểm tra điều kiện chuyển màn (ví dụ: Player đến Portal và hết quái vật)
                // if (player != null && player.isOnPortal() && enemies.isEmpty()) { // Cần thêm phương thức isOnPortal() vào Player
                //    currentLevel++;
                //    if (currentLevel <= MAX_LEVEL) {
                //       loadLevel(currentLevel); // Tải màn mới
                //    } else {
                //       // Game Over - Win
                //       // gameState = GameState.GAME_OVER;
                //       // Hiển thị màn hình thắng cuộc
                //    }
                // }

                // TODO: Kiểm tra điều kiện thua cuộc (ví dụ: Player chết và hết mạng)
                // if (player != null && player.isDead() && lives <= 0) { // Cần thêm phương thức isDead() vào Player
                //    // gameState = GameState.GAME_OVER;
                //    // Hiển thị màn hình thua cuộc
                // }


                // --- Vòng lặp Render ---
                // Xóa toàn bộ Canvas trước khi vẽ lại (chỉ nếu gc không null)
                if (gc != null) {
                    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                } else {
                    System.err.println("Warning: GraphicsContext is null during render.");
                    return; // Không thể render nếu gc null
                }


                // Vẽ bản đồ nền (chỉ nếu gameMap không null)
                if (gameMap != null) {
                    gameMap.render(gc); // Gọi render của com.example.bomberman.Map.Map
                } else {
                    System.err.println("Warning: gameMap is null during render.");
                }


                // Vẽ các thực thể động (Player, Enemies, Bombs, Flames, Items) SAU khi vẽ map

                // Vẽ Bom
                renderBombs(gc);

                // --- Vẽ Item (thường vẽ dưới Player) ---
                renderItems(gc);


                // TODO: Vẽ các thực thể khác (Enemies)
                // renderEnemies(gc);

                // Vẽ Player (chỉ nếu player không null)
                if (player != null) {
                    player.render(gc);
                } else {
                    System.err.println("Warning: player is null during render.");
                }

                // Vẽ Flames (thường vẽ trên cùng)
                renderFlames(gc);


                // --- Vẽ các animation tạm thời (trên cùng) ---
                renderTemporaryAnimations(gc);


                // TODO: Vẽ các yếu tố UI (điểm số, thời gian, ...)
                // renderUI(gc);
            }
        };

        timer.start();
    }

    // --- Phương thức để tải một màn chơi cụ thể ---
    private void loadLevel(int levelNumber) {
        try {
            System.out.println("Loading level " + levelNumber + "..."); // Log bắt đầu tải level

            // Xóa các thực thể và animation từ màn chơi trước
            bombs.clear();
            flames.clear();
            enemies.clear();
            items.clear(); // Xóa items cũ
            temporaryAnimations.clear(); // Xóa animation cũ khi load màn mới
            player = null; // Đặt player về null trước khi tạo mới


            // --- 1. Tải MapData và Khởi tạo Map ---
            MapData mapData = new MapData(levelNumber);
            // Khởi tạo Map: Truyền tham chiếu 'this' (Bomberman)
            // Đây là nơi tạo đối tượng com.example.bomberman.Map.Map, nó sẽ gọi initializeTiles()
            // initializeTiles() sẽ tạo các Tile ban đầu (bao gồm Brick cho Item ẩn)
            gameMap = new com.example.bomberman.Map.Map(mapData, this); // Sử dụng tên đầy đủ package

            // --- 2. Lấy thông tin Item được giấu từ MapData ---
            // hiddenItemsData là java.util.Map
            hiddenItemsData = mapData.getHiddenItems();
            System.out.println("Loaded " + hiddenItemsData.size() + " hidden items from map data."); // Log


            // 3. Cập nhật kích thước canvas
            int canvasWidth = mapData.getCols() * Sprite.SCALED_SIZE;
            int canvasHeight = mapData.getRows() * Sprite.SCALED_SIZE;
            if (canvas == null) {
                canvas = new Canvas(canvasWidth, canvasHeight);
                gc = canvas.getGraphicsContext2D();
            } else {
                canvas.setWidth(canvasWidth);
                canvas.setHeight(canvasHeight);
            }

            // --- 4. Duyệt qua dữ liệu bản đồ và tạo thực thể động (Player, Enemies) ---
            // KHÔNG tạ Item ở đây. Item sẽ được tạo sau khi Brick vỡ.
            char[][] charMap = mapData.getMap();
            for (int i = 0; i < mapData.getRows(); i++) {
                for (int j = 0; j < mapData.getCols(); j++) {
                    char mapChar = charMap[i][j];

                    // Tạo các thực thể động dựa trên ký tự
                    switch (mapChar) {
                        case 'p':
                            // --- Tạo Player tại vị trí (j, i) ---
                            // Chỉ tạo Player MỘT LẦN khi gặp ký tự 'p' đầu tiên
                            if (player == null) {
                                System.out.println("Creating Player at grid (" + j + ", " + i + ")"); // Log
                                // Truyền gameMap (com.example.bomberman.Map.Map) và 'this' (Bomberman)
                                player = new Player(j, i, gameMap, this);
                                // TODO: Đảm bảo Player được thêm vào một danh sách thực thể chung nếu có
                                // --- Đặt lại ô Tile nền cho Player thành Grass (EMPTY) ---
                                // Player cần có thể di chuyển, nên ô dưới chân nó phải là Grass
                                if (gameMap != null) {
                                    gameMap.setTile(j, i, Tile.createTileFromChar(j, i, ' ')); // ' ' là Grass/EMPTY
                                    System.out.println("Tile at (" + j + ", " + i + ") changed to EMPTY for Player."); // Log
                                } else {
                                    System.err.println("Error: gameMap is null while setting Player tile to Grass.");
                                }
                            } else {
                                System.err.println("Warning: Found multiple 'p' characters in map data. Player already created.");
                            }
                            break;
                        case '1':
                            // TODO: Tạo Balloom Enemy tại vị trí (j, i) và thêm vào danh sách enemies
                            System.out.println("Found Balloom at grid (" + j + ", " + i + ")"); // Log
                            // Enemy balloom = new Balloom(j, i, gameMap); // Cần constructor Balloom
                            // enemies.add(balloom); // Thêm vào danh sách enemies
                            // --- Đặt lại ô Tile nền cho Enemy thành Grass (EMPTY) ---
                            // Enemy cần có thể di chuyển, nên ô dưới chân nó phải là Grass
                            if (gameMap != null) {
                                gameMap.setTile(j, i, Tile.createTileFromChar(j, i, ' ')); // ' ' là Grass/EMPTY
                                System.out.println("Tile at (" + j + ", " + i + ") changed to EMPTY for Enemy."); // Log
                            } else {
                                System.err.println("Error: gameMap is null while setting Enemy tile to Grass.");
                            }
                            break;
                        case '2':
                            // TODO: Tạo Oneal Enemy tại vị trí (j, i) và thêm vào danh sách enemies
                            System.out.println("Found Oneal at grid (" + j + ", " + i + ")"); // Log
                            // Enemy oneal = new Oneal(j, i, gameMap);
                            // enemies.add(oneal);
                            // --- Đặt lại ô Tile nền cho Enemy thành Grass (EMPTY) ---
                            if (gameMap != null) {
                                gameMap.setTile(j, i, Tile.createTileFromChar(j, i, ' ')); // ' ' là Grass/EMPTY
                                System.out.println("Tile at (" + j + ", " + i + ") changed to EMPTY for Enemy."); // Log
                            } else {
                                System.err.println("Error: gameMap is null while setting Enemy tile to Grass.");
                            }
                            break;
                        case '3':
                            // TODO: Tạo Doll Enemy tại vị trí (j, i) và thêm vào danh sách enemies
                            System.out.println("Found Doll at grid (" + j + ", " + i + ")"); // Log
                            // Enemy doll = new Doll(j, i, gameMap);
                            // enemies.add(doll);
                            // --- Đặt lại ô Tile nền cho Enemy thành Grass (EMPTY) ---
                            if (gameMap != null) {
                                gameMap.setTile(j, i, Tile.createTileFromChar(j, i, ' ')); // ' ' là Grass/EMPTY
                                System.out.println("Tile at (" + j + ", " + i + ") changed to EMPTY for Enemy."); // Log
                            } else {
                                System.err.println("Error: gameMap is null while setting Enemy tile to Grass.");
                            }
                            break;
                        case '4':
                            // TODO: Tạo Ghost Enemy tại vị trí (j, i) và thêm vào danh sách enemies
                            System.out.println("Found Ghost at grid (" + j + ", " + i + ")"); // Log
                            // Enemy ghost = new Ghost(j, i, gameMap);
                            // enemies.add(ghost);
                            // --- Đặt lại ô Tile nền cho Enemy thành Grass (EMPTY) ---
                            if (gameMap != null) {
                                gameMap.setTile(j, i, Tile.createTileFromChar(j, i, ' ')); // ' ' là Grass/EMPTY
                                System.out.println("Tile at (" + j + ", " + i + ") changed to EMPTY for Enemy."); // Log
                            } else {
                                System.err.println("Error: gameMap is null while setting Enemy tile to Grass.");
                            }
                            break;
                        case '5':
                            // TODO: Tạo Minvo Enemy tại vị trí (j, i) và thêm vào danh sách enemies
                            System.out.println("Found Minvo at grid (" + j + ", " + i + ")"); // Log
                            // Enemy minvo = new Minvo(j, i, gameMap);
                            // enemies.add(minvo);
                            // --- Đặt lại ô Tile nền cho Enemy thành Grass (EMPTY) ---
                            if (gameMap != null) {
                                gameMap.setTile(j, i, Tile.createTileFromChar(j, i, ' ')); // ' ' là Grass/EMPTY
                                System.out.println("Tile at (" + j + ", " + i + ") changed to EMPTY for Enemy."); // Log
                            } else {
                                System.err.println("Error: gameMap is null while setting Enemy tile to Grass.");
                            }
                            break;
                        case '6':
                            // TODO: Tạo Kondoria Enemy tại vị trí (j, i) và thêm vào danh sách enemies
                            System.out.println("Found Kondoria at grid (" + j + ", " + i + ")"); // Log
                            // Enemy kondoria = new Kondoria(j, i, gameMap);
                            // enemies.add(kondoria);
                            // --- Đặt lại ô Tile nền cho Enemy thành Grass (EMPTY) ---
                            if (gameMap != null) {
                                gameMap.setTile(j, i, Tile.createTileFromChar(j, i, ' ')); // ' ' là Grass/EMPTY
                                System.out.println("Tile at (" + j + ", " + i + ") changed to EMPTY for Enemy."); // Log
                            } else {
                                System.err.println("Error: gameMap is null while setting Enemy tile to Grass.");
                            }
                            break;
                        // --- Các ký tự Item ẩn ('b', 'f', 's', 'l','a') KHÔNG tạo Item ở đây ---
                        // Tile nền tại vị trí này đã được Map.initializeTiles() đặt là Brick
                        // Item sẽ được tạo SAU khi Brick vỡ.
                        case 'b':
                        case 'f':
                        case 's':
                        case 'l':
                        case 'a':
                            System.out.println("Found hidden Item ('" + mapChar + "') at grid (" + j + ", " + i + "). Initial tile is Brick. Item will spawn after destruction."); // Log
                            break;
                        case '#': // Wall
                        case '*': // Brick (gạch không chứa item)
                        case 'x': // Portal
                        case ' ': // Grass
                            // Các ký tự này chỉ đại diện cho Tile tĩnh hoặc Tile nền ban đầu
                            // Không tạo thực thể động ở đây
                            break;
                        default:
                            // Ký tự không xác định
                            System.err.println("Warning: Unknown character '" + mapChar + "' at grid (" + j + ", " + i + ")");
                            break;
                    }
                }
            }

            // --- 5. Đảm bảo Player được tạo thành công ---
            if (player == null) {
                System.err.println("Error: Player not created from map data! ('p' character not found?)");
                // TODO: Xử lý lỗi: có thể ném exception hoặc đặt cờ lỗi để game over
                throw new IllegalStateException("Player not found in map data.");
            }

            // --- 6. Cập nhật InputHandler để điều khiển Player mới sau khi load level ---
            // Nếu InputHandler là thuộc tính của Bomberman và đã được tạo lần đầu ở start(),
            // chúng ta cần cập nhật tham chiếu Player của nó.
            // Cần thêm phương thức setPlayer(Player player) vào lớp InputHandler.
            if (inputHandler != null && player != null) {
                // inputHandler.setPlayer(player); // Cần triển khai setPlayer trong InputHandler
                // Cách đơn giản nhất là tạo lại InputHandler mỗi lần loadLevel nếu Scene không thay đổi.
                if (primaryStage != null && primaryStage.getScene() != null) {
                    inputHandler = new InputHandler(primaryStage.getScene(), player);
                } else {
                    System.err.println("Error: Cannot create new InputHandler. primaryStage or Scene is null.");
                }
            } else if (inputHandler == null && player != null) {
                // Trường hợp loadLevel được gọi lần đầu tiên từ start()
                // InputHandler sẽ được tạo sau trong start()
                System.out.println("InputHandler will be created after loadLevel in start().");
            } else {
                System.err.println("Warning: InputHandler or Player is null after loadLevel. Input might not work.");
            }


            System.out.println("Level " + levelNumber + " loaded successfully."); // Log kết thúc tải level

        } catch (Exception e) {
            // Bắt bất kỳ ngoại lệ nào xảy ra trong quá trình tải level
            System.err.println("Error loading level " + levelNumber + ": " + e.getMessage());
            e.printStackTrace(); // In stack trace để xem chi tiết lỗi
            // TODO: Xử lý lỗi tải level (ví dụ: hiển thị thông báo lỗi cho người chơi, thoát game)
            // Đặt canvas và player về null để start() biết rằng có lỗi
            canvas = null;
            player = null;
            gameMap = null;
            hiddenItemsData = null; // Đảm bảo dữ liệu item cũng bị reset
        }
    }


    // --- Phương thức để thêm một quả bom mới vào danh sách ---
    // Phương thức này sẽ được gọi từ Player.placeBomb()
    public void addBomb(Bomb bomb) {
        bombs.add(bomb);
        System.out.println("Bomb added to list. Total bombs: " + bombs.size()); // Debug log
    }

    // TODO: Thêm phương thức kiểm tra xem có bom tại vị trí lưới cụ thể không (dùng cho Player.placeBomb)
    // public boolean isBombAtGrid(int gridX, int gridY) {
    //     for (Bomb bomb : bombs) {
    //         if (bomb.isActive() && bomb.getGridX() == gridX && bomb.getGridY() == gridY) {
    //             return true;
    //         }
    //     }
    //     return false;
    // }

    // --- Phương thức để thêm các đối tượng Flame vào danh sách ---
    // Phương thức này sẽ được gọi từ updateBombs khi bom nổ
    public void addFlames(List<Flame> newFlames) {
        flames.addAll(newFlames);
        System.out.println("Added " + newFlames.size() + " flames. Total flames: " + flames.size()); // Debug log
    }


    // --- Phương thức được gọi từ Map khi một ô gạch bị ngọn lửa chạm vào ---
    // Phương thức này sẽ được gọi từ Bomb.explode() khi ngọn lửa chạm gạch
    public void brickDestroyed(int gridX, int gridY) {
        System.out.println("Bomberman received notification: Brick destroyed at (" + gridX + ", " + gridY + ")"); // Log

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
        temporaryAnimations.add(brickAnim);

        // TODO: Phát âm thanh gạch vỡ
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


    // TODO: Thêm phương thức kiểm tra xem có bom tại vị trí lưới cụ thể không (dùng cho Player.placeBomb)
    // public boolean isBombAtGrid(int gridX, int gridY) { ... }


    // --- Phương thức cập nhật tất cả Bom ---
    private void updateBombs(double deltaTime) {
        // Sử dụng Iterator để có thể xóa phần tử trong khi duyệt danh sách
        Iterator<Bomb> iterator = bombs.iterator();
        while (iterator.hasNext()) {
            Bomb bomb = iterator.next();
            bomb.update(deltaTime); // Cập nhật trạng thái của bom

            // --- Xử lý bom đã nổ ---
            // Điều kiện này sẽ đúng khi bomb.explode() được gọi và đặt active = false
            if (!bomb.isActive()) {
                // Lấy danh sách Flames được tạo ra bởi vụ nổ này
                List<Flame> newFlames = bomb.getGeneratedFlames(); // Cần phương thức getGeneratedFlames() trong Bomb
                if (newFlames != null && !newFlames.isEmpty()) {
                    addFlames(newFlames);
                    // TODO: Cần cơ chế trong Bomb để chỉ tạo Flames 1 lần
                }


                iterator.remove();
                System.out.println("Bomb removed from list. Total bombs: " + bombs.size()); // Debug log
                // --- Thông báo cho Player biết bom đã nổ để tăng lại số bom có thể đặt ---
                // Sửa lỗi: Đảm bảo bomb.getOwner() không null trước khi gọi increaseBombCount
                if (bomb.getOwner() != null) {
                    bomb.getOwner().increaseBombCount(); // Gọi phương thức mới trong Player
                }
            }
            // TODO: Xử lý khi bom nổ nhưng animation nổ của bom gốc chưa kết thúc (chưa isActive = false)
            // if (bomb.isExploded() && bomb.isActive()) {
            //    // Cập nhật animation nổ và kiểm tra khi nào kết thúc để đặt isActive = false
            // }
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
            flame.update(deltaTime); // Cập nhật trạng thái của ngọn lửa

            // Nếu ngọn lửa không còn active, xóa khỏi danh sách
            if (!flame.isActive()) {
                iterator.remove();
                // System.out.println("Flame removed from list. Total flames: " + flames.size()); // Log quá nhiều, có thể tắt
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
    // Trong lớp Bomberman.java

    // --- Phương thức cập nhật tất cả TemporaryAnimation (ví dụ: animation gạch vỡ) ---
    private void updateTemporaryAnimations(double deltaTime) {
        Iterator<TemporaryAnimation> iterator = temporaryAnimations.iterator();
        while (iterator.hasNext()) {
            TemporaryAnimation anim = iterator.next();
            anim.update(deltaTime); // Cập nhật trạng thái animation

            // Nếu animation đã kết thúc (ví dụ: animation gạch vỡ đã chạy hết)
            if (!anim.isActive()) {
                System.out.println("Bomberman detected TemporaryAnimation finished at (" + anim.getGridX() + ", " + anim.getGridY() + ")."); // Log khi phát hiện kết thúc

                int gridX = anim.getGridX();
                int gridY = anim.getGridY();

                // --- 1. Yêu cầu Map thay đổi Tile tại vị trí này thành Grass (EMPTY) ---
                if (gameMap != null) {
                    gameMap.setTile(gridX, gridY, Tile.createTileFromChar(gridX, gridY, ' ')); // ' ' là Grass/EMPTY
                    System.out.println("Tile at (" + gridX + ", " + gridY + ") changed to EMPTY."); // Log khi thay đổi Tile
                } else {
                    System.err.println("Error: gameMap is null when finishing brick animation!");
                }

                // --- 2. Xác định xem có Item nào nên được tạo ra tại vị trí này không ---
                char itemToSpawnChar = '\0'; // Sử dụng ký tự '\0' để đánh dấu rằng chưa có Item nào được xác định cần tạo
                String itemKey = gridX + "," + gridY;


                if (hiddenItemsData != null && hiddenItemsData.containsKey(itemKey)) {
                    itemToSpawnChar = hiddenItemsData.get(itemKey);
                    // Xóa Item đã được tạo khỏi danh sách hiddenItemsData để không tạo lại lần nữa
                    hiddenItemsData.remove(itemKey);
                    System.out.println("Hidden item '" + itemToSpawnChar + "' found at (" + gridX + ", " + gridY + ") after brick destruction animation finished."); // Log

                } else {
                    double randomDropProbability = 0.1;

                    // Sử dụng đối tượng Random đã được khởi tạo trong Bomberman
                    if (random.nextDouble() < randomDropProbability) {
                        char[] possibleRandomItems = {'b', 'f', 's', 'l' ,'a'}; // THÊM hoặc BỚT các ký tự Item nếu bạn có loại khác

                        // Chọn ngẫu nhiên một chỉ mục (index) trong mảng các ký tự Item
                        int randomIndex = random.nextInt(possibleRandomItems.length);
                        // Lấy ký tự Item tương ứng với chỉ mục ngẫu nhiên
                        itemToSpawnChar = possibleRandomItems[randomIndex];
                        System.out.println("Random item '" + itemToSpawnChar + "' will spawn at (" + gridX + ", " + gridY + ")."); // Log

                    } else {
                        // Không có hidden item và tỷ lệ ngẫu nhiên cũng thất bại
                        System.out.println("No item spawned at (" + gridX + ", " + gridY + ") after brick destruction animation finished (no hidden item and random chance failed)."); // Log
                    }
                }


                // --- 3. Tạo đối tượng Item nếu đã xác định được ký tự Item cần tạo (itemToSpawnChar khác '\0') ---
                if (itemToSpawnChar != '\0') { // '\0' là ký tự null, kiểm tra xem itemToSpawnChar đã được gán chưa
                    // Gọi phương thức spawnItemAt đã có sẵn để tạo Item cụ thể và thêm nó vào danh sách `items` của Bomberman
                    spawnItemAt(gridX, gridY, itemToSpawnChar);
                }
                iterator.remove(); // Xóa an toàn TemporaryAnimation đã kết thúc
                // System.out.println("Temporary animation removed from list at (" + gridX + ", " + gridY + ")."); // Log (có thể quá nhiều)
            }
        }
    }
    public List<Bomb> getBombs() {
        return bombs; // Trả về tham chiếu đến danh sách bombs
    }

    // --- Phương thức vẽ tất cả TemporaryAnimation ---
    private void renderTemporaryAnimations(GraphicsContext gc) {
        for (TemporaryAnimation anim : temporaryAnimations) {
            anim.render(gc);
        }
    }


    // --- Phương thức cập nhật tất cả Items ---
    // Trong lớp Bomberman.java

    // --- Phương thức cập nhật tất cả Items ---
    private void updateItems(double deltaTime) {
        // Sử dụng Iterator để có thể xóa phần tử trong khi duyệt danh sách một cách an toàn
        Iterator<com.example.bomberman.entities.Items.Item> iterator = items.iterator();

        while (iterator.hasNext()) {
            com.example.bomberman.entities.Items.Item item = iterator.next();

            // 1. Cập nhật trạng thái của Item (bao gồm animation và logic hết hạn nếu có)
            item.update(deltaTime);

            // 2. Kiểm tra va chạm Player với Item chỉ nếu cả Player và Item còn active
            // Chúng ta chỉ kiểm tra va chạm nếu Player còn sống
            if (player != null && player.isAlive() && item.isActive()) {
                // Gọi phương thức collidesWith() đã thêm vào lớp Player
                if (player.collidesWith(item)) {
                    // Va chạm xảy ra! Player đã nhặt Item.

                    System.out.println("Player collided with Item at (" + item.getGridX() + ", " + item.getGridY() + ")"); // Log

                    // Áp dụng hiệu ứng của Item lên Player
                    item.applyEffect(player); // Item sẽ gọi phương thức phù hợp trong Player

                    // Đánh dấu Item là không còn active sau khi bị nhặt
                    // Điều này sẽ khiến Item bị xóa ở bước tiếp theo
                    item.setActive(false);

                    // TODO: Phát âm thanh nhặt item (gọi ở đây hoặc trong applyEffect của Item)

                    // TODO: Nếu Player chỉ có thể nhặt 1 Item mỗi frame để tránh lỗi, có thể break; ở đây
                    // break; // Thoát vòng lặp sau khi nhặt 1 item
                }
            }

            // 3. Xóa Item khỏi danh sách nếu nó không còn active (do bị nhặt HOẶC hết hạn tự nhiên nếu có)
            if (!item.isActive()) {
                iterator.remove(); // Xóa an toàn khỏi danh sách `items`
                // System.out.println("Item removed from list. Total items remaining: " + items.size()); // Log (có thể quá nhiều)
            }
        }
    }

    // --- Phương thức vẽ tất cả Items ---
    private void renderItems(GraphicsContext gc) {
        for (Item item : items) {
            item.render(gc);
        }
    }



    public static void main(String[] args) {
        launch(args);
    }
}
