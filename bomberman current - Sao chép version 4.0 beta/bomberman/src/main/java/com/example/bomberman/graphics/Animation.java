package com.example.bomberman.graphics;

// Lớp trợ giúp để quản lý một chuỗi các Sprite cho animation
public class Animation {
    private Sprite[] frames; // Các khung hình (Sprite) của animation
    private double frameDuration; // Thời gian hiển thị mỗi khung hình (giây)
    private double totalDuration; // Tổng thời gian của animation (giây)
    private boolean loop; // Có lặp lại animation không

    // Constructor
    public Animation(double frameDuration, boolean loop, Sprite... frames) {
        this.frameDuration = frameDuration;
        this.loop = loop;
        this.frames = frames;
        this.totalDuration = frameDuration * frames.length;
    }

    // Lấy frame Sprite hiện tại dựa trên thời gian đã trôi qua (animationTime)
    public Sprite getFrame(double animationTime) {
        if (frames == null || frames.length == 0) {
            return null; // Không có frame nào
        }

        double time = animationTime;
        if (loop) {
            // Nếu lặp lại, lấy phần dư của thời gian so với tổng thời gian animation
            time = animationTime % totalDuration;
        } else {
            // Nếu không lặp lại, giới hạn thời gian trong tổng thời gian animation
            time = Math.min(animationTime, totalDuration);
        }

        // Tính chỉ số frame dựa trên thời gian và thời gian hiển thị mỗi frame
        int frameIndex = (int) (time / frameDuration);

        // Đảm bảo chỉ số frame nằm trong phạm vi mảng
        if (frameIndex >= frames.length) {
            frameIndex = frames.length - 1; // Giữ ở frame cuối nếu không lặp và hết thời gian
        }

        return frames[frameIndex];
    }

    public double getTotalDuration() {
        return totalDuration;
    }

    public boolean isLooping() {
        return loop;
    }
}
