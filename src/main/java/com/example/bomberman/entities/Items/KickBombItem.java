
package com.example.bomberman.entities.Items;

import com.example.bomberman.Bomberman;
import com.example.bomberman.entities.Player;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

// Lớp đại diện cho vật phẩm Kick Bomb (cho phép đá Bom đã đặt)
public class KickBombItem extends Item {

    // --- Thuộc tính Animation cho KickBombItem ---
    private Animation animation;
    private double animationTimer = 0; // Bộ đếm thời gian cho animation của Item

    // Constructor
    public KickBombItem(int startGridX, int startGridY) {
        super(startGridX, startGridY, Sprite.powerupKickBomb); // <-- Sử dụng sprite đúng
        System.out.println("KickBombItem created at (" + gridX + ", " + gridY + ")"); // Log để kiểm tra
        // --- Khởi tạo animation cho KickBombItem ---
        double frameDuration = 0.2; // Thời gian hiển thị mỗi frame (ví dụ: 0.2 giây). HÃY ĐIỀU CHỈNH.
        boolean loop = true; // Animation lặp lại.
        animation = new Animation(frameDuration, loop, Sprite.powerupKickBomb, Sprite.powerupKickBomb);
    }

    @Override
    public void update(double deltaTime) {
         super.update(deltaTime); // <-- Gọi lớp cha nếu cần

        if (isActive()) {
            animationTimer += deltaTime;
        }
    }

    // --- Override phương thức render để vẽ frame animation hiện tại ---
    @Override
    public void render(GraphicsContext gc) {
        // Chỉ vẽ nếu vật phẩm còn active (chưa bị nhặt)
        if (isActive()) { // Sử dụng phương thức isActive() từ lớp Item base
            Image currentImage = null;

            // Lấy frame animation hiện tại để vẽ, sử dụng animationTimer của Item
            if (animation != null) { // Kiểm tra animation không null
                Sprite currentSpriteFrame = animation.getFrame(animationTimer); // Lấy frame dựa trên timer và animation
                if (currentSpriteFrame != null) {
                    currentImage = currentSpriteFrame.getFxImage(); // Lấy JavaFX Image từ Sprite frame
                }
            }

            // Fallback: Nếu không có animation hoặc animation chưa sẵn sàng, dùng sprite mặc định từ lớp cha (được gán trong constructor)
            if (currentImage == null && sprite != null) {
                currentImage = sprite.getFxImage(); // Sử dụng sprite ban đầu của Item base
            }

            // Fallback cuối cùng nếu vẫn không có hình ảnh (không nên xảy ra nếu sprite ban đầu hợp lệ)
            if (currentImage == null) {
                currentImage = Sprite.powerupKickBomb.getFxImage(); // Sử dụng sprite powerupBombpass làm mặc định cuối cùng
            }


            if (currentImage != null) {
                gc.drawImage(currentImage, pixelX, pixelY+ Bomberman.UI_PANEL_HEIGHT, Sprite.SCALED_SIZE, Sprite.SCALED_SIZE);
            }

            // TODO: Logic vẽ các hiệu ứng khác của KickBombItem (nếu có)
        }
    }


    // --- TRIỂN KHAI PHƯƠNG THỨC applyEffect() (BẮT BUỘC vì lớp cha là abstract) ---
    // Phương thức này được gọi bởi Bomberman khi Player nhặt được KickBombItem
    @Override
    public void applyEffect(Player player) {
        if (isActive()) { // Chỉ áp dụng hiệu ứng nếu vật phẩm còn active
            System.out.println("KickBombItem collected by Player at (" + gridX + ", " + gridY + "). Enabling Kick Bomb ability."); // Log


            player.enableKickBomb(); // enableKickBomb() là phương thức trong Player

            setActive(false); // Đánh dấu active = false để vật phẩm biến mất sau khi nhặt
            // TODO: Phát âm thanh nhặt item
        }
    }

    // --- Getters ---
    // Bạn có thể thêm getters đặc thù cho KickBombItem nếu có
}