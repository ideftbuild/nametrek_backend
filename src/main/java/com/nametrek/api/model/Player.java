package com.nametrek.api.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

import jakarta.persistence.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Data
@NoArgsConstructor
@Entity
@Table(name="players")
public class Player {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public enum EventType {
        INACTIVE, ACTIVE
    }
	private String name;
    private Double score = 0d;
    private EventType status;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false, foreignKey = @ForeignKey(value = ConstraintMode.CONSTRAINT, name = "FK_ROOM_PLAYER"))
    private Room room;

    public Player (String name) {
        this.name = name;
    }

    public Player (String name, Room room) {
        this.name = name;
        this.room = room;
    }

    public Player (String name, Room room, Double score) {
        this.name = name;
        this.room = room;
        this.score = score;
    }

    public Long getId() {
        return id;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public void activate() {
        this.status = EventType.ACTIVE;
    }

    public void deactivate() {
        this.status = EventType.INACTIVE;
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

    public boolean isSameRoom(Room room) {
        return this.room.equals(room);
    }
}
