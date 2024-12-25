package com.nametrek.api.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Topics {
    public String questionTopic;
    public String answerTopic;
    public String roomUpdatesTopic;

    public Topics(String questionTopic, String answerTopic, String roomUpdatesTopic) {
        this.questionTopic = questionTopic;
        this.answerTopic = answerTopic;
        this.roomUpdatesTopic = roomUpdatesTopic;
    }

    public String getQuestionTopic() {
        return questionTopic;
    }

    public String getAnswerTopic() {
        return answerTopic;
    }

    public String getRoomUpdatesTopic() {
        return roomUpdatesTopic;
    }
}
