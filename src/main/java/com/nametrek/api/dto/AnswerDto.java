package com.nametrek.api.dto;

import lombok.Setter;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AnswerDto {
    private String answer;
    private String category;
    private String playerId;
    private String questionId;
    private boolean isCorrect;

    public boolean getIsCorrect() { return isCorrect; }

    public String getPlayerId() { return playerId; }

    public String getQuestionId() { return questionId; }

    public String getAnswer() { return answer; }

    public AnswerDto(String category, String answer, String playerId) {
        this.category = category;
        this.answer = answer;
        this.playerId = playerId;
    }

    public void markAsCorrect() {
        this.isCorrect = true;
    }

    public void markAsIncorrect() {
        this.isCorrect = false;
    }
}
