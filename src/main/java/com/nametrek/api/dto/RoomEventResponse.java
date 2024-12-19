package com.nametrek.api.dto;

import com.nametrek.api.model.Player;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RoomEventResponse {
    private Player player;
    private String eventType; // e.g., "join" or "leave" 
    private String timestamp; // Use ISO 8601 format for consistency
}
