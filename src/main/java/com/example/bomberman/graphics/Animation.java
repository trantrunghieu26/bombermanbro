package com.example.bomberman.graphics;



// Lớp quản lý chuỗi các Sprite để tạo Animation
public class Animation {

    private Sprite[] frames; // Mảng các Sprite tạo nên animation
    private double frameDuration; // Thời gian hiển thị mỗi frame (giây)
    private boolean loop; // Có lặp lại animation không
    private double totalDuration; // Tổng thời gian của animation (giây)

    // Constructor
    public Animation(double frameDuration, boolean loop, Sprite... frames) {
        this.frameDuration = frameDuration;
        this.loop = loop;
        this.frames = frames;
        this.totalDuration = frames.length * frameDuration; // Tính tổng thời gian
    }

    // Phương thức lấy frame hiện tại dựa trên thời gian đã trôi qua
    public Sprite getFrame(double animationTimer) {
        if (frames == null || frames.length == 0) {
            return null; // Trả về null nếu không có frame nào
        }

        // Nếu không lặp và animation đã kết thúc, trả về frame cuối cùng hoặc null
        if (!loop && animationTimer >= totalDuration) {
            return frames[frames.length - 1]; // Trả về frame cuối cùng
            // Hoặc có thể trả về null nếu muốn animation biến mất hoàn toàn sau khi kết thúc
            // return null;
        }

        // Tính toán chỉ mục frame hiện tại
        // Sử dụng phép chia lấy phần dư (%) để xử lý animation lặp
        int frameIndex = (int) ((animationTimer / frameDuration));

        if (loop) {
            frameIndex = frameIndex % frames.length; // Đảm bảo chỉ mục nằm trong giới hạn khi lặp
        } else {
            // Đảm bảo chỉ mục không vượt quá giới hạn khi không lặp
            frameIndex = Math.min(frameIndex, frames.length - 1);
        }


        // Trả về Sprite tương ứng với chỉ mục frame
        return frames[frameIndex];
    }

    // --- Phương thức mới để kiểm tra xem animation đã kết thúc chưa (chỉ dùng cho non-looping) ---
    public boolean isFinished(double animationTimer) {
        // Animation được coi là kết thúc khi thời gian đã trôi qua lớn hơn hoặc bằng tổng thời gian của animation
        // Phương thức này chỉ thực sự có ý nghĩa cho animation KHÔNG lặp lại
        return !loop && animationTimer >= totalDuration;
    }

    // --- Getters ---
    public double getFrameDuration() {
        return frameDuration;
    }

    public boolean isLooping() {
        return loop;
    }

    public int getFrameCount() {
        return frames.length;
    }

    public double getTotalDuration() {
        return totalDuration;
    }

}
