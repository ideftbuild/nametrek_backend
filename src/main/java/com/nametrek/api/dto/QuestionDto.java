package com.nametrek.api.dto;

import lombok.Setter;

import java.util.UUID;

import lombok.Getter;

@Getter
@Setter
public class QuestionDto {
    private String category;
    private Long playerId;


    public Long getPlayerId() { return playerId; };

    public QuestionDto(String category) {
        this.category = category;
    }

}
