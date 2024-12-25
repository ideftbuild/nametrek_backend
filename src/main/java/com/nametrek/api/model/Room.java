package com.nametrek.api.model;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.nametrek.api.exception.RoomEmptyException;
import com.nametrek.api.exception.RoomFullException;

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
    
    public void incrementPlayerCount() {
        if (activePlayerCount >= maxPlayers) {
            throw new RoomFullException("The room is full. No more players can join");
        }
        activePlayerCount++;
    }

    public void incrementRound() {
        if (currentRound >= maxRounds) {
            throw new RoomFullException("Round complete");
        }
        currentRound++;
    }


    public void decrementPlayerCount() {
        if (activePlayerCount <= 0) {
            throw new RoomEmptyException("The room is empty");
        }
        activePlayerCount--;
    }

}
