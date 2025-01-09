package com.nametrek.api.dto;

import java.util.List;

import lombok.Data;

@Data
public class AnswerDto {
    private String answer;
    private String category;
    private Long playerId;

    public Long getPlayerId() { return playerId; }

    public String getAnswer() { return answer; }

    public AnswerDto() {
    }

    public AnswerDto(String category) {
        this.category = category;
    }

    public AnswerDto(String category, String answer, Long playerId) {
        this.category = category;
        this.answer = answer;
        this.playerId = playerId;
    }
}
