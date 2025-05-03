package com.example.bomberman.entities;

import com.example.bomberman.Map.Map;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite;

public class Oneal extends Enemy {

    public Oneal(int startGridX, int startGridY, Map map) {
        super(startGridX, startGridY, map);
        speed = 100;

        // --- Initialize animations ---

        walkLeftAnimation = new Animation(frameDuration, true, Sprite.oneal_left1, Sprite.oneal_left2, Sprite.oneal_left3);
        walkRightAnimation = new Animation(frameDuration, true, Sprite.oneal_right1, Sprite.oneal_right2, Sprite.oneal_right3);
        walkUpAnimation = new Animation(frameDuration, true, Sprite.oneal_left1, Sprite.oneal_left2, Sprite.oneal_left3); // Using left sprites
        walkDownAnimation = new Animation(frameDuration, true, Sprite.oneal_right1, Sprite.oneal_right2, Sprite.oneal_right3); // Using right sprites
        deadAnimation = new Animation(frameDuration, false, Sprite.oneal_dead);
        currentAnimation = walkLeftAnimation; // Default animation
    }
}
















