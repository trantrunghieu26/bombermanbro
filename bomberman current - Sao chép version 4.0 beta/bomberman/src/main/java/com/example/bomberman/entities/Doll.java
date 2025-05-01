package com.example.bomberman.entities;

import com.example.bomberman.Map.Map;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite;

public class Doll extends Enemy {

    public Doll(int startGridX, int startGridY, Map map) {
        super(startGridX, startGridY, map);
        speed = 140;

        // --- Initialize animations ---

        walkLeftAnimation = new Animation(frameDuration, true, Sprite.doll_left1, Sprite.doll_left2, Sprite.doll_left3);
        walkRightAnimation = new Animation(frameDuration, true, Sprite.doll_right1, Sprite.doll_right2, Sprite.doll_right3);
        walkUpAnimation = new Animation(frameDuration, true, Sprite.doll_left1, Sprite.doll_left2, Sprite.doll_left3); // Using left sprites
        walkDownAnimation = new Animation(frameDuration, true, Sprite.doll_right1, Sprite.doll_right2, Sprite.doll_right3); // Using right sprites
        deadAnimation = new Animation(frameDuration, false, Sprite.doll_dead);
        currentAnimation = walkLeftAnimation; // Default animation
    }
}
