    package com.example.bomberman.input;

    import javafx.scene.Scene;
    import javafx.scene.input.KeyEvent;
    import com.example.bomberman.entities.Player;

    public class InputHandler {
        private final Player player;

        public InputHandler(Scene scene, Player player) {
            this.player = player;
            scene.setOnKeyPressed(this::handleKeyPress);
        }

        private void handleKeyPress(KeyEvent event) {
            player.move(event.getCode());
        }
    }
