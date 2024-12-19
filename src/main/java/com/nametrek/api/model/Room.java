package com.nametrek.api.model;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
public class Room implements Identifiable {
    private final String id = UUID.randomUUID().toString();
	private String status = "active";
    private Integer activePlayerCount = 0;
	private Integer maxPlayers = 4;
	private Integer currentRound = 0;
	private Integer maxRounds = 4;
    private String owner;

    public Room(Integer activePlayerCount, String owner) {
        this.owner = owner;
        this.activePlayerCount = activePlayerCount;
    }
}
