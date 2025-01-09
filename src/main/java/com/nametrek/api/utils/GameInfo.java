package com.nametrek.api.utils;

import java.util.UUID;

public class GameInfo {
    private UUID roomId;
    private Integer round;
    private Long player;
    // other game-related fields (e.g., current score, players, etc.)

    public GameInfo(UUID roomId) {
        this.roomId = roomId;
    }
    // Constructor to initialize the fields
    public GameInfo(UUID roomId, Integer rounds) {
        this.roomId = roomId;
        this.round = rounds;
    }

    // Getters and Setters
    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public int getRound() {
        return round;
    }

    public void setRound(Integer rounds) {
        this.round = rounds;
    }

    public void setPlayer(Long player) {
        this.player = player;
    }

    public Long getPlayer() {
        return player;
    }
    // Other necessary methods (e.g., toString, equals, hashCode, etc.)
}
