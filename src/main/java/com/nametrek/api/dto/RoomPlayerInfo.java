package com.nametrek.api.dto;

import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomPlayerInfo {
    private UUID roomId;
    private String roomCode;
    private PlayerDto player;

    public RoomPlayerInfo(UUID roomId, PlayerDto player) {
        this.roomId = roomId;
        this.player = player;
    }

    public RoomPlayerInfo(UUID roomId, String roomCode, PlayerDto player) {
        this.roomId = roomId;
        this.roomCode = roomCode;
        this.player = player;
    }
}
