package com.example.bomberman.entities;

import com.example.bomberman.Map.Map;
import com.example.bomberman.graphics.Animation;
import com.example.bomberman.graphics.Sprite;

public class Kondoria extends Enemy {

    public Kondoria(int startGridX, int startGridY, Map map) {
        super(startGridX, startGridY, map);
        speed = 230;

        // --- Initialize animations ---

        walkLeftAnimation = new Animation(frameDuration, true, Sprite.kondoria_left1, Sprite.kondoria_left2, Sprite.kondoria_left3);
        walkRightAnimation = new Animation(frameDuration, true, Sprite.kondoria_right1, Sprite.kondoria_right2, Sprite.kondoria_right3);
        walkUpAnimation = new Animation(frameDuration, true, Sprite.kondoria_left1, Sprite.kondoria_left2, Sprite.kondoria_left3); // Using left sprites
        walkDownAnimation = new Animation(frameDuration, true, Sprite.kondoria_right1, Sprite.kondoria_right2, Sprite.kondoria_right3); // Using right sprites
        deadAnimation = new Animation(frameDuration, false, Sprite.kondoria_dead);
        currentAnimation = walkLeftAnimation; // Default animation

    }
}
