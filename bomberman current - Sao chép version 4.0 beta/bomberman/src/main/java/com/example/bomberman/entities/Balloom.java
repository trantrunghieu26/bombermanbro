package com.example.bomberman.entities;

import com.example.bomberman.Map.Map;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite;

public class Balloom extends Enemy {

    public Balloom(int startGridX, int startGridY, Map map) {
        super(startGridX, startGridY, map);
        speed = 80.0;

        // --- Initialize animations ---

        walkLeftAnimation = new Animation(frameDuration, true, Sprite.balloom_left1, Sprite.balloom_left2, Sprite.balloom_left3);
        walkRightAnimation = new Animation(frameDuration, true, Sprite.balloom_right1, Sprite.balloom_right2, Sprite.balloom_right3);
        walkUpAnimation = new Animation(frameDuration, true, Sprite.balloom_left1, Sprite.balloom_left2, Sprite.balloom_left3); // Tạm thời dùng left
        walkDownAnimation = new Animation(frameDuration, true, Sprite.balloom_right1, Sprite.balloom_right2, Sprite.balloom_right3); // Tạm thời dùng right
        deadAnimation = new Animation(frameDuration, false, Sprite.balloom_dead); // Animation chết, không lặp lại
        currentAnimation = walkLeftAnimation; // Animation mặc định
    }
}
