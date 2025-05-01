package com.example.bomberman;

import com.example.bomberman.entities.*;
import com.example.bomberman.entities.Items.*;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
import com.example.bomberman.Map.*;

import com.example.bomberman.graphics.Sprite;
import com.example.bomberman.graphics.Animation; // Import lớp Animation

import com.example.bomberman.Input.InputHandler;
import java.io.InputStream;



import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Random;

//Vẽ biểu thị điểm số mạng sống vvv
import javafx.scene.paint.Color; // Import Color
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import java.util.concurrent.TimeUnit;


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

    //GameState MENU vân vân
    private GameState currentState = GameState.MENU;
    private Stage primaryStage;
    private Font uiFont;



    // --- Quản lý màn chơi ---
    private int currentLevel = 1;
    private final int MAX_LEVEL = 10;
    //--- portal có thể di chuyển được hay không---
    private boolean portalActivated = false; // Cờ cho biết Portal đã mở chưa
    private int portalGridX = -1; // Lưu vị trí Portal X
    private int portalGridY = -1;

    private int score = 0;
    private int lives = 3;
    public static final int UI_PANEL_HEIGHT = 32;
    private static final double LEVEL_DURATION_SECONDS = 200.0;
    private double levelTimeRemaining;
    // private GameState gameState = GameState.PLAYING; // Enum GameState (PLAYING, PAUSED, GAME_OVER, LEVEL_CLEARED)

    private InputHandler inputHandler;


    private Random random = new Random();

    // --- THÊM PHƯƠNG THỨC ĐỂ LẤY ĐIỂM ---
    public int getScore() {
        return score;
    }
    public GameState getCurrentState() {
        return currentState;
    }
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    // hiệu ứng POrtal
    public double getElapsedTime() {
        // Cách tính đơn giản nhất là lấy tổng thời gian trừ đi thời gian còn lại
        // Đảm bảo không trả về giá trị âm nếu levelTimeRemaining có thể nhỏ hơn 0
        return Math.max(0, LEVEL_DURATION_SECONDS - levelTimeRemaining);
    }
    public void startGame() {
        System.out.println("Starting game...");
        // Không cần loadLevel(1) ngay ở đây vì start() đã gọi rồi khi khởi tạo
        // Chỉ cần chuyển trạng thái để handle() bắt đầu update/render game
        // Nếu muốn load lại level 1 một cách chắc chắn:
        // loadLevel(1); // Đảm bảo bắt đầu từ level 1 sạch sẽ
        this.currentLevel = 1; // Reset level về 1 nếu cần
        currentState = GameState.PLAYING; // Chuyển sang trạng thái chơi
        // Nếu player đã tồn tại từ lần load đầu, không cần load lại level
        // Nếu muốn load lại hoàn toàn: loadLevel(1);
    }

    /**
     * Khởi động lại game sau khi Game Over: Chuyển state sang PLAYING và load level 1.
     * Được gọi bởi InputHandler khi nhấn Enter ở màn hình Game Over.
     */
    public void restartGame() {
        System.out.println("Restarting game...");
        this.currentLevel = 1; // Đặt lại level về 1
        // Cần load lại level để reset mọi thứ: map, player, score, timer,...
        loadLevel(this.currentLevel);
        // Sau khi loadLevel thành công, trạng thái sẽ tự động được đặt lại nếu cần,
        // nhưng để chắc chắn, đặt lại là PLAYING
        currentState = GameState.PLAYING;
    }
    // -------------------------------------------------------------


    // ... các phương thức khác (start, loadLevel, updateGame, renderGame, renderUI, ...) ...



    // --- THÊM PHƯƠNG THỨC ĐỂ TĂNG ĐIỂM ---
    public void addScore(int points) {
        if (points > 0) {
            this.score += points;
            System.out.println("Score increased by " + points + ". Total score: " + this.score); // Log (tùy chọn)
        }
    }

    private void renderUI(GraphicsContext gc) {
        if (gc == null || canvas == null) return;

        // --- Lấy dữ liệu cần hiển thị ---
        int currentLives = (player != null && player.isAlive()) ? player.getLives() : 0;
        int currentScore = this.score;
        int currentLevel = (gameMap != null) ? gameMap.getLevel() : 0;
        int totalSecondsRemaining = (int) Math.ceil(levelTimeRemaining);

        // --- Lấy kích thước Canvas ---
        double canvasWidth = canvas.getWidth();
        double canvasHeight = canvas.getHeight(); // Không dùng nhưng có thể cần sau này

        // --- Định dạng chữ ---
        gc.setFill(Color.WHITE); // Màu chữ
        Font uiFont = Font.font("Arial", 20); // Chọn Font
        gc.setFont(uiFont);

        // --- Tính toán vị trí ---
        double padding = 10; // Khoảng cách lề
        double textBaselineOffsetY = 5;
        double yPositionText = UI_PANEL_HEIGHT / 2.0 + textBaselineOffsetY;

        double baselineOffset = 5; // Khoảng cách từ đỉnh chữ xuống baseline (ước lượng)



        // 1. Level (Góc trái)
        String levelText = "LEVEL: " + currentLevel; //+ " (" + mapRows + "x" + mapCols + ")"; // Bỏ kích thước map cho gọn

        gc.fillText(levelText, padding, yPositionText);

        // 2. Score (Giữa màn hình)
        String scoreText = "SCORE: " + currentScore;

        Text scoreTextNode = new Text(scoreText); // Dùng Text để đo chính xác
        scoreTextNode.setFont(uiFont);
        double scoreTextWidth = scoreTextNode.getLayoutBounds().getWidth();
        double xPositionScore = (canvasWidth / 2.0) - (scoreTextWidth / 2.0);
        gc.fillText(scoreText, xPositionScore, yPositionText);
        // 3. Lives (Góc phải)

        double iconDrawWidth = Sprite.SCALED_SIZE-4; // Kích thước vẽ icon mong muốn
        double iconDrawHeight = Sprite.SCALED_SIZE-8;
        double yPositionIcon = (UI_PANEL_HEIGHT - iconDrawHeight) / 2.0;

        Image lifeIcon = null;
        Sprite iconSprite = Sprite.player_down; // Chọn icon sprite

        if (iconSprite != null && iconSprite.getFxImage() != null) {
            lifeIcon = iconSprite.getFxImage();
        }

        String livesNumText = "" + currentLives;
        Text livesNumTextNode = new Text(livesNumText);
        livesNumTextNode.setFont(uiFont);
        double livesNumTextWidth = livesNumTextNode.getLayoutBounds().getWidth();
        // Đặt text gần mép phải
        double xPositionLivesText = canvasWidth - livesNumTextWidth - padding;
        gc.fillText(livesNumText, xPositionLivesText, yPositionText );

        // Vẽ Icon ngay bên trái số mạng
        if (lifeIcon != null) {
            double xPositionIcon = xPositionLivesText - iconDrawWidth - 5; // Cách text 5px
            gc.drawImage(lifeIcon, xPositionIcon, yPositionIcon, iconDrawWidth, iconDrawHeight);
        }


        //Time

        String timeText = "TIME: " + totalSecondsRemaining;
        double xPositionTime = padding + 150; // Điều chỉnh khoảng cách 150 nếu cần
        gc.fillText(timeText, xPositionTime, yPositionText);

    }
    private void renderMenu(GraphicsContext gc) {
        if (gc == null || canvas == null) return;

        // --- Vẽ nền (ví dụ: đen) ---
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // --- Vẽ Tiêu đề Game ---
        String title = "BOMBERMAN";
        gc.setFill(Color.YELLOW);
        // Chọn Font lớn cho tiêu đề
        Font titleFont = (this.uiFont != null) ? Font.font(this.uiFont.getFamily(), 60) : Font.font("Arial", 60);
        gc.setFont(titleFont);
        // Căn giữa tiêu đề
        Text titleNode = new Text(title);
        titleNode.setFont(titleFont);
        double titleWidth = titleNode.getLayoutBounds().getWidth();
        gc.fillText(title, (canvas.getWidth() / 2.0) - (titleWidth / 2.0), 150); // Điều chỉnh Y nếu cần

        // --- Vẽ tùy chọn "Start Game" ---
        String startText = "Press ENTER to Start";
        gc.setFill(Color.WHITE);
        // Dùng font nhỏ hơn
        Font optionFont = (this.uiFont != null) ? Font.font(this.uiFont.getFamily(), 24) : Font.font("Arial", 24);
        gc.setFont(optionFont);
        // Căn giữa
        Text startNode = new Text(startText);
        startNode.setFont(optionFont);
        double startWidth = startNode.getLayoutBounds().getWidth();
        gc.fillText(startText, (canvas.getWidth() / 2.0) - (startWidth / 2.0), 300); // Điều chỉnh Y

        // TODO: Thêm các tùy chọn khác như "High Scores", "Exit" nếu muốn
    }
    private void renderGameOverScreen(GraphicsContext gc) {
        if (gc == null || canvas == null) return;

        // --- Vẽ nền (ví dụ: đỏ sẫm hoặc đen) ---
        gc.setFill(Color.DARKRED);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // --- Vẽ chữ "GAME OVER" ---
        String gameOverText = "GAME OVER";
        gc.setFill(Color.WHITE);
        // Font lớn
        Font gameOverFont = (this.uiFont != null) ? Font.font(this.uiFont.getFamily(), 60) : Font.font("Arial", 60);
        gc.setFont(gameOverFont);
        // Căn giữa
        Text goNode = new Text(gameOverText);
        goNode.setFont(gameOverFont);
        double goWidth = goNode.getLayoutBounds().getWidth();
        gc.fillText(gameOverText, (canvas.getWidth() / 2.0) - (goWidth / 2.0), 150);

        // --- Vẽ Điểm số ---
        String scoreText = "Final Score: " + this.score; // Lấy điểm cuối cùng
        gc.setFill(Color.YELLOW);
        // Font nhỏ hơn
        Font scoreFont = (this.uiFont != null) ? Font.font(this.uiFont.getFamily(), 24) : Font.font("Arial", 24);
        gc.setFont(scoreFont);
        // Căn giữa
        Text scoreNode = new Text(scoreText);
        scoreNode.setFont(scoreFont);
        double scoreWidth = scoreNode.getLayoutBounds().getWidth();
        gc.fillText(scoreText, (canvas.getWidth() / 2.0) - (scoreWidth / 2.0), 250);

        // --- Vẽ tùy chọn "Restart" / "Exit" ---
        String restartText = "Press ENTER to Restart";
        String exitText = "Press ESC to Exit";
        gc.setFill(Color.WHITE);
        gc.setFont(scoreFont); // Dùng lại font điểm số

        // Căn giữa Restart
        Text restartNode = new Text(restartText);
        restartNode.setFont(scoreFont);
        double restartWidth = restartNode.getLayoutBounds().getWidth();
        gc.fillText(restartText, (canvas.getWidth() / 2.0) - (restartWidth / 2.0), 350);

        // Căn giữa Exit
        Text exitNode = new Text(exitText);
        exitNode.setFont(scoreFont);
        double exitWidth = exitNode.getLayoutBounds().getWidth();
        gc.fillText(exitText, (canvas.getWidth() / 2.0) - (exitWidth / 2.0), 400); // Cách Restart một chút
    }
    // --- THÊM PHƯƠNG THỨC XỬ LÝ VA CHẠM LỬA - BOM ---
    private void handleFlameBombCollisions() {
        // Sử dụng List tạo mới để tránh ConcurrentModificationException nếu bom nổ tạo flame mới ngay
        List<Flame> currentFlames = new ArrayList<>(flames);
        List<Bomb> currentBombs = new ArrayList<>(bombs);

        for (Flame flame : currentFlames) {
            // Chỉ kiểm tra flame còn active
            if (!flame.isActive()) continue;

            for (Bomb bomb : currentBombs) {
                if (bomb.isActive() && !bomb.isExploded()) {
                    if (flame.getGridX() == bomb.getGridX() && flame.getGridY() == bomb.getGridY()) {
                        bomb.triggerExplosion();
                        // Không cần break vì một flame có thể kích nổ nhiều bom trên đường đi
                    }
                }
            }
        }
    }
    private void handlePlayerItemCollisions() {
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
    public void togglePause() {
        if (currentState == GameState.PLAYING) {
            currentState = GameState.PAUSED;
            System.out.println("Game Paused"); if (inputHandler != null) {
                inputHandler.clearMovingKeys(); // Gọi hàm mới trong InputHandler
            }
            if (player != null) {
                player.setMovingDirection(Direction.NONE); // Bảo Player dừng lại ngay
            }
            // ---------------------------------

            // TODO: Có thể dừng nhạc nền ở đây
        } else if (currentState == GameState.PAUSED) {
            currentState = GameState.PLAYING;
            System.out.println("Game Resumed");
            // TODO: Có thể tiếp tục nhạc nền ở đây
        }
        // Không làm gì nếu đang ở trạng thái khác (MENU, GAME_OVER)
    }

    private void renderPauseScreen(GraphicsContext gc) {
        if (gc == null || canvas == null) return;

        // --- Vẽ lớp phủ mờ --- (Tùy chọn)
        gc.setFill(Color.rgb(0, 0, 0, 0.5)); // Màu đen với độ trong suốt 50%
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // --- Vẽ chữ "PAUSED" ---
        String pauseText = "PAUSED";
        gc.setFill(Color.YELLOW); // Màu chữ Pause
        // Sử dụng font đã load hoặc font lớn hơn
        Font pauseFont = Font.font(uiFont.getFamily(), 48); // Lấy family từ font UI, đặt cỡ 48
        gc.setFont(pauseFont);

        // Căn chữ "PAUSED" ra giữa màn hình
        Text textNode = new Text(pauseText);
        textNode.setFont(pauseFont);
        double textWidth = textNode.getLayoutBounds().getWidth();
        double textHeight = textNode.getLayoutBounds().getHeight(); // Cần để căn giữa Y nếu muốn
        double x = (canvas.getWidth() / 2.0) - (textWidth / 2.0);
        double y = (canvas.getHeight() / 2.0) + (textHeight / 4.0); // Căn chỉnh baseline Y

        gc.fillText(pauseText, x, y);

        // Có thể vẽ thêm hướng dẫn "Press ESC to Resume"
        gc.setFont(uiFont); // Quay lại font nhỏ hơn
        gc.setFill(Color.WHITE);
        String resumeText = "Press ESC to Resume";
        Text resumeTextNode = new Text(resumeText);
        resumeTextNode.setFont(uiFont);
        double resumeTextWidth = resumeTextNode.getLayoutBounds().getWidth();
        gc.fillText(resumeText, (canvas.getWidth() / 2.0) - (resumeTextWidth / 2.0), y + 40); // Vẽ dưới chữ PAUSED
    }
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;

        primaryStage.setTitle("Bomberman Game");
        primaryStage.setResizable(false);
        try {
            // Đặt đúng đường dẫn file font của bạn
            InputStream fontStream = getClass().getResourceAsStream("/Font/PressStart2P-Regular.ttf");
            if (fontStream != null) {
                // Gán font đã load cho biến thành viên
                this.uiFont = Font.loadFont(fontStream, 16); // Cỡ chữ 16
                fontStream.close();
                if (this.uiFont == null) { // Kiểm tra nếu loadFont thất bại
                    System.err.println("Font.loadFont returned null. Using default Arial.");
                    this.uiFont = Font.font("Arial", 16);
                } else {
                    System.out.println("Custom font loaded: " + this.uiFont.getName());
                }
            } else {
                System.err.println("Could not find font resource stream. Using default Arial.");
                this.uiFont = Font.font("Arial", 16); // Fallback quan trọng
            }
        } catch (Exception e) {
            System.err.println("Error loading custom font: " + e.getMessage());
            e.printStackTrace();
            this.uiFont = Font.font("Arial", 16); // Fallback quan trọng
        }

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
        inputHandler = new InputHandler(scene, player,this);

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
                // --- XỬ LÝ DỰA TRÊN GAME STATE ---
                switch (currentState) {
                    case PLAYING:
                        // Chỉ update logic game khi đang chơi
                        updateGame(deltaTime);
                        // Luôn vẽ màn hình game
                        renderGame();
                        break;

                    case PAUSED:

                        renderGame();
                        renderPauseScreen(gc); // << GỌI HÀM VẼ PAUSE MỚI
                        break;

                    case MENU:
                        // TODO: Xử lý update và render cho Menu sau
                         renderMenu(gc);
                        break;

                    case GAME_OVER:
                       renderGameOverScreen(gc);
                        break;

                    // Thêm các case khác nếu cần (LEVEL_CLEARED, GAME_WON)
                    default:
                        // Trạng thái không xác định, có thể vẽ màn hình game mặc định
                        renderGame();
                        break;
                }
            }
        };

        timer.start();
    }
    private void renderGame(){
        if (gc == null) return;
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // --- 2. Vẽ Nền Thanh UI ---
        if (UI_PANEL_HEIGHT > 0) {
            gc.setFill(Color.rgb(40, 40, 40)); // Màu xám tối (hoặc màu bạn chọn)
            gc.fillRect(0, 0, canvas.getWidth(), UI_PANEL_HEIGHT); // Vẽ ở trên cùng
        }


        // Vẽ bản đồ nền (chỉ nếu gameMap không null)
        if (gameMap != null) {
            gameMap.render(gc); // Gọi render của com.example.bomberman.Map.Map
        } else {
            System.err.println("Warning: gameMap is null during render.");
        }
        renderBombs(gc);
        renderItems(gc);
        renderTemporaryAnimations(gc);


        // TODO: Vẽ các thực thể khác (Enemies)
        // renderEnemies(gc);

        if (player != null) {
            player.render(gc);
        } else {
            System.err.println("Warning: player is null during render.");
        }
        renderFlames(gc);
        renderUI(gc);
    }
    private void updateGame(double deltaTime){
        if (levelTimeRemaining > 0) { // Chỉ đếm ngược nếu thời gian còn > 0
            levelTimeRemaining -= deltaTime;

        }
        // Trong Bomberman.updateGame()
        if (levelTimeRemaining <= 0 && currentState == GameState.PLAYING) { // Thêm kiểm tra currentState
            System.out.println("TIME'S UP! -> GAME OVER (Temporary)");
            currentState = GameState.GAME_OVER; // << CHUYỂN STATE
            // Không cần gọi player.die() nữa nếu state đã chuyển
        }
        if (player != null) {
            player.update(deltaTime);
        }
        updateBombs(deltaTime);
        updateFlames(deltaTime);
        updateItems(deltaTime);
        updateTemporaryAnimations(deltaTime);
        if (!portalActivated) {
            //sửa lại điều kiện portal ở đây;
            boolean activationConditionMet = true; // Điều kiện test
            if (activationConditionMet) {
                portalActivated = true;
                System.out.println("Portal Activated!");
            }
        }
        //--- KIểm tra Portal---
        handleKickBombTrigger();
        // Trong Bomberman.handle() hoặc handlePortalTransition()
        handleFlameBombCollisions(); // << GỌI PHƯƠNG THỨC MỚI Ở ĐÂY
        handlePlayerItemCollisions();

        // Cập nhật Bom và xử lý Bom không còn hoạt động
        handlePortalTransition();
        // --- Cập nhật Items ---
    }
private void handlePortalTransition(){
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
            currentLevel++;
            if (currentLevel <= MAX_LEVEL) {
                System.out.println("Loading next level: " + currentLevel);
                loadLevel(currentLevel);
            } else {
                System.out.println("CONGRATULATIONS! YOU BEAT THE GAME!");
                if (primaryStage != null) primaryStage.close();
            }
            // return;
        }
    }
}
private void handleKickBombTrigger(){
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
    // --- Phương thức để tải một màn chơi cụ thể ---
    private void loadLevel(int levelNumber) {
        try {
            System.out.println("Loading level " + levelNumber + "..."); // Log bắt đầu tải level
            this.score = 0;
            this.levelTimeRemaining = LEVEL_DURATION_SECONDS;
            this.portalActivated = false;
            this.portalGridX = -1;
            this.portalGridY = -1;
            System.out.println("Score, Timer, Portal state reset.");
            System.out.println("Level timer reset to " + LEVEL_DURATION_SECONDS + " seconds.");
            // Xóa các thực thể và animation từ màn chơi trước
            bombs.clear();
            flames.clear();
            enemies.clear();
            items.clear(); // Xóa items cũ
            temporaryAnimations.clear(); // Xóa animation cũ khi load màn mới
            player = null; // Đặt player về null trước khi tạo mới


            // --- 1. Tải MapData và Khởi tạo Map ---
            MapData mapData = new MapData(levelNumber);
            gameMap = new com.example.bomberman.Map.Map(mapData, this); // Sử dụng tên đầy đủ package

            // --- 2. Lấy thông tin Item được giấu từ MapData ---
            // hiddenItemsData là java.util.Map
            hiddenItemsData = mapData.getHiddenItems();
            System.out.println("Loaded " + hiddenItemsData.size() + " hidden items from map data."); // Log


            // 3. Cập nhật kích thước canvas
            int canvasWidth = mapData.getCols() * Sprite.SCALED_SIZE;
            int canvasHeight = mapData.getRows() * Sprite.SCALED_SIZE + UI_PANEL_HEIGHT;
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
                        case '#': break;
                        case '*': break;
                        case 'x': // Portal
                            if (portalGridX == -1) {
                                portalGridX = j;
                                portalGridY = i;
                                System.out.println("Portal found and position stored at (" + j + ", " + i + ")");
                            } else {
                                // Đã bỏ warning spam ở đây
                            }
                            break;
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
                    inputHandler = new InputHandler(primaryStage.getScene(), player,this);
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
    public boolean isBombAtGrid(int gridX, int gridY) {
         for (Bomb bomb : bombs) {
             if (bomb.isActive() && bomb.getGridX() == gridX && bomb.getGridY() == gridY) {
                 return true;
             }
         }
         return false;
     }

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
        Iterator<Item> iterator = items.iterator();

        while (iterator.hasNext()) {
            Item item = iterator.next();

            item.update(deltaTime);


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
    public List<Item> getItems() {
        return this.items; // 'items' là thuộc tính List<Item> bạn đã khai báo ở đầu lớp Bomberman
    }

    public boolean isPortalActivated() {
        return portalActivated;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
