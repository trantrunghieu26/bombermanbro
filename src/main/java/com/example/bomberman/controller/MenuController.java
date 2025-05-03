package com.example.bomberman.controller;

import com.example.bomberman.Bomberman;
import com.example.bomberman.GameState;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.image.Image;

public class MenuController implements SceneController {

    private Bomberman bomberman; // Tham chiếu đến lớp Bomberman chính
    // Các biến trạng thái riêng của Menu có thể chuyển vào đây
    private String[] menuOptions = {"Start Game", "Settings", "Music: ON"};
    private int selectedOptionIndex = 0;
    private boolean isMusicOn = true; // Trạng thái nhạc ban đầu

    public MenuController(Bomberman bomberman) {
        this.bomberman = bomberman;
        // Có thể load tài nguyên riêng cho Menu ở đây nếu cần
    }

    @Override
    public void update(double deltaTime) {
        // Menu thường không cần logic update phức tạp
    }

    @Override
    public void render(GraphicsContext gc) {
        // --- Lấy tài nguyên từ Bomberman ---
        Image menuBackground = bomberman.getMenuBackground(); // Cần getter trong Bomberman
        Image handCursorImage = bomberman.getHandCursorImage(); // Cần getter trong Bomberman
        Font uiFont = bomberman.getUiFont(); // Cần getter trong Bomberman
        double canvasWidth = bomberman.getCanvasWidth(); // Cần getter trong Bomberman
        double canvasHeight = bomberman.getCanvasHeight(); // Cần getter trong Bomberman

        if (gc == null || uiFont == null) return;

        // --- Vẽ nền ---
        if (menuBackground != null) {
            gc.drawImage(menuBackground, 0, 0, canvasWidth, canvasHeight);
        } else {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvasWidth, canvasHeight);
        }

        // --- Vẽ Tiêu đề Game ---
        gc.setFill(Color.WHITE);
        Font titleFont = Font.font(uiFont.getFamily(), 60);
        gc.setFont(titleFont);
        String title = "BOMBERMAN";
        Text titleNode = new Text(title); titleNode.setFont(titleFont);
        double titleWidth = titleNode.getLayoutBounds().getWidth();
        gc.fillText(title, (canvasWidth / 2.0) - (titleWidth / 2.0), 150);

        // --- Vẽ các lựa chọn ---
        gc.setFont(uiFont);
        double startY = 300;
        double spacing = 40;
        double cursorOffset = 10;
        double cursorScale = 2.0;

        for (int i = 0; i < menuOptions.length; i++) {
            if (menuOptions[i].startsWith("Music:")) {
                menuOptions[i] = "Music: " + (isMusicOn ? "ON" : "OFF");
            }
            String optionText = menuOptions[i];
            Text textNode = new Text(optionText); textNode.setFont(uiFont);
            double textWidth = textNode.getLayoutBounds().getWidth();
            double textHeight = textNode.getLayoutBounds().getHeight();
            double xPosition = (canvasWidth / 2.0) - (textWidth / 2.0);
            double yPosition = startY + i * spacing;

            if (i == selectedOptionIndex) {
                gc.setFill(Color.YELLOW);
                if (handCursorImage != null) {
                    double cursorW = handCursorImage.getWidth() * cursorScale;
                    double cursorH = handCursorImage.getHeight() * cursorScale;
                    double cursorX = xPosition - cursorW - cursorOffset;
                    double cursorY = yPosition - textHeight * 0.75; // Căn giữa Y tương đối
                    gc.drawImage(handCursorImage, cursorX, cursorY, cursorW, cursorH);
                }
            } else {
                gc.setFill(Color.WHITE);
            }
            gc.fillText(optionText, xPosition, yPosition);
        }
    }

    @Override
    public void handleInput(KeyCode code, boolean isPressed) {
        if (!isPressed) return; // Chỉ xử lý khi nhấn phím xuống cho Menu

        switch (code) {
            case UP:
            case W:
                navigateMenuUp();
                break;
            case DOWN:
            case S:
                navigateMenuDown();
                break;
            case ENTER:
                selectMenuOption();
                break;
            case ESCAPE:
                // Thoát game từ Menu
                if (bomberman.getPrimaryStage() != null) {
                    bomberman.getPrimaryStage().close();
                }
                break;
            default:
                // Các phím khác không xử lý trong Menu
                break;
        }
    }

    // --- Các phương thức xử lý logic riêng của Menu ---
    private void navigateMenuUp() {
        selectedOptionIndex--;
        if (selectedOptionIndex < 0) {
            selectedOptionIndex = menuOptions.length - 1;
        }
        // TODO: Play menu navigation sound
    }

    private void navigateMenuDown() {
        selectedOptionIndex++;
        if (selectedOptionIndex >= menuOptions.length) {
            selectedOptionIndex = 0;
        }
        // TODO: Play menu navigation sound
    }

    private void selectMenuOption() {
        // TODO: Play menu selection sound
        switch (selectedOptionIndex) {
            case 0: // Start Game
                // Yêu cầu Bomberman chuyển sang trạng thái Playing
                // Việc load level sẽ do Bomberman xử lý khi nhận yêu cầu này
                bomberman.requestLoadLevelAndSwitchState(1, GameState.PLAYING); // Cần phương thức này trong Bomberman
                break;
            case 1: // Settings
                System.out.println("Settings selected (Not implemented)");
                // bomberman.switchController(GameState.SETTINGS); // Nếu có state Settings
                break;
            case 2: // Music
                toggleMusic();
                break;
        }
    }

    private void toggleMusic() {
        isMusicOn = !isMusicOn;
        System.out.println("Music toggled: " + isMusicOn);
        bomberman.setMusicOn(isMusicOn); // Cần phương thức này trong Bomberman để quản lý nhạc
        // TODO: Thêm logic bật/tắt nhạc thực tế ở đây
    }
}