package com.nametrek.api.dto;

public enum GameEventType {
    GAME_COUNTDOWN,  // Countdown before the game starts
    GAME_UPDATE,     // General update during the game
    GAME_STARTED,    // Game has started and is ongoing
    GAME_ENDED,      // Entire game has ended
    ROUND_STARTED,   // A specific round has started
    ROUND_ENDED,      // A specific round has ended
    GAME_MESSAGE,
    LOSS,
    WIN,
}
