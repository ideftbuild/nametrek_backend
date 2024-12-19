package com.nametrek.api.dto;

import com.nametrek.api.model.Player;
import com.nametrek.api.model.Room;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RoomPlayerResponse {
    public Player player;
    public Room room;
}
