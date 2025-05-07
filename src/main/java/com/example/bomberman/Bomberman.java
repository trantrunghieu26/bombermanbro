package com.example.bomberman;

// ---- GIỮ LẠI CÁC IMPORT CẦN THIẾT ----
import com.example.bomberman.Map.MapData;
import com.example.bomberman.Map.Tile; // Giữ lại nếu Player.checkCollision cần
import com.example.bomberman.controller.*; // Import package controller
import com.example.bomberman.entities.*;
import com.example.bomberman.entities.Items.*;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Particle;
import com.example.bomberman.graphics.Sprite;
import com.example.bomberman.Input.InputHandler;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode; // Thêm KeyCode
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javafx.scene.media.*;


public class Bomberman extends Application {

    // =========================================================================
    // Constants and Static Fields (Giữ nguyên)
    // =========================================================================
    public static final int UI_PANEL_HEIGHT = 32;
    private static final double LEVEL_DURATION_SECONDS = 200.0; // Giá trị gốc
    private final int MAX_LEVEL = 10;
    // Các hằng số animation có thể giữ ở đây hoặc chuyển vào Controller tương ứng
    // private final double SCORE_ANIMATION_DURATION = 1.5;
    // private final double GAMEOVER_TEXT_FADE_IN_DURATION = 0.8;
    // private final double TRANSITION_DURATION = 1.0;

    // =========================================================================
    // Core Components & Game State
    // =========================================================================
    private Stage primaryStage;
    private Canvas canvas;
    private GraphicsContext gc;
    private InputHandler inputHandler;
    private Random random = new Random();
    private GameState currentState = GameState.MENU; // Enum vẫn dùng để quản lý state
    private SceneController currentController;     // Controller hiện tại

    // =========================================================================
    // Game Data (Giữ lại hoặc chuyển vào GameContext)
    // =========================================================================
    private com.example.bomberman.Map.Map gameMap;
    private Player player;
    private final List<Bomb> bombs = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>(); // TODO: Enemy
    private final List<Item> items = new ArrayList<>();
    private final List<Flame> flames = new ArrayList<>();
    private final List<TemporaryAnimation> temporaryAnimations = new ArrayList<>();
    private List<Particle> particles = new ArrayList<>();
    private java.util.Map<String, Character> hiddenItemsData;
    private int currentLevel = 1;
    private int score = 0;
    // private int lives = 3; // Lives được quản lý bởi Player
    private double levelTimeRemaining;
    private boolean portalActivated = false;
    private int portalGridX = -1;
    private int portalGridY = -1;

    // =========================================================================
    // UI & Rendering Resources (Giữ lại)
    // =========================================================================
    private Font uiFont;
    private Font gameOverFont;
    private Image menuBackground;
    private Image handCursorImage;
    private Image lastScreenSnapshot = null;
    private Animation playerDeadAnimation;
    private boolean isMusicOn = true; // Trạng thái nhạc
    // =========================================================================
    // Sound
    // =========================================================================
    private MediaPlayer backgroundMusicPlayer;

    // AudioClips for sound effects (đặt tên theo chức năng hoặc file của bạn)
    private AudioClip menuMoveSound;
    private AudioClip menuSelectSound;
    private AudioClip bombPlacedSound; // Âm thanh đặt bom (giả định tên file)
    private AudioClip explosionSound; // Âm thanh nổ bom
    private AudioClip playerDeadSound; // Âm thanh Player chết
    private AudioClip itemPickupSound; // Âm thanh nhặt Item
    private AudioClip  playerWalkSound; // Âm thanh bước chân (sẽ xử lý phức tạp hơn)
    private AudioClip gameOverSound;// am thanh game over
    private boolean isPlayerWalkSoundPlaying = false;

    // =========================================================================
    // Application Lifecycle & Initialization
    // =========================================================================
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Bomberman Game");
        primaryStage.setResizable(false);

        loadResources(); // Load fonts, images
        canvas = new Canvas(1, 1);
        gc = canvas.getGraphicsContext2D();

        Group root = new Group(canvas);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        boolean initialLoadSuccess = loadLevel(currentLevel);
        if (!initialLoadSuccess) {
            System.err.println("FATAL: Initial level load failed. Cannot start game.");
            // Có thể hiển thị Alert lỗi ở đây
            return; // Không thể tiếp tục nếu level đầu lỗi
        }
        inputHandler = new InputHandler(scene, player, this);
        primaryStage.show();
        // --- Khởi tạo trạng thái ban đầu ---
        switchController(GameState.MENU); // Bắt đầu ở Menu
        playBackgroundMusic();
        startGameLoop(); // Bắt đầu vòng lặp game
    }

    public static void main(String[] args) {
        launch(args);
    }

    // =========================================================================
    // Resource Loading (Giữ nguyên)
    // =========================================================================
    private void loadResources() {
        // ... (Code load font, images giữ nguyên như gốc) ...
        // Load Font
        try (InputStream fontStream = getClass().getResourceAsStream("/Font/PressStart2P-Regular.ttf")) {
            if (fontStream != null) {
                this.uiFont = Font.loadFont(fontStream, 16);
                if (this.uiFont == null) {
                    System.err.println("Font.loadFont returned null. Using default Arial.");
                    this.uiFont = Font.font("Arial", 16);
                } else {
                    System.out.println("Custom font loaded: " + this.uiFont.getName());
                    this.gameOverFont = Font.font(this.uiFont.getFamily(), 72);
                    System.out.println("Game Over font created: " + this.gameOverFont.getName() + " size " + this.gameOverFont.getSize());
                }
            } else {
                System.err.println("Could not find font resource stream. Using default Arial.");
                this.uiFont = Font.font("Arial", 16);
            }
        } catch (Exception e) {
            System.err.println("Error loading custom font: " + e.getMessage());
            e.printStackTrace();
            this.uiFont = Font.font("Arial", 16);
            this.gameOverFont = Font.font("Arial", 72);
        }
        if (this.gameOverFont == null) {
            this.gameOverFont = Font.font("Arial", 72);
        }

        // Load Menu Background
        try (InputStream bgStream = getClass().getResourceAsStream("/textures/nền.png")) {
            if (bgStream != null) {
                menuBackground = new Image(bgStream);
                System.out.println("Menu background loaded.");
            } else {
                System.err.println("Could not find menu background resource stream.");
            }
        } catch (Exception e) {
            System.err.println("Error loading menu background: " + e.getMessage());
            e.printStackTrace();
        }

        // Load Hand Cursor
        try (InputStream hcStream = getClass().getResourceAsStream("/textures/contro2.png")) {
            if (hcStream != null) {
                handCursorImage = new Image(hcStream);
                System.out.println("Hand cursor loaded.");
            } else {
                System.err.println("Could not find hand cursor resource stream.");
            }
        } catch (IOException e) {
            System.err.println("Error loading hand cursor: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            // Background Music
            // Đường dẫn tài nguyên phải bắt đầu bằng '/' và là đường dẫn trong thư mục res/Sound
            java.net.URL bgMusicUrl = getClass().getResource("/Sound/BackGroundMusic.mp3");
            if (bgMusicUrl != null) {
                javafx.scene.media.Media backgroundMusic = new javafx.scene.media.Media(bgMusicUrl.toExternalForm());
                backgroundMusicPlayer = new javafx.scene.media.MediaPlayer(backgroundMusic);
                backgroundMusicPlayer.setCycleCount(javafx.scene.media.MediaPlayer.INDEFINITE); // Lặp vô hạn
                backgroundMusicPlayer.setVolume(0.3); // Điều chỉnh âm lượng (0.0 đến 1.0)
                System.out.println("Background music loaded.");
            } else {
                System.err.println("Could not find background music resource: /Sound/BackGroundMusic.mp3");
            }

            // Sound Effects (AudioClip)
            // Sử dụng một phương thức helper để tải AudioClip an toàn hơn
            menuMoveSound = loadAudioClip("/Sound/DichuyencontroMenu.mp3", "Menu Move");
            menuSelectSound = loadAudioClip("/Sound/SelectLuaChonMenu.mp3", "Menu Select");
            // Bạn cần kiểm tra tên file âm thanh thực tế của mình và cập nhật đường dẫn/tên biến ở đây
            // Giả định các tên file sau tồn tại hoặc bạn có thể thay đổi chúng:
            bombPlacedSound = loadAudioClip("/Sound/BombPlaced.mp3", "Bomb Placed"); // Giả định tên file
            playerDeadSound = loadAudioClip("/Sound/PlayerDead.mp3", "Player Dead"); // Sử dụng tên file của bạn
            itemPickupSound = loadAudioClip("/Sound/PickUpItem.mp3", "Item Pickup"); // Sử dụng tên file của bạn
            playerWalkSound = loadAudioClip("/Sound/amthanhdibo.mp3", "Player Walk"); // Sử dụng tên file của bạn
            gameOverSound = loadAudioClip("/Sound/GameOver.mp3", "Game Over"); // Sử dụng tên file của bạn
            explosionSound = loadAudioClip("/Sound/amthanhbomno.mp3", "Bomb Explosion");
            bombPlacedSound = loadAudioClip("/Sound/placebomb.mp3", "Bomb Placed");
            // TODO: Thêm các AudioClip khác tương ứng với file bạn có: EnemyDead, BrickBreak, LevelClear,...

        } catch (Exception e) {
            System.err.println("Error loading sound resources: " + e.getMessage());
            e.printStackTrace();
            // Tiếp tục chạy game mà không có âm thanh
        }

        // Load Player Dead Animation Sprites
        this.playerDeadAnimation = new Animation(1.0/3.0, false, Sprite.player_dead1, Sprite.player_dead2, Sprite.player_dead3); // Thời gian frame hợp lý hơn
    }
    private AudioClip loadAudioClip(String resourcePath, String debugName) {
        try {
            java.net.URL resourceUrl = getClass().getResource(resourcePath);
            if (resourceUrl != null) {
                javafx.scene.media.AudioClip clip = new javafx.scene.media.AudioClip(resourceUrl.toExternalForm());
                System.out.println(debugName + " sound loaded.");
                return clip;
            } else {
                System.err.println("Could not find " + debugName + " sound resource: " + resourcePath);
                return null; // Trả về null nếu không tìm thấy
            }
        } catch (Exception e) {
            System.err.println("Error loading " + debugName + " sound: " + e.getMessage());
            e.printStackTrace();
            return null; // Trả về null nếu có lỗi khác
        }
    }
    public void playBackgroundMusic() {
        if (backgroundMusicPlayer != null && isMusicOn) {
            // Đảm bảo dừng nhạc nền cũ trước khi phát lại nếu cần
            // backgroundMusicPlayer.stop(); // Tùy chọn, nếu muốn nhạc luôn bắt đầu từ đầu
            backgroundMusicPlayer.play();
            System.out.println("Playing background music.");
        }
    }

    /**
     * Dừng phát nhạc nền.
     */
    public void stopBackgroundMusic() {
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.stop();
            System.out.println("Stopping background music.");
        }
    }
    /**
     * Điều chỉnh trạng thái bật/tắt âm thanh toàn cục.
     */
    public void setMusicOn(boolean on) {
        this.isMusicOn = on;
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.setMute(!on); // Bật/tắt tiếng MediaPlayer
            // MediaPlayer có thể tạm dừng/phát tiếp, hoặc dừng hẳn. Mute đơn giản hơn.
            // Nếu bạn dùng stop/play, cần logic phức tạp hơn khi chuyển state/tạm dừng game.
        }

        // Quản lý âm thanh bước chân (nếu đang phát)
        // Nếu nhạc bị tắt, dừng âm thanh bước chân ngay lập tức
        if (!on && isPlayerWalkSoundPlaying) {
            stopPlayerWalkSound();
        }
        // Nếu nhạc được bật VÀ Player đang di chuyển VÀ âm thanh bước chân chưa phát, bắt đầu phát lại
        // Logic này sẽ được kích hoạt lại khi Player bắt đầu di chuyển hoặc khi Player đang di chuyển
        // và bạn bật nhạc lại từ menu.
        if (on && player != null && player.isMoving() && !isPlayerWalkSoundPlaying) {
            startPlayerWalkSound(); // Cần phương thức này bên dưới
        }

        System.out.println("Music set to: " + isMusicOn);
    }
    private void playSound(javafx.scene.media.AudioClip clip) {
        if (clip != null && isMusicOn) {
            clip.play();
        }
    }

    // Các phương thức cụ thể để phát từng loại âm thanh hiệu ứng
    public void playMenuMoveSound() { playSound(menuMoveSound); }
    public void playMenuSelectSound() { playSound(menuSelectSound); }
    public void playExplosionSound() { playSound(explosionSound); }
    public void playPlayerDeadSound() { playSound(playerDeadSound); }
    public void playItemPickupSound() { playSound(itemPickupSound); }
    public void playGameOverSound() { playSound(gameOverSound); }
    public void playBombPlacedSound() {playSound(bombPlacedSound);}
    public void playPlayerWalkSound() {playSound(playerWalkSound); }
    // TODO: Thêm các phương thức playSound khác: playEnemyDeadSound(), playBrickBreakSound(), ...
    /**
     * Bắt đầu phát âm thanh bước chân (lặp).
     * Được gọi từ Player khi bắt đầu di chuyển.
     */
    public void startPlayerWalkSound() {
       // System.out.println("Attempting to start player walk sound...");
       // System.out.println("  - playerWalkSound null? " + (playerWalkSound == null));
       // System.out.println("  - isMusicOn? " + isMusicOn);
       //   System.out.println("  - isPlayerWalkSoundPlaying? " + isPlayerWalkSoundPlaying);

        if (playerWalkSound != null && isMusicOn && !isPlayerWalkSoundPlaying) {
            playerWalkSound.setCycleCount(AudioClip.INDEFINITE);
            playerWalkSound.setVolume(2.0); // Đảm bảo âm lượng không phải là 0
            playerWalkSound.play(); // Gọi play trực tiếp
            isPlayerWalkSoundPlaying = true;
            System.out.println("SUCCESS: Starting player walk sound loop.");
        } else {
            System.out.println("INFO: Conditions not met to start player walk sound.");
            if (playerWalkSound == null) System.out.println("  Reason: playerWalkSound is null.");
            if (!isMusicOn) System.out.println("  Reason: isMusicOn is false.");
            if (isPlayerWalkSoundPlaying) System.out.println("  Reason: isPlayerWalkSoundPlaying is already true.");
        }
    }
    /**
     * Dừng phát âm thanh bước chân.
     * Được gọi từ Player khi dừng di chuyển.
     */
    public void stopPlayerWalkSound() {
        // Chỉ dừng nếu clip tồn tại VÀ âm thanh bước chân đang phát
        if (playerWalkSound != null && isPlayerWalkSoundPlaying) {
            System.out.println("DEBUG: Calling playerWalkSound.stop()");
            playerWalkSound.stop(); // Dừng lần phát cuối cùng (trong trường hợp lặp là toàn bộ loop)
            isPlayerWalkSoundPlaying = false;
            System.out.println("Stopping player walk sound loop.");
        }
    }
    // =========================================================================
    // Game Loop (Sửa đổi để ủy quyền)
    // =========================================================================
    private void startGameLoop() {
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

                // --- Ủy quyền cho Controller hiện tại ---
                if (currentController != null) {
                    currentController.update(deltaTime);

                    // Xóa màn hình trước khi vẽ
                    if (gc != null && canvas != null) {
                        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                        // Vẽ thanh UI nền (nếu có) trước khi controller vẽ
                        if (UI_PANEL_HEIGHT > 0) {
                            gc.setFill(Color.rgb(40, 40, 40));
                            gc.fillRect(0, 0, canvas.getWidth(), UI_PANEL_HEIGHT);
                        }
                        // Controller vẽ nội dung chính
                        currentController.render(gc);
                    }
                }
            }
        };
        timer.start();
    }

    // =========================================================================
    // State Management (QUAN TRỌNG)
    // =========================================================================
    public void switchController(GameState newState) {
        System.out.println("Switching state from " + currentState + " to: " + newState);
        // Dừng nhạc nền khi vào các trạng thái tạm dừng hoặc kết thúc (có âm thanh riêng)

        if (currentState == GameState.PLAYING && (newState == GameState.PAUSED || newState == GameState.GAME_OVER_TRANSITION)) {
            // Tạm dừng nhạc nền khi vào PAUSED hoặc chuẩn bị Game Over
            stopBackgroundMusic(); // Hoặc bạn có thể chỉ tạm dừng bằng player.pause()
        }
        // (Optional) Gọi onExitState của controller cũ nếu có
        // if (currentController != null && currentController instanceof YourSpecificController) {
        //     ((YourSpecificController) currentController).onExitState();
        // }

        this.currentState = newState; // Cập nhật enum state

        // Tạo controller mới dựa trên newState
        switch (newState) {
            case MENU:
                this.currentController = new MenuController(this);
                playBackgroundMusic();
                break;
            case PLAYING:
                // Playing state chỉ nên được vào sau khi level đã load thành công
                // Việc chuyển sang PLAYING sẽ do requestLoadLevelAndSwitchState xử lý
                // Nếu gọi trực tiếp switchController(PLAYING) ở đây mà chưa load level sẽ lỗi
                playBackgroundMusic();
                if (this.player != null && this.gameMap != null) {
                    this.currentController = new PlayingController(this);
                } else {
                    switchController(GameState.MENU);
                    return;
                }
                break;
            case PAUSED:
                // Chỉ vào Pause từ Playing
                if (currentController instanceof PlayingController) {
                    this.currentController = new PauseController(this);
                } else {
                    System.err.println("Cannot switch to PAUSED state from " + currentState);
                    return; // Không chuyển state
                }
                break;
            case GAME_OVER_TRANSITION:
                // Thường vào từ Playing
                if (currentController instanceof PlayingController) {
                    this.currentController = new GameOverTransitionController(this);
                } else {
                    System.err.println("Cannot switch to GAME_OVER_TRANSITION state from " + currentState);
                    return; // Không chuyển state
                }
                break;
            case GAME_OVER:
                // Thường vào từ GameOverTransition
                if (currentController instanceof GameOverTransitionController) {
                    this.currentController = new GameOverController(this);
                    // Phát âm thanh Game Over khi vào trạng thái này
                    playGameOverSound();
                } else {
                    System.err.println("Cannot switch to GAME_OVER state from " + currentState);
                    // Có thể fallback về Menu hoặc trạng thái an toàn khác
                    switchController(GameState.MENU);
                    return; // Không chuyển state
                }
                break;
            default:
                System.err.println("Unknown or unsupported GameState: " + newState);
                // Fallback về Menu
                switchController(GameState.MENU);
                break;
        }

        // (Optional) Gọi onEnterState của controller mới nếu có
        // if (currentController != null && currentController instanceof YourSpecificController) {
        //     ((YourSpecificController) currentController).onEnterState();
        // }
    }

    /**
     * Phương thức trung gian để xử lý yêu cầu load level và chuyển state.
     * Đảm bảo level được load thành công trước khi thực sự chuyển sang state mong muốn.
     */
    public void requestLoadLevelAndSwitchState(int levelNumber, GameState targetState) {
        System.out.println("Requesting load level " + levelNumber + " and switch to " + targetState);
        boolean loadSuccess = loadLevel(levelNumber); // loadLevel trả về true/false

        if (loadSuccess) {
            // Nếu load thành công, chuyển sang state mong muốn
            switchController(targetState);
        } else {
            // Nếu load thất bại, quay về Menu (hoặc hiển thị lỗi)
            System.err.println("Failed to load level " + levelNumber + ". Returning to MENU.");
            switchController(GameState.MENU);
        }
    }


    // =========================================================================
    // Level Management (Sửa đổi để trả về boolean)
    // =========================================================================
    private boolean loadLevel(int levelNumber) { // Sửa đổi để trả về boolean
        try {
            System.out.println("Loading level " + levelNumber + "...");
            this.currentLevel = levelNumber;

            // --- Reset state trước khi load ---
            this.score = 0; // Reset score khi load level mới (theo code gốc)
            this.levelTimeRemaining = LEVEL_DURATION_SECONDS;
            this.portalActivated = false;
            this.portalGridX = -1;
            this.portalGridY = -1;
            bombs.clear();
            flames.clear();
            enemies.clear(); // TODO
            items.clear();
            temporaryAnimations.clear();
            particles.clear();
            player = null; // Quan trọng: Player sẽ được tạo lại
            hiddenItemsData = null;
            lastScreenSnapshot = null; // Xóa snapshot cũ
            System.out.println("Level state reset.");

            // --- Load Map Data ---
            MapData mapData = new MapData(levelNumber);
            gameMap = new com.example.bomberman.Map.Map(mapData, this);
            hiddenItemsData = mapData.getHiddenItems();

            // --- Cập nhật Canvas ---
            int canvasWidth = mapData.getCols() * Sprite.SCALED_SIZE;
            int canvasHeight = mapData.getRows() * Sprite.SCALED_SIZE + UI_PANEL_HEIGHT;
            if (canvas == null) { // Tạo mới nếu chưa có
                canvas = new Canvas(canvasWidth, canvasHeight);
                gc = canvas.getGraphicsContext2D();
                // Cần cập nhật Group và Scene nếu canvas được tạo mới sau khi stage đã show()?
                // Cách tốt hơn là tạo canvas ở start() và chỉ resize ở đây.
            } else {
                canvas.setWidth(canvasWidth);
                canvas.setHeight(canvasHeight);
            }

            // --- Tạo Entities ---
            initializeEntitiesFromMap(mapData); // Tạo player, enemies...

            // --- Kiểm tra và cập nhật InputHandler ---
            if (player == null) {
                throw new IllegalStateException("Player not found in map data for level " + levelNumber);
            }
            if (inputHandler != null) {
                inputHandler.setPlayer(player); // Cập nhật Player cho InputHandler
            } else {
                System.err.println("InputHandler is null during loadLevel!");
                // Có thể cần tạo InputHandler ở đây nếu nó chưa được tạo ở start()
            }

            System.out.println("Level " + levelNumber + " loaded successfully.");
            return true; // Load thành công

        } catch (Exception e) {
            System.err.println("Error loading level " + levelNumber + ": " + e.getMessage());
            e.printStackTrace();
            // Reset các thành phần quan trọng về null để tránh lỗi tiếp theo
            canvas = null; // Hoặc đặt lại kích thước mặc định?
            gc = null;
            player = null;
            gameMap = null;
            hiddenItemsData = null;
            // Không chuyển state ở đây, hàm gọi sẽ xử lý (ví dụ quay về Menu)
            return false; // Load thất bại
        }
    }

    // --- initializeEntitiesFromMap (Giữ nguyên logic gốc) ---
    private void initializeEntitiesFromMap(MapData mapData) {
        char[][] charMap = mapData.getMap();
        for (int i = 0; i < mapData.getRows(); i++) {
            for (int j = 0; j < mapData.getCols(); j++) {
                char mapChar = charMap[i][j];
                switch (mapChar) {
                    case 'p':
                        if (player == null) {
                            player = new Player(j, i, gameMap, this);
                            if (gameMap != null) { // Đặt tile dưới player
                                gameMap.setTile(j, i, Tile.createTileFromChar(j, i, ' '));
                            }
                        } // Bỏ qua nếu player đã tồn tại
                        break;
                    case '1': // Balloom
                        enemies.add(new Balloom(j, i, gameMap,this));
                        if (gameMap != null) gameMap.setTile(j, i, Tile.createTileFromChar(j, i, ' '));
                        System.out.println("Balloom placeholder at grid (" + j + ", " + i + ")");
                        break;
                    case '2':
                        enemies.add(new Oneal(j, i, gameMap, this)); // Tạo Oneal
                        if (gameMap != null) gameMap.setTile(j, i, Tile.createTileFromChar(j, i, ' '));
                        System.out.println("Oneal created at grid (" + j + ", " + i + ")");
                        break;
                    case '3':
                        enemies.add(new Doll(j,i,gameMap,this));
                        if (gameMap != null) gameMap.setTile(j, i, Tile.createTileFromChar(j, i, ' '));
                        break;
                    case '4':
                        enemies.add(new Ghost(j,i,gameMap,this));
                        if (gameMap != null) gameMap.setTile(j, i, Tile.createTileFromChar(j, i, ' '));
                        break;
                    case 'x': // Portal
                        if (portalGridX == -1) { portalGridX = j; portalGridY = i; }
                        break;
                    // ... (Các case khác như gốc) ...
                    case 'b': case 'f': case 's': case 'l': case 'a': break; // Chỉ là tile ban đầu
                    case '#': case '*': case ' ': break; // Tile tĩnh
                    default: System.err.println("Warning: Unknown map char '" + mapChar + "'"); break;
                }
            }
        }
        // Đảm bảo Player được tạo
        if (player == null) {
            throw new IllegalStateException("Player could not be created from map data!");
        }
    }

    // =========================================================================
    // Entity Update Methods (Giữ nguyên, sẽ được gọi bởi PlayingController)
    // =========================================================================
    public void updateBombs(double deltaTime) { /* ... Code gốc ... */
        Iterator<Bomb> iterator = bombs.iterator();
        while (iterator.hasNext()) {
            Bomb bomb = iterator.next();
            bomb.update(deltaTime);
            if (!bomb.isActive()) {
                List<Flame> newFlames = bomb.getGeneratedFlames();
                if (newFlames != null && !newFlames.isEmpty()) {
                    addFlames(newFlames);
                }
                iterator.remove();
                Object owner = bomb.getOwner();
                if (owner instanceof Player) {
                    ((Player) owner).increaseBombCount();
                } else if (owner instanceof Doll) { // Hoặc nếu bạn dùng owner = null cho bom của Doll
                    ((Doll) owner).enemyBombExploded();
                } else if (owner == null) {

                }
            }
        }
    }
    public void updateFlames(double deltaTime) { /* ... Code gốc ... */
        Iterator<Flame> iterator = flames.iterator();
        while (iterator.hasNext()) {
            Flame flame = iterator.next();
            flame.update(deltaTime);
            if (!flame.isActive()) {
                iterator.remove();
            }
        }
    }
    public void updateItems(double deltaTime) { /* ... Code gốc ... */
        Iterator<Item> iterator = items.iterator();
        while (iterator.hasNext()) {
            Item item = iterator.next();
            item.update(deltaTime);
            if (!item.isActive()) {
                iterator.remove();
            }
        }
    }
    public void updateTemporaryAnimations(double deltaTime) { /* ... Code gốc ... */
        Iterator<TemporaryAnimation> iterator = temporaryAnimations.iterator();
        while (iterator.hasNext()) {
            TemporaryAnimation anim = iterator.next();
            anim.update(deltaTime);
            if (!anim.isActive()) {
                handleAnimationFinished(anim); // Gọi hàm xử lý item spawn
                iterator.remove();
            }
        }
    }
    public void updateEnemies(double deltaTime) {
        Iterator<Enemy> iterator = enemies.iterator();
        while (iterator.hasNext()) {
            Enemy enemy = iterator.next();
            enemy.update(deltaTime);
            // Xóa Enemy nếu animation chết đã hoàn thành
            if (!enemy.isAlive() && enemy.getDyingTimer() >= enemy.getTimeToDie()) {
                iterator.remove();
                System.out.println("Removed dead enemy from list.");
            }
        }
    }

    // =========================================================================
    // Game Logic & Callbacks (Giữ nguyên)
    // =========================================================================
    public void addBomb(Bomb bomb) { if (bomb != null) bombs.add(bomb); }
    public void addFlames(List<Flame> newFlames) { if (newFlames != null) flames.addAll(newFlames); }
    public void addScore(int points) { if (points > 0) this.score += points; /* Tạm bỏ log */ }

    public void brickDestroyed(int gridX, int gridY) {
        addScore(10);
        double animationDuration = 0.5;
        double frameDuration = animationDuration / 3;
        Animation brickExplosionAnim = new Animation(frameDuration, false,
                Sprite.brick_exploded, Sprite.brick_exploded1, Sprite.brick_exploded2);
        TemporaryAnimation brickAnim = new TemporaryAnimation(gridX, gridY, brickExplosionAnim);
        temporaryAnimations.add(brickAnim);
        // TODO: Play sound
    }

    private void handleAnimationFinished(TemporaryAnimation finishedAnimation) { // Được gọi từ updateTemporaryAnimations
        int gridX = finishedAnimation.getGridX();
        int gridY = finishedAnimation.getGridY();
        if (gameMap != null) {
            gameMap.setTile(gridX, gridY, Tile.createTileFromChar(gridX, gridY, ' '));
        }
        char itemToSpawnChar = '\0';
        String itemKey = gridX + "," + gridY;
        if (hiddenItemsData != null && hiddenItemsData.containsKey(itemKey)) {
            itemToSpawnChar = hiddenItemsData.get(itemKey);
            hiddenItemsData.remove(itemKey);
        } else {
            double randomDropProbability = 0.1; // Giá trị gốc
            if (random.nextDouble() < randomDropProbability) {
                char[] possibleRandomItems = {'b', 'f', 's', 'l', 'a'};
                itemToSpawnChar = possibleRandomItems[random.nextInt(possibleRandomItems.length)];
            }
        }
        if (itemToSpawnChar != '\0') {
            spawnItemAt(gridX, gridY, itemToSpawnChar);
        }
    }

    public void spawnItemAt(int gridX, int gridY, char itemTypeChar) { /* ... Code gốc ... */
        Item newItem = null;
        switch (itemTypeChar) {
            case 'b': newItem = new BombItem(gridX, gridY); break;
            case 'f': newItem = new FlameItem(gridX, gridY); break;
            case 's': newItem = new SpeedItem(gridX, gridY); break;
            case 'l': newItem = new LifeItem(gridX, gridY); break;
            case 'a': newItem = new KickBombItem(gridX, gridY); break;
            default: System.err.println("Warning: Unknown item type '" + itemTypeChar + "'"); break;
        }
        if (newItem != null) {
            items.add(newItem);
            // System.out.println("Spawned Item: " + newItem.getClass().getSimpleName());
        }
    }

    // =========================================================================
    // Collision & Interaction Handling (Giữ nguyên, gọi bởi PlayingController)
    // =========================================================================
    public void handleKickBombTrigger() { /* ... Code gốc ... */
        if (player != null && player.kickableBombPending != null && player.kickDirectionPending != Direction.NONE) {
            Bomb bombToKick = player.kickableBombPending;
            Direction directionToKick = player.kickDirectionPending;
            // Giả định Bomb có startKicking()
            bombToKick.startKicking(directionToKick, Bomb.KICK_SPEED_CONSTANT);
            player.kickableBombPending = null;
            player.kickDirectionPending = Direction.NONE;
        }
    }
    public void handleFlameBombCollisions() { /* ... Code gốc ... */
        List<Flame> currentFlames = new ArrayList<>(flames);
        List<Bomb> currentBombs = new ArrayList<>(bombs);
        for (Flame flame : currentFlames) {
            if (!flame.isActive()) continue;
            for (Bomb bomb : currentBombs) {
                if (bomb.isActive() && !bomb.isExploded()) {
                    if (flame.getGridX() == bomb.getGridX() && flame.getGridY() == bomb.getGridY()) {
                        // Giả định Bomb có triggerExplosion()
                        bomb.triggerExplosion();
                    }
                }
            }
        }
    }
    public void handlePlayerItemCollisions() { /* ... Code gốc ... */
        if (player == null || !player.isAlive()) return;
        Iterator<Item> itemIterator = items.iterator();
        while (itemIterator.hasNext()) {
            Item item = itemIterator.next();
            // Giả định Player có collidesWith(Item) và Item có applyEffect(Player)
            if (item.isActive() && player.collidesWith(item)) {
                item.applyEffect(player);
                item.setActive(false); // Item tự xóa trong updateItems
                addScore(50);
                if (this != null) { // Đảm bảo gameManager không null (mặc dù this không bao giờ null ở đây)
                    playItemPickupSound(); // T (gọi phương thức của Bomberman)
                }// Cộng điểm khi nhặt item (thêm vào)
            }
        }
    }
    public void handlePortalTransition(){
        if (player != null && player.isAlive() && portalActivated && portalGridX != -1) {
            int playerCurrentGridX = player.getGridX();
            int playerCurrentGridY = player.getGridY();

            if (playerCurrentGridX == portalGridX && playerCurrentGridY == portalGridY) {

                System.out.println("Player entered portal! (Stored Grid Coordinates Match)");

                if (levelTimeRemaining > 0) { // Cộng điểm thời gian nếu còn
                    addScore((int)(levelTimeRemaining * 2));
                    System.out.println("Added time bonus score.");
                }
                addScore(1000); // Cộng điểm qua màn cố định
                System.out.println("Added level clear bonus score.");

                currentLevel++; // Tăng số level hiện tại

                if (currentLevel <= MAX_LEVEL) { // Kiểm tra xem còn level để chơi không
                    System.out.println("Loading next level: " + currentLevel);
                    requestLoadLevelAndSwitchState(currentLevel, GameState.PLAYING);
                } else { // Đã hoàn thành level cuối cùng
                    System.out.println("CONGRATULATIONS! YOU BEAT THE GAME!");
                    // TODO: Chuyển sang trạng thái GAME_WON thay vì đóng cửa sổ
                    if (primaryStage != null) {
                        primaryStage.close(); // Đóng cửa sổ game
                    }
                }
            }
        }
    }
    // Trong Bomberman.java
    public void handleFlameEnemyCollisions() {
        List<Flame> currentFlames = new ArrayList<>(flames); // Tránh ConcurrentModificationException
        List<Enemy> currentEnemies = new ArrayList<>(enemies);

        for (Flame flame : currentFlames) {
            if (!flame.isActive()) continue;
            for (Enemy enemy : currentEnemies) {
                // Chỉ xử lý va chạm nếu Enemy còn sống
                if (enemy.isAlive()) {
                    // Kiểm tra va chạm AABB đơn giản giữa tâm Flame và tâm Enemy
                    double flameCenterX = flame.getPixelX() + Sprite.SCALED_SIZE / 2.0;
                    double flameCenterY = flame.getPixelY() + Sprite.SCALED_SIZE / 2.0;
                    double enemyCenterX = enemy.getX() + Sprite.SCALED_SIZE / 2.0;
                    double enemyCenterY = enemy.getY() + Sprite.SCALED_SIZE / 2.0;
                    double dx = flameCenterX - enemyCenterX;
                    double dy = flameCenterY - enemyCenterY;
                    double distance = Math.sqrt(dx*dx + dy*dy);
                    double collisionDistance = Sprite.SCALED_SIZE * 0.7; // Khoảng cách va chạm (điều chỉnh)

                    if (distance < collisionDistance) {
                        enemy.die(); // Gọi phương thức die() của Enemy
                        // Flame không biến mất khi chạm Enemy
                    }
                }
            }
        }
    }
    // Thêm phương thức này vào lớp Bomberman.java
    public void handlePlayerFlameCollisions() {
        // Chỉ kiểm tra nếu Player tồn tại, còn sống và không đang trong trạng thái chết tạm thời
        if (player == null || !player.isAlive() || player.isDyingTemporarily()) {
            return; // Không xử lý va chạm nếu Player không hợp lệ
        }

        // Kiểm tra xem Player có đang bất tử không
        if (player.isInvincible()) { // isInvincible là thuộc tính public, nếu là private dùng player.isInvincible()
            // Player đang bất tử, bỏ qua va chạm với lửa
            return;
        }

        // Duyệt qua tất cả các ngọn lửa đang hoạt động
        List<Flame> currentFlames = new ArrayList<>(flames); // Tạo bản sao để tránh lỗi nếu danh sách flames thay đổi trong vòng lặp (ít xảy ra ở đây nhưng là thói quen tốt)
        for (Flame flame : currentFlames) {
            // Chỉ kiểm tra va chạm với ngọn lửa còn hoạt động
            if (flame.isActive()) {
                // --- Kiểm tra va chạm dạng hình hộp (Axis-Aligned Bounding Box - AABB) ---
                // So sánh vị trí và kích thước của hộp va chạm Player và Flame.
                // Giả định cả hai đều có kích thước va chạm bằng Sprite.SCALED_SIZE
                // và pixelX/pixelY là vị trí góc trên bên trái tương đối so với khu vực game (dưới UI panel).

                double playerLeft = player.getPixelX();
                double playerRight = player.getPixelX() + Sprite.SCALED_SIZE;
                double playerTop = player.getPixelY();
                double playerBottom = player.getPixelY() + Sprite.SCALED_SIZE;

                double flameLeft = flame.getPixelX();
                double flameRight = flame.getPixelX() + Sprite.SCALED_SIZE;
                double flameTop = flame.getPixelY();
                double flameBottom = flame.getPixelY() + Sprite.SCALED_SIZE;

                // Kiểm tra sự chồng lấn giữa hai hình chữ nhật
                boolean overlap = playerRight > flameLeft && playerLeft < flameRight &&
                        playerBottom > flameTop && playerTop < flameBottom;

                // --- Hoặc sử dụng phương pháp kiểm tra khoảng cách tâm (giống PlayerEnemyCollisions) ---
                // double playerCenterX = player.getPixelX() + Sprite.SCALED_SIZE / 2.0;
                // double playerCenterY = player.getPixelY() + Sprite.SCALED_SIZE / 2.0;
                // double flameCenterX = flame.getPixelX() + Sprite.SCALED_SIZE / 2.0;
                // double flameCenterY = flame.getPixelY() + Sprite.SCALED_SIZE / 2.0;
                // double dx = playerCenterX - flameCenterX;
                // double dy = playerCenterY - flameCenterY;
                // double distance = Math.sqrt(dx*dx + dy*dy);
                // double collisionThreshold = Sprite.SCALED_SIZE * 0.8; // Điều chỉnh ngưỡng va chạm

                // if (distance < collisionThreshold) { ... va chạm ... }


                // Nếu có va chạm chồng lấn
                if (overlap) { // Nếu dùng phương pháp AABB
                    // if (distance < collisionThreshold) { // Nếu dùng phương pháp khoảng cách
                    System.out.println("Player collided with flame at (" + flame.getGridX() + ", " + flame.getGridY() + ")!"); // Log
                    // Gọi phương thức Player.takeDamage(). Player sẽ tự xử lý giảm mạng, animation chết tạm thời, và hồi sinh/game over.
                    player.takeDamage(1); // Mất 1 mạng

                    // Quan trọng: Thoát khỏi vòng lặp ngay sau khi Player nhận sát thương
                    // để tránh Player nhận sát thương nhiều lần từ cùng một vụ nổ trong một frame.
                    return;
                }
            }
        }
    }

    public void handlePlayerEnemyCollisions() {
        if (player == null || !player.isAlive()) return;

        List<Enemy> currentEnemies = new ArrayList<>(enemies);
        for (Enemy enemy : currentEnemies) {
            if (enemy.isAlive()) { // Chỉ va chạm với Enemy còn sống
                double playerCenterX = player.getPixelX() + Sprite.SCALED_SIZE / 2.0;
                double playerCenterY = player.getPixelY() + Sprite.SCALED_SIZE / 2.0;
                double enemyCenterX = enemy.getX() + Sprite.SCALED_SIZE / 2.0;
                double enemyCenterY = enemy.getY() + Sprite.SCALED_SIZE / 2.0;
                double dx = playerCenterX - enemyCenterX;
                double dy = playerCenterY - enemyCenterY;
                double distance = Math.sqrt(dx*dx + dy*dy);
                double collisionDistance = Sprite.SCALED_SIZE * 0.8; // Khoảng cách va chạm (điều chỉnh)

                if (distance < collisionDistance) {
                    System.out.println("Player collided with an enemy!");
                    // Player chết khi va chạm Enemy
                    player.takeDamage(1);
                    return;
                }
            }
        }
    }
    // =========================================================================
    // Rendering Components (Được gọi bởi các Controller)
    // =========================================================================
    /** Vẽ các thành phần game chính (map, entities) lên gc. */
    public void renderGameComponents(GraphicsContext gc) {
        if (gc == null) return;
        // Vẽ map nền
        if (gameMap != null) gameMap.render(gc);
        // Vẽ các lớp entities theo thứ tự
        renderItems(gc);          // Items
        renderBombs(gc);          // Bombs
        renderTemporaryAnimations(gc); // Hiệu ứng nổ gạch,...
        renderEnemies(gc);     // TODO: Enemies
        renderFlames(gc);         // Flames (có thể vẽ trên player?)
        if (player != null) player.render(gc); // Player
        // Vẽ UI luôn được gọi sau cùng bởi vòng lặp chính (handle) sau khi controller render
        renderUI(gc); // Vẽ thanh UI thông tin
    }

    // Các hàm render con (giữ nguyên)
    private void renderBombs(GraphicsContext gc) { for (Bomb b : bombs) b.render(gc); }
    private void renderFlames(GraphicsContext gc) { for (Flame f : flames) f.render(gc); }
    private void renderItems(GraphicsContext gc) { for (Item i : items) i.render(gc); }
    private void renderTemporaryAnimations(GraphicsContext gc) { for (TemporaryAnimation ta : temporaryAnimations) ta.render(gc); }
    private void renderEnemies(GraphicsContext gc) {
        for (Enemy e : enemies) {
            e.render(gc);
        }
    }
    private void renderUI(GraphicsContext gc) { /* ... Code gốc ... */
        if (gc == null || canvas == null || uiFont == null) return;
        int currentLives = (player != null && player.isAlive()) ? player.getLives() : 0;
        int currentScore = this.score;
        int currentLevelDisplay = this.currentLevel; // Dùng level hiện tại của Bomberman
        int totalSecondsRemaining = Math.max(0, (int) Math.ceil(levelTimeRemaining));
        double canvasWidth = canvas.getWidth();
        double padding = 10;
        double textBaselineOffsetY = 5;
        double yPositionText = UI_PANEL_HEIGHT / 2.0 + textBaselineOffsetY;
        gc.setFill(Color.WHITE);
        gc.setFont(this.uiFont);

        // Level
        String levelText = "LEVEL: " + currentLevelDisplay;
        gc.fillText(levelText, padding, yPositionText);
        // Time
        String timeText = "TIME: " + totalSecondsRemaining;
        double xPositionTime = padding + 150;
        gc.fillText(timeText, xPositionTime, yPositionText);
        // Score
        String scoreText = "SCORE: " + currentScore;
        Text scoreTextNode = new Text(scoreText); scoreTextNode.setFont(uiFont);
        double scoreTextWidth = scoreTextNode.getLayoutBounds().getWidth();
        double xPositionScore = (canvasWidth / 2.0) - (scoreTextWidth / 2.0);
        gc.fillText(scoreText, xPositionScore, yPositionText);
        // Lives
        String livesNumText = "" + currentLives;
        Text livesNumTextNode = new Text(livesNumText); livesNumTextNode.setFont(uiFont);
        double livesNumTextWidth = livesNumTextNode.getLayoutBounds().getWidth();
        double xPositionLivesText = canvasWidth - livesNumTextWidth - padding;
        gc.fillText(livesNumText, xPositionLivesText, yPositionText );
        // Life Icon
        Sprite iconSprite = Sprite.player_down;
        if (iconSprite != null && iconSprite.getFxImage() != null) {
            Image lifeIcon = iconSprite.getFxImage();
            double iconDrawWidth = Sprite.SCALED_SIZE - 4;
            double iconDrawHeight = Sprite.SCALED_SIZE - 8;
            double yPositionIcon = (UI_PANEL_HEIGHT - iconDrawHeight) / 2.0;
            double xPositionIcon = xPositionLivesText - iconDrawWidth - 5;
            gc.drawImage(lifeIcon, xPositionIcon, yPositionIcon, iconDrawWidth, iconDrawHeight);
        }
    }


    // =========================================================================
    // Game Over Particle Logic (Giữ nguyên)
    // =========================================================================
    public void initializeGameOverParticles() { /* ... Code gốc ... */
        particles.clear();
        int numberOfParticles = 100; // Số gốc
        if (canvas != null) {
            for (int i = 0; i < numberOfParticles; i++) {
                // Giả định Particle constructor nhận width, height
                particles.add(new Particle(canvas.getWidth(), canvas.getHeight()));
            }
        }
    }
    public void updateGameOverParticles(double deltaTime) { /* ... Code gốc ... */
        if (canvas != null) {
            for (Particle p : particles) {
                // Giả định Particle update nhận delta và canvas height
                p.update(deltaTime, canvas.getHeight());
            }
        }
    }

    // =========================================================================
    // Utility Methods & Getters (Thêm các getter cần thiết cho Controller)
    // =========================================================================
    public GameState getCurrentState() { return currentState; }
    public SceneController getCurrentController() { return currentController; } // QUAN TRỌNG cho InputHandler
    public Stage getPrimaryStage() { return primaryStage; }
    public GraphicsContext getGraphicsContext() { return gc; } // Cần cho render
    public Canvas getCanvas() { return canvas; } // Cần để lấy kích thước, snapshot
    public double getCanvasWidth() { return (canvas != null) ? canvas.getWidth() : 0; }
    public double getCanvasHeight() { return (canvas != null) ? canvas.getHeight() : 0; }
    public Player getPlayer() { return player; }
    public com.example.bomberman.Map.Map getGameMap() { return gameMap; }
    public List<Enemy> getEnemies() { return enemies; }
    public List<Bomb> getBombs() { return bombs; }
    public List<Item> getItems() { return items; }
    public List<Flame> getFlames() { return flames;} // Thêm getter nếu cần
    public List<TemporaryAnimation> getTemporaryAnimations() { return temporaryAnimations;} // Thêm getter nếu cần
    public List<Particle> getParticles() { return particles; } // Cần cho GameOverController
    public int getScore() { return score; }
    public double getLevelTimeRemaining() { return levelTimeRemaining; }
    public void setLevelTimeRemaining(double time) { this.levelTimeRemaining = time; } // Cần setter
    public boolean isPortalActivated() { return portalActivated; }
    public void setPortalActivated(boolean activated) { this.portalActivated = activated; } // Cần setter
    public Font getUiFont() { return uiFont; }
    public Font getGameOverFont() { return gameOverFont; }
    public Image getMenuBackground() { return menuBackground; }
    public Image getHandCursorImage() { return handCursorImage; }
    public Image getLastScreenSnapshot() { return lastScreenSnapshot; }
    public void setLastScreenSnapshot(Image snapshot) { this.lastScreenSnapshot = snapshot; } // Cần setter
    public Animation getPlayerDeadAnimation() { return playerDeadAnimation; }
    public InputHandler getInputHandler() { return inputHandler; } // Cần cho PauseController
    // Cần setter


    // Helper nhận KeyCode trả về Direction (có thể đặt ở InputHandler hoặc đây)
    public Direction getDirectionFromKey(KeyCode code) {
        switch (code) {
            case W: case UP: return Direction.UP;
            case S: case DOWN: return Direction.DOWN;
            case A: case LEFT: return Direction.LEFT;
            case D: case RIGHT: return Direction.RIGHT;
            default: return Direction.NONE;
        }
    }
    // Helper kiểm tra phím di chuyển
    public boolean isMovementKey(KeyCode code) {
        return code == KeyCode.W || code == KeyCode.S || code == KeyCode.A || code == KeyCode.D
                || code == KeyCode.UP || code == KeyCode.DOWN || code == KeyCode.LEFT || code == KeyCode.RIGHT;
    }
    public boolean isBombAtGrid(int gridX, int gridY) {
        // Duyệt qua danh sách bom hiện có trong Bomberman
        for (Bomb bomb : bombs) { // 'bombs' là List<Bomb> của Bomberman
            // Chỉ kiểm tra bom còn active và đúng vị trí
            if (bomb.isActive() && bomb.getGridX() == gridX && bomb.getGridY() == gridY) {
                return true; // Tìm thấy bom tại vị trí
            }
        }
        return false; // Không tìm thấy bom nào tại vị trí đó
    }
    public double getElapsedTime() {
        return Math.max(0, LEVEL_DURATION_SECONDS - this.levelTimeRemaining);
    }

}