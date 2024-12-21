package com.nametrek.api.dto;

import com.nametrek.api.model.Player;
import com.nametrek.api.model.Room;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomEventResponse {
    private Room room;
    private Player player;
    private String eventType; // e.g., "join" or "leave" 
    private String timestamp; 
                           
    public RoomEventResponse(Room room, Player player, String eventType, String timestamp) {
        this.room = room;
        this.player = player;
        this.eventType = eventType;
        this.timestamp = timestamp;
    }
}
