
package com.example.bomberman.entities.Items;

import com.example.bomberman.entities.Player;
import com.example.bomberman.graphics.Sprite;
import com.example.bomberman.graphics.Animation; // Import lớp Animation
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image; // Import Image

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

        // Sử dụng các Sprite cho animation của KickBombItem.
        // Nếu chỉ có 1 sprite (powerupBombpass), bạn có thể lặp lại sprite đó.
        // Nếu có sprite nhấp nháy khác (ví dụ: powerupBombpass_alt), bạn có thể dùng cả hai.
        animation = new Animation(frameDuration, loop, Sprite.powerupKickBomb, Sprite.powerupKickBomb); // <-- Animation lặp lại sprite chính
        // TODO: Nếu có sprite nhấp nháy khác, thay dòng trên bằng:
        // animation = new Animation(frameDuration, loop, Sprite.powerupBombpass, Sprite.powerupBombpass_alt);
    }

    // --- Override phương thức update để xử lý animation và các logic khác ---
    @Override
    public void update(double deltaTime) {
        // TODO: Nếu lớp Item base có logic update chung cần thiết (ví dụ: timer tồn tại), hãy gọi super.update(deltaTime);
        // super.update(deltaTime); // <-- Gọi lớp cha nếu cần

        // --- Cập nhật bộ đếm thời gian animation ---
        // Chỉ cập nhật timer nếu vật phẩm còn active
        if (isActive()) { // isActive() là phương thức từ lớp Item base
            animationTimer += deltaTime;
            // TODO: Nếu Item có thời gian tồn tại giới hạn, kiểm tra ở đây
            // Ví dụ: if (animationTimer > maxItemLifetime) { setActive(false); } // setActive() từ Item base
        }

        // TODO: Thêm các logic update đặc thù khác cho KickBombItem nếu có
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
                // Vẽ hình ảnh KickBombItem tại vị trí pixel hiện tại
                // pixelX, pixelY là thuộc tính từ lớp Item base
                // Vẽ với kích thước đã scale (Sprite.SCALED_SIZE)
                gc.drawImage(currentImage, pixelX, pixelY, Sprite.SCALED_SIZE, Sprite.SCALED_SIZE);
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

            // GỌI PHƯƠNG THỨC TRONG LỚP PLAYER ĐỂ BẬT KHẢ NĂNG Đá Bom
            player.enableKickBomb(); // enableKickBomb() là phương thức trong Player

            setActive(false); // Đánh dấu active = false để vật phẩm biến mất sau khi nhặt
            // TODO: Phát âm thanh nhặt item
        }
    }

    // --- Getters ---
    // Bạn có thể thêm getters đặc thù cho KickBombItem nếu có
}