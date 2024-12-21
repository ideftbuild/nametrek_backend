package com.nametrek.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Player implements Identifiable, Scorable {
	private String id = UUID.randomUUID().toString();
	private String username;
    private Integer score = 0;
    private String roomId;

    public Player (String username) {
        this.username = username;
    }

    public Player (String username, String roomId) {
        this.username = username;
        this.roomId = roomId;
    }

    public Player (String username, String roomId, Integer score) {
        this.username = username;
        this.roomId = roomId;
        this.score = score;
    }

    @Override
    public String getId() {
        return id;
    }

    public Player deepCopy(ObjectMapper ObjectMapper) {
        try {
            return ObjectMapper.readValue(
                ObjectMapper.writeValueAsString(this),
                Player.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error create deep copy of player", e);
        }
    }

    public void incrementScore(Integer step) {
        score += step;
    }
}
