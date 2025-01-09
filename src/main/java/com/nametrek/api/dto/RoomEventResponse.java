package com.nametrek.api.dto;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomEventResponse {
    private Long playerId;
    private RoomDto room;
    private List<PlayerDto> players;
    private EventType eventType; // e.g., "join" or "leave" 
                           
    public RoomEventResponse(RoomDto room, List<PlayerDto> players, EventType eventType) {
        this.room = room;
        this.players = players;
        this.eventType = eventType;
    }

    public RoomEventResponse(Long playerId, List<PlayerDto> players, EventType eventType) { 
        this.playerId = playerId;
        this.players = players;
        this.eventType = eventType;
    }
}

