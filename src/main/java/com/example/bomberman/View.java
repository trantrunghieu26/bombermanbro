package com.example.bomberman;

import com.example.bomberman.Map.Tile;
import com.example.bomberman.entities.Bomb;
import com.example.bomberman.entities.Enemy;
import com.example.bomberman.entities.Flame;
import com.example.bomberman.entities.Items.Item;
import com.example.bomberman.entities.TemporaryAnimation;
import com.example.bomberman.graphics.Sprite;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class View {

    // attributes//////////////////

    private Image animation0;
    private Image animation1;
    private Image animation2;
    private Image[] imagesA;
    private Random random = new Random();
    private final List<TemporaryAnimation> temporaryAnimations = new ArrayList<>();

    // Method

    public View() {
        loadAnimation();
        if (animation0 != null && animation1 != null && animation2 != null) {
            imagesA = new Image[]{animation0, animation1, animation2};
        } else {
            System.out.println("Those images can't be loaded!");
        }
    }

    public void renderForAllPLAYING(Controller gameManager) {
        if (gameManager.gc == null) {return;}
        gameManager.gc.clearRect(0, 0, gameManager.canvas.getWidth(), gameManager.canvas.getHeight());

        // --- 2. Vẽ Nền Thanh UI ---
        if (Controller.UI_PANEL_HEIGHT > 0) {
            gameManager.gc.setFill(Color.rgb(40, 40, 40)); // Màu xám tối (hoặc màu bạn chọn)
            gameManager.gc.fillRect(0, 0, gameManager.canvas.getWidth(), Controller.UI_PANEL_HEIGHT); // Vẽ ở trên cùng
        }

        if (gameManager.getMap() == null) return;

        gameManager.getMap().render(gameManager);

        for (Enemy enemy : gameManager.getEnemies()) {
            enemy.render(gameManager.gc);
        }

        // Render Bombs
        List<List<Bomb>> listOfBombLists = gameManager.getBto();
        for (List<Bomb> bombList : listOfBombLists) {
            for (Bomb bomb : bombList) {
                bomb.render(gameManager.gc);
            }
        }

        for (Item item : gameManager.getItems()) {
            item.render(gameManager.gc);
        }

        renderTemporaryAnimations(gameManager.gc);

        // Render Player
        if (!gameManager.getPlayer().isRemoved()) {
            gameManager.getPlayer().render(gameManager.gc);
        }

        // Render Flames
        for (Flame flame : gameManager.getFlames()) {
            flame.render(gameManager.gc);
        }

        renderUI(gameManager);
    }

    // --- Phương thức vẽ tất cả TemporaryAnimation ---
    public void renderTemporaryAnimations(GraphicsContext gc) {
        for (TemporaryAnimation anim : temporaryAnimations) {
            anim.render(gc);
        }
    }


    // --- Phương thức cập nhật tất cả TemporaryAnimation (ví dụ: animation gạch vỡ) ---
    public void updateTemporaryAnimations(double deltaTime, Controller con) {
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
                if (con.getMap() != null) {
                    con.getMap().setTile(gridX, gridY, Tile.createTileFromChar(gridX, gridY, ' ')); // ' ' là Grass/EMPTY
                    System.out.println("Tile at (" + gridX + ", " + gridY + ") changed to EMPTY."); // Log khi thay đổi Tile
                } else {
                    System.err.println("Error: gameMap is null when finishing brick animation!");
                }

                // --- 2. Xác định xem có Item nào nên được tạo ra tại vị trí này không ---
                char itemToSpawnChar = '\0'; // Sử dụng ký tự '\0' để đánh dấu rằng chưa có Item nào được xác định cần tạo
                String itemKey = gridX + "," + gridY;


                if (con.getHiddenItemsData() != null && con.getHiddenItemsData().containsKey(itemKey)) {
                    itemToSpawnChar = con.getHiddenItemsData().get(itemKey);
                    // Xóa Item đã được tạo khỏi danh sách hiddenItemsData để không tạo lại lần nữa
                    con.getHiddenItemsData().remove(itemKey);
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
                    con.spawnItemAt(gridX, gridY, itemToSpawnChar);
                }
                iterator.remove(); // Xóa an toàn TemporaryAnimation đã kết thúc
                // System.out.println("Temporary animation removed from list at (" + gridX + ", " + gridY + ")."); // Log (có thể quá nhiều)
            }
        }
    }


    public void renderUI(Controller gameManager) {
        if (gameManager.gc == null || gameManager.canvas == null) return;

        // --- Lấy dữ liệu cần hiển thị ---
        int currentLives = (gameManager.getPlayer() != null && gameManager.getPlayer().isAlive()) ? gameManager.getPlayer().getLives() : 0;
        int currentScore = gameManager.getScore();
        int currentLevel = (gameManager.getMap() != null) ? gameManager.getMap().getLevel() : 0;
        int totalSecondsRemaining = (int) Math.ceil(gameManager.getLevelTimeRemaining());

        // --- Lấy kích thước Canvas ---
        double canvasWidth = gameManager.canvas.getWidth();
        double canvasHeight = gameManager.canvas.getHeight(); // Không dùng nhưng có thể cần sau này

        // --- Định dạng chữ ---
        gameManager.gc.setFill(Color.WHITE); // Màu chữ
        Font uiFont = Font.font("Arial", 20); // Chọn Font
        gameManager.gc.setFont(uiFont);

        // --- Tính toán vị trí ---
        double padding = 10; // Khoảng cách lề
        double textBaselineOffsetY = 5;
        double yPositionText = Controller.UI_PANEL_HEIGHT / 2.0 + textBaselineOffsetY;

        double baselineOffset = 5; // Khoảng cách từ đỉnh chữ xuống baseline (ước lượng)



        // 1. Level (Góc trái)
        String levelText = "LEVEL: " + currentLevel; //+ " (" + mapRows + "x" + mapCols + ")"; // Bỏ kích thước map cho gọn

        gameManager.gc.fillText(levelText, padding, yPositionText);

        // 2. Score (Giữa màn hình)
        String scoreText = "SCORE: " + currentScore;

        Text scoreTextNode = new Text(scoreText); // Dùng Text để đo chính xác
        scoreTextNode.setFont(uiFont);
        double scoreTextWidth = scoreTextNode.getLayoutBounds().getWidth();
        double xPositionScore = (canvasWidth / 2.0) - (scoreTextWidth / 2.0);
        gameManager.gc.fillText(scoreText, xPositionScore, yPositionText);
        // 3. Lives (Góc phải)

        double iconDrawWidth = Sprite.SCALED_SIZE-4; // Kích thước vẽ icon mong muốn
        double iconDrawHeight = Sprite.SCALED_SIZE-8;
        double yPositionIcon = (Controller.UI_PANEL_HEIGHT - iconDrawHeight) / 2.0;

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
        gameManager.gc.fillText(livesNumText, xPositionLivesText, yPositionText );

        // Vẽ Icon ngay bên trái số mạng
        if (lifeIcon != null) {
            double xPositionIcon = xPositionLivesText - iconDrawWidth - 5; // Cách text 5px
            gameManager.gc.drawImage(lifeIcon, xPositionIcon, yPositionIcon, iconDrawWidth, iconDrawHeight);
        }


        //Time

        String timeText = "TIME: " + totalSecondsRemaining;
        double xPositionTime = padding + 150; // Điều chỉnh khoảng cách 150 nếu cần
        gameManager.gc.fillText(timeText, xPositionTime, yPositionText);

    }


    public void renderPauseScreen(Controller gameManager, Font uiFont) {
        if (gameManager.gc == null || gameManager.canvas == null) return;

        // --- Vẽ lớp phủ mờ --- (Tùy chọn)
        gameManager.gc.setFill(Color.rgb(0, 0, 0, 0.5)); // Màu đen với độ trong suốt 50%
        gameManager.gc.fillRect(0, 0, gameManager.canvas.getWidth(), gameManager.canvas.getHeight());

        // --- Vẽ chữ "PAUSED" ---
        String pauseText = "PAUSED";
        gameManager.gc.setFill(Color.YELLOW); // Màu chữ Pause
        // Sử dụng font đã load hoặc font lớn hơn
        Font pauseFont = Font.font(uiFont.getFamily(), 48); // Lấy family từ font UI, đặt cỡ 48
        gameManager.gc.setFont(pauseFont);

        // Căn chữ "PAUSED" ra giữa màn hình
        Text textNode = new Text(pauseText);
        textNode.setFont(pauseFont);
        double textWidth = textNode.getLayoutBounds().getWidth();
        double textHeight = textNode.getLayoutBounds().getHeight(); // Cần để căn giữa Y nếu muốn
        double x = (gameManager.canvas.getWidth() / 2.0) - (textWidth / 2.0);
        double y = (gameManager.canvas.getHeight() / 2.0) + (textHeight / 4.0); // Căn chỉnh baseline Y

        gameManager.gc.fillText(pauseText, x, y);

        // Có thể vẽ thêm hướng dẫn "Press ESC to Resume"
        gameManager.gc.setFont(uiFont); // Quay lại font nhỏ hơn
        gameManager.gc.setFill(Color.WHITE);
        String resumeText = "Press ESC to Resume";
        Text resumeTextNode = new Text(resumeText);
        resumeTextNode.setFont(uiFont);
        double resumeTextWidth = resumeTextNode.getLayoutBounds().getWidth();
        gameManager.gc.fillText(resumeText, (gameManager.canvas.getWidth() / 2.0) - (resumeTextWidth / 2.0), y + 40); // Vẽ dưới chữ PAUSED
    }

    public void renderGameOverScreen(Controller gameManager, Font uiFont) {
        if (gameManager.gc == null || gameManager.canvas == null) return;

        // --- Vẽ nền (ví dụ: đỏ sẫm hoặc đen) ---
        gameManager.gc.setFill(Color.DARKRED);
        gameManager.gc.fillRect(0, 0, gameManager.canvas.getWidth(), gameManager.canvas.getHeight());

        // --- Vẽ chữ "GAME OVER" ---
        String gameOverText = "GAME OVER";
        gameManager.gc.setFill(Color.WHITE);
        // Font lớn
        Font gameOverFont = (uiFont != null) ? Font.font(uiFont.getFamily(), 60) : Font.font("Arial", 60);
        gameManager.gc.setFont(gameOverFont);
        // Căn giữa
        Text goNode = new Text(gameOverText);
        goNode.setFont(gameOverFont);
        double goWidth = goNode.getLayoutBounds().getWidth();
        gameManager.gc.fillText(gameOverText, (gameManager.canvas.getWidth() / 2.0) - (goWidth / 2.0), 150);

        // --- Vẽ Điểm số ---
        String scoreText = "Final Score: " + gameManager.getScore(); // Lấy điểm cuối cùng
        gameManager.gc.setFill(Color.YELLOW);
        // Font nhỏ hơn
        Font scoreFont = (uiFont != null) ? Font.font(uiFont.getFamily(), 24) : Font.font("Arial", 24);
        gameManager.gc.setFont(scoreFont);
        // Căn giữa
        Text scoreNode = new Text(scoreText);
        scoreNode.setFont(scoreFont);
        double scoreWidth = scoreNode.getLayoutBounds().getWidth();
        gameManager.gc.fillText(scoreText, (gameManager.canvas.getWidth() / 2.0) - (scoreWidth / 2.0), 250);

        // --- Vẽ tùy chọn "Restart" / "Exit" ---
        String restartText = "Press ENTER to Restart";
        String exitText = "Press ESC to Exit";
        String returnMenu = "Press BACK_SPACE to Return Menu";
        gameManager.gc.setFill(Color.WHITE);
        gameManager.gc.setFont(scoreFont); // Dùng lại font điểm số

        // Căn giữa Restart
        Text restartNode = new Text(restartText);
        restartNode.setFont(scoreFont);
        double restartWidth = restartNode.getLayoutBounds().getWidth();
        gameManager.gc.fillText(restartText, (gameManager.canvas.getWidth() / 2.0) - (restartWidth / 2.0), 350);

        // Căn giữa Exit
        Text exitNode = new Text(exitText);
        exitNode.setFont(scoreFont);
        double exitWidth = exitNode.getLayoutBounds().getWidth();
        gameManager.gc.fillText(exitText, (gameManager.canvas.getWidth() / 2.0) - (exitWidth / 2.0), 400); // Cách Restart một chút

        Text returnMenuNode = new Text(returnMenu);
        returnMenuNode.setFont(scoreFont);
        double returnMenuWidth = returnMenuNode.getLayoutBounds().getWidth();
        gameManager.gc.fillText(returnMenu, (gameManager.canvas.getWidth() / 2.0) - (returnMenuWidth / 2.0), 450);
    }

    public void renderMenu(Controller gameManager, Image menuBackground, Font uiFont, Image handCursorImage) {
        if (gameManager.gc == null || gameManager.canvas == null) return;

        // --- Vẽ nền (ví dụ: đen) ---
        if (menuBackground != null) {
            gameManager.gc.drawImage(menuBackground, 0, 0, gameManager.canvas.getWidth(), gameManager.canvas.getHeight());
        } else {
            gameManager.gc.setFill(Color.BLACK);
            gameManager.gc.fillRect(0, 0, gameManager.canvas.getWidth(), gameManager.canvas.getHeight());
        }
        // --- Vẽ Tiêu đề Game ---
        gameManager.gc.setFill(Color.WHITE); // Đổi màu nếu cần
        Font titleFont = Font.font(uiFont.getFamily(), 60); // Lấy font đã load
        gameManager.gc.setFont(titleFont);
        String title = "BOMBERMAN";
        Text titleNode = new Text(title); titleNode.setFont(titleFont);
        double titleWidth = titleNode.getLayoutBounds().getWidth();
        gameManager.gc.fillText(title, (gameManager.canvas.getWidth() / 2.0) - (titleWidth / 2.0), 150); // Căn giữa

        // 3. Vẽ các lựa chọn
        gameManager.gc.setFont(uiFont); // Đặt lại font cho lựa chọn
        double startY = 300;
        double spacing = 40;

        for (int i = 0; i < gameManager.menuOptions.length; i++) {
            // Cập nhật text Music ON/OFF
            if (gameManager.menuOptions[i].startsWith("Music:")) {
                gameManager.menuOptions[i] = "Music: " + (gameManager.isMusicOn ? "ON" : "OFF");
            }

            String optionText = gameManager.menuOptions[i];
            // Đo chiều rộng text để căn giữa hoặc tính vị trí con trỏ
            Text textNode = new Text(optionText); textNode.setFont(uiFont);
            double textWidth = textNode.getLayoutBounds().getWidth();
            double textHeight = textNode.getLayoutBounds().getHeight(); // Lấy chiều cao để căn con trỏ Y

            double xPosition = (gameManager.canvas.getWidth() / 2.0) - (textWidth / 2.0); // Căn giữa
            double yPosition = startY + i * spacing;

            // Highlight
            if (i == gameManager.selectedOptionIndex) {
                gameManager.gc.setFill(Color.YELLOW); // Màu khi được chọn
            } else {
                gameManager.gc.setFill(Color.WHITE); // Màu bình thường
            }
            gameManager.gc.fillText(optionText, xPosition, yPosition);

            // 4. Vẽ con trỏ nếu đây là lựa chọn được chọn và ảnh con trỏ đã load
            if (i == gameManager.selectedOptionIndex && handCursorImage != null) {
                // Scale con trỏ lên nếu muốn (ví dụ: gấp đôi kích thước gốc 16x16 -> 32x32)
                double cursorDrawWidth = handCursorImage.getWidth() * 2; // Ví dụ scale x2
                double cursorDrawHeight = handCursorImage.getHeight() * 2; // Ví dụ scale x2

                // Tính toán vị trí X: Bên trái của chữ
                double cursorX = xPosition - cursorDrawWidth - 10; // Cách chữ 10px

                // Tính toán vị trí Y: Căn giữa theo chiều cao của chữ
                // Baseline của chữ ở yPosition, cần dịch lên một nửa chiều cao chữ rồi trừ nửa chiều cao con trỏ
                double cursorY = yPosition - textHeight * 0.5 - cursorDrawHeight * 0.5; // Căn giữa Y (ước lượng)

                // Vẽ con trỏ với kích thước đã scale
                gameManager.gc.drawImage(handCursorImage, cursorX, cursorY, cursorDrawWidth, cursorDrawHeight);
            }
        }
    }

    public void renderWinState(Controller gameManager, Image wingame, Font uiFont) {
        if (gameManager.gc == null || gameManager.canvas == null) return;

        // --- Vẽ nền (ví dụ: đỏ sẫm hoặc đen) ---
        if (wingame != null) {
            gameManager.gc.drawImage(wingame, 0, 0, gameManager.canvas.getWidth(), gameManager.canvas.getHeight());
        } else {
            gameManager.gc.setFill(Color.BLACK);
            gameManager.gc.fillRect(0, 0, gameManager.canvas.getWidth(), gameManager.canvas.getHeight());
        }

        Font scoreFont = (uiFont != null) ? Font.font(uiFont.getFamily(), 24) : Font.font("Arial", 24);

        // --- Vẽ tùy chọn "Restart" / "Exit" ---
        String restartText = "Press ENTER to Restart";
        String exitText = "Press ESC to Exit";
        String returnMenu = "Press BACK_SPACE to Return Menu";
        gameManager.gc.setFill(Color.BLUE);
        gameManager.gc.setFont(scoreFont); // Dùng lại font điểm số

        // Căn giữa Restart
        Text restartNode = new Text(restartText);
        restartNode.setFont(scoreFont);
        double restartWidth = restartNode.getLayoutBounds().getWidth();
        gameManager.gc.fillText(restartText, (gameManager.canvas.getWidth() / 2.0) - (restartWidth / 2.0), 380);

        // Căn giữa Exit
        Text exitNode = new Text(exitText);
        exitNode.setFont(scoreFont);
        double exitWidth = exitNode.getLayoutBounds().getWidth();
        gameManager.gc.fillText(exitText, (gameManager.canvas.getWidth() / 2.0) - (exitWidth / 2.0), 430); // Cách Restart một chút

        Text returnMenuNode = new Text(returnMenu);
        returnMenuNode.setFont(scoreFont);
        double returnMenuWidth = returnMenuNode.getLayoutBounds().getWidth();
        gameManager.gc.fillText(returnMenu, (gameManager.canvas.getWidth() / 2.0) - (returnMenuWidth / 2.0), 100);
    }

    public List<TemporaryAnimation> getTemporaryAnimations() { return this.temporaryAnimations; }

    public void loadAnimation() {
        try {
            InputStream ani0 = getClass().getResourceAsStream("/textures/Animation0.png");
            if (ani0 != null) {
                animation0 = new Image(ani0); // Load ảnh gốc
                ani0.close();
                System.out.println("Loading Animation 0...");
            } else {
                System.err.println("Could not find Animation 0.");
            }
        } catch (IOException e) {
            System.err.println("Error loading Animation 0: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            InputStream ani1 = getClass().getResourceAsStream("/textures/Animation1.png");
            if (ani1 != null) {
                animation1 = new Image(ani1); // Load ảnh gốc
                ani1.close();
                System.out.println("Loading Animation 1...");
            } else {
                System.err.println("Could not find Animation 1.");
            }
        } catch (IOException e) {
            System.err.println("Error loading Animation 1: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            InputStream ani2 = getClass().getResourceAsStream("/textures/Animation2.png");
            if (ani2 != null) {
                animation2 = new Image(ani2); // Load ảnh gốc
                ani2.close();
                System.out.println("Loading Animation 2...");
            } else {
                System.err.println("Could not find Animation 2.");
            }
        } catch (IOException e) {
            System.err.println("Error loading Animation 2: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void renderSetting(Controller gameManager, Font uiFont) {
        if (gameManager.gc == null || gameManager.canvas == null) return;

        gameManager.gc.clearRect(0, 0, gameManager.canvas.getWidth(), gameManager.canvas.getHeight());
        gameManager.gc.setFill(Color.BLACK);
        gameManager.gc.fillRect(0, 0, gameManager.canvas.getWidth(), gameManager.canvas.getHeight());

        // --- Vẽ Tiêu đề Game ---
        gameManager.gc.setFill(Color.BLUE); // Đổi màu nếu cần
        Font titleFont = Font.font(uiFont.getFamily(), 60); // Lấy font đã load
        gameManager.gc.setFont(titleFont);
        String title = "SETTING";
        Text titleNode = new Text(title);
        titleNode.setFont(titleFont);
        double titleWidth = titleNode.getLayoutBounds().getWidth();
        gameManager.gc.fillText(title, (gameManager.canvas.getWidth() / 2.0) - (titleWidth / 2.0), 80);

        // 3. Vẽ các lựa chọn
        gameManager.gc.setFont(uiFont); // Đặt lại font cho lựa chọn

        double poX = gameManager.canvas.getWidth() / 20.0;
        double poY = titleNode.getLayoutBounds().getHeight() + 100;

        double imageWidth = animation0.getWidth();
        double imageHeight = animation0.getHeight();

        double startY = 200;
        double spacing = 50;
        String sample = "Animation 0: is selected";
        Text theText = new Text(sample);

        for (int i = 0; i < gameManager.Animations.length; i++) {
            if (gameManager.isAnimations[i]) {
                gameManager.Animations[i] = "Animation " + i + ": is selected";
            } else {
                gameManager.Animations[i] = "Animation " + i;
            }

            String optionText = gameManager.Animations[i];
            Text textNode = new Text(optionText);
            textNode.setFont(uiFont);
            double sampleWidth = theText.getLayoutBounds().getWidth();

            double midConstant = (gameManager.canvas.getWidth() / 2.0) - (sampleWidth / 2.0);
            double xPosition = midConstant + (gameManager.canvas.getWidth() / 6.5);
            double yPosition = startY + i * spacing;

            // Highlight
            if (i == gameManager.selectSetting) {
                gameManager.gc.setFill(Color.YELLOW); // Màu khi được chọn
            } else {
                gameManager.gc.setFill(Color.BLUE); // Màu bình thường
            }
            gameManager.gc.fillText(optionText, xPosition, yPosition);
        }

        gameManager.gc.drawImage(imagesA[gameManager.selectSetting], poX, poY, imageWidth / 3, imageHeight / 3);
    }
}
