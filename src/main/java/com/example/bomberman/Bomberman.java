package com.example.bomberman;

import com.example.bomberman.Input.InputHandler;
import com.example.bomberman.entities.Direction;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer; // Import AnimationTimer

import java.io.IOException;
import java.io.InputStream;


public class Bomberman extends Application {

    public GameState currentState = GameState.MENU;

    private Stage primaryStage;
    public static Font uiFont;
    private Image menuBackground;
    private Image wingame;
    private Image handCursorImage; // Thêm biến này nếu load con trỏ riêng
    public static final int UI_PANEL_HEIGHT = 32;

    public Controller con;
    public View view;
    public InputHandler inputHandler;

    public int currentLevel = 1;

    public Bomberman myGame = this;


    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        
        // Cài đặt window
        primaryStage.setTitle("Bomberman Game");
        primaryStage.setResizable(false); // Không cho resize để tránh lỗi canvas
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

        try {
            InputStream bgStream = getClass().getResourceAsStream("/textures/nền.png"); // Sửa đường dẫn nếu cần
            if (bgStream != null) {
                menuBackground = new Image(bgStream);
                bgStream.close();
                System.out.println("Menu background loaded.");
            } else {
                System.err.println("Could not find menu background resource stream.");
            }
        } catch (Exception e) {
            System.err.println("Error loading menu background: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            InputStream wgStream = getClass().getResourceAsStream("/textures/wingame.png");
            if (wgStream != null) {
                wingame = new Image(wgStream);
                wgStream.close();
                System.out.println("What is the next?");
            } else {
                System.err.println("Well, everything don't follow you!");
            }
        } catch (Exception e) {
            System.err.println("Error: I don't Know what happened!" + e.getMessage());
            e.printStackTrace();
        }

        try {
            InputStream hcStream = getClass().getResourceAsStream("/textures/contro2.png"); // Đặt tên file đúng
            if (hcStream != null) {
                handCursorImage = new Image(hcStream); // Load ảnh gốc
                hcStream.close();
                System.out.println("Hand cursor loaded.");
            } else {
                System.err.println("Could not find hand cursor resource stream.");
            }
        } catch (IOException e) {
            System.err.println("Error loading hand cursor: " + e.getMessage());
            e.printStackTrace();
        }

        this.con = new Controller();
        con.controllerActive(myGame);

        // --- 4. Bắt đầu Vòng lặp Game (AnimationTimer) ---
        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdateTime = 0; // Thời điểm của lần cập nhật trước (nanoseconds)

            @Override
            public void handle(long now) {
                if (lastUpdateTime == 0) {
                    lastUpdateTime = now;
                    return; // Bỏ qua frame đầu tiên để tránh deltaTime lớn
                }

                // Tính toán thời gian trôi qua giữa các frame (đơn vị: giây)
                double deltaTime = (now - lastUpdateTime) / 1_000_000_000.0;
                lastUpdateTime = now;

                // --- XỬ LÝ DỰA TRÊN GAME STATE ---
                switch (currentState) {
                    case PLAYING:
                        // Chỉ update logic game khi đang chơi
                        myGame.con.updateForAll(deltaTime, myGame);
                        myGame.view.renderForAllPLAYING(myGame.con);
                        if (myGame.con.getLevelTimeRemaining() <= 0 && myGame.currentState == GameState.PLAYING) {
                            System.out.println("TIME'S UP! -> GAME OVER (Temporary)");
                            myGame.currentState = GameState.GAME_OVER; // << CHUYỂN STATE
                            // Không cần gọi player.die() nữa nếu state đã chuyển
                        }
                        break;

                    case PAUSED:
                        view.renderForAllPLAYING(con);
                        view.renderPauseScreen(con, uiFont); // << GỌI HÀM VẼ PAUSE MỚI
                        break;

                    case MENU:
                        view.renderMenu(con, menuBackground, uiFont, handCursorImage);
                        break;

                    case GAME_OVER:
                        view.renderGameOverScreen(con, uiFont);
                        break;

                    case GAME_WON:
                        view.renderWinState(con, wingame, uiFont);
                        break;

                    case SETTING:
                        view.renderSetting(con, uiFont);
                        break;

                    default:
                        // Trạng thái không xác định, có thể vẽ màn hình game mặc định
                        view.renderForAllPLAYING(con);
                        break;
                }
            }
        };

        timer.start(); // Bắt đầu vòng lặp game
    }

    public GameState getCurrentState() { return this.currentState; }

    public Stage getPrimaryStage() { return this.primaryStage; }

    public void togglePause() {
        if (currentState == GameState.PLAYING) {
            currentState = GameState.PAUSED;
            System.out.println("Game Paused"); if (inputHandler != null) {
                inputHandler.clearMovingKeys(); // Gọi hàm mới trong InputHandler
            }
            if (con.getPlayer() != null) {
                con.getPlayer().setMovingDirection(Direction.NONE); // Bảo Player dừng lại ngay
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

    public void navigateMenuUp() {
        if (currentState == GameState.MENU) {
            con.selectedOptionIndex--;
            if (con.selectedOptionIndex < 0) {
                con.selectedOptionIndex = con.menuOptions.length - 1;
            }
            // TODO: Play menu navigation sound
        } else if (currentState == GameState.SETTING) {
            con.selectSetting--;
            if (con.selectSetting < 0) {
                con.selectSetting = con.Animations.length - 1;
            }
            // TODO: Play Setting navigation sound
        }
    }

    public void navigateMenuDown() {
        if (currentState == GameState.MENU) {
            con.selectedOptionIndex++;
            if (con.selectedOptionIndex >= con.menuOptions.length) {
                con.selectedOptionIndex = 0;
            }
            // TODO: Play menu navigation sound
        } else if (currentState == GameState.SETTING) {
            con.selectSetting++;
            if (con.selectSetting >= con.Animations.length) {
                con.selectSetting = 0;
            }
            // TODO: Play Setting navigation sound
        }
    }

    public void selectForOption() {
        if (currentState == GameState.MENU) {
            // TODO: Play menu selection sound
            switch (con.selectedOptionIndex) {
                case 0: // Start Game
                    con.startGame(myGame);
                    break;
                case 1: // Settings
                    con.chooseSetting(myGame);
                    break;
                case 2: // Music
                    con.toggleMusic();
                    break;
            }
        } else if (currentState == GameState.SETTING) {
            // TODO: Play menu selection sound
            switch (con.selectSetting) {
                case 0:
                    con.chooseAnimation0(myGame);
                    break;
                case 1:
                    con.chooseAnimation1(myGame);
                    break;
                case 2:
                    con.chooseAnimation2(myGame);
                    break;
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
