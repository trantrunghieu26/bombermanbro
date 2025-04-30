package com.example.bomberman.entities.Items;

import com.example.bomberman.Bomberman;
import com.example.bomberman.entities.Player;
import com.example.bomberman.graphics.Sprite;
import com.example.bomberman.graphics.Animation; // Import lớp Animation
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

// Lớp đại diện cho vật phẩm Bomb Up (tăng số lượng bom tối đa)
public class BombItem extends Item {

    // --- Thuộc tính Animation cho BombItem ---
    private Animation animation;
    private double animationTimer = 0; // Bộ đếm thời gian cho animation của Item

    // Constructor
    public BombItem(int startGridX, int startGridY) {
        // Gọi constructor của lớp cha (Item), truyền vị trí và sprite ban đầu
        // Sử dụng Sprite.powerupBombs cho BombItem
        super(startGridX, startGridY, Sprite.powerupBombs); // Vẫn truyền sprite ban đầu
        System.out.println("BombItem created at (" + gridX + ", " + gridY + ")"); // Log để kiểm tra

        // --- Khởi tạo animation cho BombItem ---
        double frameDuration = 0.2; // Thời gian hiển thị mỗi frame (ví dụ: 0.2 giây)
        boolean loop = true; // Animation lặp lại

        // Sử dụng các Sprite cho animation của BombItem (ví dụ: nhấp nháy giữa 2 sprite)
        // Bạn cần đảm bảo các Sprite này tồn tại trong Sprite.java
        // Nếu chỉ có 1 sprite cho Item, animation này có thể đơn giản là lặp lại sprite đó
        animation = new Animation(frameDuration, loop, Sprite.powerupBombs, Sprite.powerupBombs); // Ví dụ: lặp lại cùng 1 sprite
        // Nếu có sprite nhấp nháy khác:
        // animation = new Animation(frameDuration, loop, Sprite.powerupBombs, Sprite.powerupBombs_alt); // Ví dụ: nhấp nháy giữa 2 sprite khác nhau
    }

    // --- Override phương thức update để xử lý animation và các logic khác ---
    @Override
    public void update(double deltaTime) {
        // Gọi update của lớp cha nếu có logic chung cần thiết
         super.update(deltaTime); // Lớp cha Item hiện tại không có logic update

        // --- Cập nhật bộ đếm thời gian animation ---
        // Chỉ cập nhật timer nếu vật phẩm còn active
        if (isActive()) {
            animationTimer += deltaTime;
            // TODO: Nếu Item có thời gian tồn tại giới hạn, kiểm tra ở đây
            // Ví dụ: if (animationTimer > maxItemLifetime) { setActive(false); }
        }

        // TODO: Thêm các logic update đặc thù khác cho BombItem nếu có
    }

    // --- Override phương thức render để vẽ frame animation hiện tại ---
    @Override
    public void render(GraphicsContext gc) {
        // Chỉ vẽ nếu vật phẩm còn active (chưa bị nhặt)
        if (isActive()) { // Sử dụng phương thức isActive() từ lớp cha Item
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
                currentImage = Sprite.powerupBombs.getFxImage(); // Sử dụng sprite mặc định của BombItem
            }


            if (currentImage != null) {
                // Vẽ hình ảnh BombItem tại vị trí pixel hiện tại
                // Cần điều chỉnh vị trí vẽ nếu sprite có kích thước khác Sprite.SCALED_SIZE
                // hoặc nếu bạn muốn căn giữa sprite.
                // Hiện tại đang vẽ góc trên bên trái tại pixelX, pixelY (từ lớp cha Item)
                gc.drawImage(currentImage, pixelX, pixelY+ Bomberman.UI_PANEL_HEIGHT, Sprite.SCALED_SIZE, Sprite.SCALED_SIZE); // Vẽ với kích thước đã scale
            }

            // TODO: Logic vẽ các hiệu ứng khác của BombItem
        }
    }


    // --- TRIỂN KHAI PHƯƠNG THỨC applyEffect() (BẮT BUỘC vì lớp cha là abstract) ---
    // Phương thức này được gọi khi Player nhặt được BombItem
    @Override
    public void applyEffect(Player player) {
        if (isActive()) { // Chỉ áp dụng hiệu ứng nếu vật phẩm còn active
            System.out.println("BombItem collected by Player at (" + gridX + ", " + gridY + "). Increasing bomb limit."); // Log
            player.increaseMaxBombs();
            setActive(false); // Đánh dấu active = false để vật phẩm biến mất sau khi nhặt
            // TODO: Phát âm thanh nhặt item
        }
    }

    // --- Getters ---
    // Có thể cần thêm getters đặc thù cho BombItem nếu có
}
