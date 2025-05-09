package com.example.bomberman; // Hoặc package bạn muốn

public enum GameState {
    MENU,        // Màn hình Menu chính (sẽ làm sau)
    PLAYING,     // Đang trong màn chơi
    PAUSED,      // Đang tạm dừng
    GAME_OVER,   // Màn hình thua cuộc (sẽ làm sau)
    GAME_OVER_TRANSITION,
    //LEVEL_CLEARED, // Màn hình chuyển màn (tùy chọn, sẽ làm sau)
    GAME_WON ,    // Màn hình thắng cuộc (sẽ làm sau)

}