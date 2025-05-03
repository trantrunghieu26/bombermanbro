package com.example.bomberman.entities;

import com.example.bomberman.Map.Map;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite;

public class Minvo extends Enemy {

    public Minvo(int startGridX, int startGridY, Map map) {
        super(startGridX, startGridY, map);
        speed = 200;

        // --- Initialize animations ---

        walkLeftAnimation = new Animation(frameDuration, true, Sprite.minvo_left1, Sprite.minvo_left2, Sprite.minvo_left3);
        walkRightAnimation = new Animation(frameDuration, true, Sprite.minvo_right1, Sprite.minvo_right2, Sprite.minvo_right3);
        walkUpAnimation = new Animation(frameDuration, true, Sprite.minvo_left1, Sprite.minvo_left2, Sprite.minvo_left3); // Using left sprites
        walkDownAnimation = new Animation(frameDuration, true, Sprite.minvo_right1, Sprite.minvo_right2, Sprite.minvo_right3); // Using right sprites
        deadAnimation = new Animation(frameDuration, false, Sprite.minvo_dead);
        currentAnimation = walkLeftAnimation; // Default animation

    }
}
