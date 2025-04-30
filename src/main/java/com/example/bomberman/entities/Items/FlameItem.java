package com.example.bomberman.entities.Items;

import com.example.bomberman.Bomberman;
import com.example.bomberman.entities.Player;
import com.example.bomberman.graphics.Sprite;
import com.example.bomberman.graphics.Animation; // Import lớp Animation
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

// Lớp đại diện cho vật phẩm Flame Up (tăng độ dài ngọn lửa)
public class FlameItem extends Item {

    // --- Thuộc tính Animation cho FlameItem ---
    private Animation animation;
    private double animationTimer = 0; // Bộ đếm thời gian cho animation của Item

    // Constructor
    public FlameItem(int startGridX, int startGridY) {
        // Gọi constructor của lớp cha (Item), truyền vị trí và sprite ban đầu
        // Sử dụng Sprite.powerupFlames cho FlameItem
        super(startGridX, startGridY, Sprite.powerupFlames); // Vẫn truyền sprite ban đầu
        System.out.println("FlameItem created at (" + gridX + ", " + gridY + ")"); // Log để kiểm tra

        // --- Khởi tạo animation cho FlameItem ---
        double frameDuration = 0.2; // Thời gian hiển thị mỗi frame (ví dụ: 0.2 giây)
        boolean loop = true; // Animation lặp lại

        // Sử dụng các Sprite cho animation của FlameItem
        // Giả sử có Sprite.powerupFlames và Sprite.powerupFlames_alt cho animation nhấp nháy
        // Nếu chỉ có 1 sprite, lặp lại sprite đó
        animation = new Animation(frameDuration, loop, Sprite.powerupFlames, Sprite.powerupFlames); // Ví dụ: lặp lại cùng 1 sprite
        // Nếu có sprite nhấp nháy khác:
        // animation = new Animation(frameDuration, loop, Sprite.powerupFlames, Sprite.powerupFlames_alt);
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
        // TODO: Thêm các logic update đặc thù khác cho FlameItem nếu có
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
                currentImage = Sprite.powerupFlames.getFxImage(); // Sử dụng sprite mặc định của FlameItem
            }

            if (currentImage != null) {
                // Vẽ hình ảnh FlameItem tại vị trí pixel hiện tại
                gc.drawImage(currentImage, pixelX, pixelY+ Bomberman.UI_PANEL_HEIGHT, Sprite.SCALED_SIZE, Sprite.SCALED_SIZE); // Vẽ với kích thước đã scale
            }
            // TODO: Logic vẽ các hiệu ứng khác của FlameItem
        }
    }

    // --- TRIỂN KHAI PHƯƠNG THỨC applyEffect() ---
    // Phương thức này được gọi khi Player nhặt được FlameItem
    @Override
    public void applyEffect(Player player) {
        if (isActive()) { // Chỉ áp dụng hiệu ứng nếu vật phẩm còn active
            System.out.println("FlameItem collected by Player at (" + gridX + ", " + gridY + "). Increasing flame length."); // Log

            // TODO: Gọi phương thức trong Player để tăng độ dài ngọn lửa
            // Cần thêm phương thức applyFlameItemEffect() vào lớp Player
            player.increaseFlameLength(); // Gọi phương thức đã thêm vào Player

            setActive(false); // Đánh dấu active = false để vật phẩm biến mất sau khi nhặt
            // TODO: Phát âm thanh nhặt item
        }
    }
}
