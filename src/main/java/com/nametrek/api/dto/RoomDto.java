package com.nametrek.api.dto;

import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class RoomDto {
	private Integer currentRound;
    private Integer activePlayerCount;
}
