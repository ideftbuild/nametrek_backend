package com.nametrek.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GameEvent<T> {

    public GameEventType type;
    public T value;

    public GameEvent(GameEventType type, T value) {
        this.type = type;  // Identifies the event (e.g., "countdown", "gameUpdate")
        this.value = value;  // Holds the data (e.g., countdown value, message, etc.)
    }
}
