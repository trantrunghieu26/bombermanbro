package com.example.bomberman.entities;

import com.example.bomberman.Map.Map;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite;

public class Ghost extends Enemy {

    public Ghost(int startGridX, int startGridY, Map map) {
        super(startGridX, startGridY, map);
        speed = 170;

        // --- Initialize animations ---

        walkLeftAnimation = new Animation(frameDuration, true, Sprite.ghost_left1, Sprite.ghost_left2, Sprite.ghost_left3);
        walkRightAnimation = new Animation(frameDuration, true, Sprite.ghost_right1, Sprite.ghost_right2, Sprite.ghost_right3);
        walkUpAnimation = new Animation(frameDuration, true, Sprite.ghost_left1, Sprite.ghost_left2, Sprite.ghost_left3); // Using left sprites
        walkDownAnimation = new Animation(frameDuration, true, Sprite.ghost_right1, Sprite.ghost_right2, Sprite.ghost_right3); // Using right sprites
        deadAnimation = new Animation(frameDuration, false, Sprite.ghost_dead);
        currentAnimation = walkLeftAnimation; // Default animation
    }
}
