package com.nametrek.api.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Getter
@Setter
public class RoomDto {
	private Integer round;
    private Integer rounds;
    private Integer maxPlayers;
    private Long owner;

    public RoomDto(Integer round, Integer rounds, Integer maxPlayers, Long owner) {
        this.round = round;
        this.rounds = rounds;
        this.maxPlayers = maxPlayers;
        this.owner = owner;
    }
}
