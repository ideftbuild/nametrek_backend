package com.nametrek.api.dto;

import lombok.Setter;

import java.util.UUID;

import lombok.Getter;

@Getter
@Setter
public class QuestionDto {
    private final String id = UUID.randomUUID().toString();
    private String question;
    private String category;
    private String playerId;

    public String getId() { return id; };

    public String getPlayerId() { return playerId; };
    // public QuestionDto(String category, String question, String playerId) {
    //     this.question = question;
    //     this.playerId = playerId;
    // }

    public QuestionDto(String category, String question) {
        this.category = category;
        this.question = question;
    }

}
