package com.example.bomberman.entities.Items;

import com.example.bomberman.Bomberman;
import com.example.bomberman.entities.Player;
import com.example.bomberman.graphics.Sprite;
import com.example.bomberman.graphics.Animation; // Import lớp Animation
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

// Lớp đại diện cho vật phẩm Life Up (tăng số mạng)
public class LifeItem extends Item {

    // --- Thuộc tính Animation cho LifeItem ---
    private Animation animation;
    private double animationTimer = 0; // Bộ đếm thời gian cho animation của Item

    // Constructor
    public LifeItem(int startGridX, int startGridY) {
        // Gọi constructor của lớp cha (Item), truyền vị trí và sprite ban đầu
        // Sử dụng Sprite.powerupLives cho LifeItem
        super(startGridX, startGridY, Sprite.powerupLife); // Vẫn truyền sprite ban đầu
        System.out.println("LifeItem created at (" + gridX + ", " + gridY + ")"); // Log để kiểm tra

        // --- Khởi tạo animation cho LifeItem ---
        double frameDuration = 0.2; // Thời gian hiển thị mỗi frame
        boolean loop = true; // Animation lặp lại

        // Sử dụng các Sprite cho animation của LifeItem
        // Giả sử có Sprite.powerupLives và Sprite.powerupLives_alt
        animation = new Animation(frameDuration, loop, Sprite.powerupLife, Sprite.powerupLife); // Ví dụ: lặp lại cùng 1 sprite
        // Nếu có sprite nhấp nháy khác:
        // animation = new Animation(frameDuration, loop, Sprite.powerupLives, Sprite.powerupLives_alt);
    }

    // --- Override phương thức update để xử lý animation và các logic khác ---
    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);
        // Chỉ cập nhật timer nếu vật phẩm còn active
        if (isActive()) {
            animationTimer += deltaTime;
            // TODO: Nếu Item có thời gian tồn tại giới hạn, kiểm tra ở đây
        }
        // TODO: Thêm các logic update đặc thù khác cho LifeItem nếu có
    }

    // --- Override phương thức render để vẽ frame animation hiện tại ---
    @Override
    public void render(GraphicsContext gc) {
        // Chỉ vẽ nếu vật phẩm còn active (chưa bị nhặt)
        if (isActive()) {
            Image currentImage = null;

            // Lấy frame animation hiện tại để vẽ, sử dụng animationTimer của Item
            if (animation != null) {
                Sprite currentSpriteFrame = animation.getFrame(animationTimer); // Lấy frame dựa trên timer
                if (currentSpriteFrame != null) {
                    currentImage = currentSpriteFrame.getFxImage();
                }
            }

            // Fallback: Nếu không có animation hoặc animation chưa sẵn sàng, dùng sprite mặc định từ lớp cha
            if (currentImage == null && sprite != null) {
                currentImage = sprite.getFxImage();
            }

            // Fallback cuối cùng nếu vẫn không có hình ảnh
            if (currentImage == null) {
                currentImage = Sprite.powerupLife.getFxImage(); // Sử dụng sprite mặc định của LifeItem
            }

            if (currentImage != null) {
                // Vẽ hình ảnh LifeItem tại vị trí pixel hiện tại
                gc.drawImage(currentImage, pixelX, pixelY+ Bomberman.UI_PANEL_HEIGHT, Sprite.SCALED_SIZE, Sprite.SCALED_SIZE); // Vẽ với kích thước đã scale
            }
            // TODO: Logic vẽ các hiệu ứng khác của LifeItem
        }
    }

    // --- TRIỂN KHAI PHƯƠNG THỨC applyEffect() ---
    // Phương thức này được gọi khi Player nhặt được LifeItem
    @Override
    public void applyEffect(Player player) {
        if (isActive()) { // Chỉ áp dụng hiệu ứng nếu vật phẩm còn active
            System.out.println("LifeItem collected by Player at (" + gridX + ", " + gridY + "). Increasing lives."); // Log

            // TODO: Gọi phương thức trong Player để tăng số mạng
            // Cần thêm phương thức applyLifeItemEffect() vào lớp Player
            player.increaseLives();
            Bomberman game = player.getGameManager();
            if (game != null) {
                game.addScore(50); // Ví dụ: cộng 50 điểm cho Flame Item
            }/// Gọi phương thức đã thêm vào Player

            setActive(false); // Đánh dấu active = false để vật phẩm biến mất sau khi nhặt
            // TODO: Phát âm thanh nhặt item
        }
    }
}
