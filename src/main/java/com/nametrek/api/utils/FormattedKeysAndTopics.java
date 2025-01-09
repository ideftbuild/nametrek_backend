package com.nametrek.api.utils;

import java.util.UUID;

import lombok.Getter;

@Getter
public class FormattedKeysAndTopics {
    String gameUpdateTopic;
    String answerTopic;
    String questionTopic;
    String gameTopic;
    String roomKey;
    String roomPlayerKey;
    String usedAnswersKey;
    // other topics

    public FormattedKeysAndTopics() {
        //
    }

    public FormattedKeysAndTopics(
            String gameUpdateTopic, 
            String answerTopic,
            String questionTopic,
            String roomKey,
            String usedAnswersKey) {
        this.roomKey = roomKey;
        this.usedAnswersKey = usedAnswersKey;
        this.gameUpdateTopic = gameUpdateTopic;
        this.answerTopic  = answerTopic;
        this.questionTopic = questionTopic;
    }

    public void setKeys(UUID roomId) {
        this.roomKey = RedisKeys.formatRoomKey(roomId);
        this.roomPlayerKey = RedisKeys.formatRoomPlayersKey(roomId);
    }

    public void setTopics(UUID roomId) {
        this.gameUpdateTopic = TopicsFormatter.formatGameUpdateTopic(roomId);
        this.answerTopic  = TopicsFormatter.formatAnswerTopic(roomId);
        this.questionTopic = TopicsFormatter.formatQuestionTopic(roomId);
    }

    public void setKeysAndTopics(UUID roomId) {
        this.roomKey = RedisKeys.formatRoomKey(roomId);
        this.roomPlayerKey = RedisKeys.formatRoomPlayersKey(roomId);
        this.gameUpdateTopic = TopicsFormatter.formatGameUpdateTopic(roomId);
        this.answerTopic  = TopicsFormatter.formatAnswerTopic(roomId);
        this.questionTopic = TopicsFormatter.formatQuestionTopic(roomId);
    }

    public void setUsedAnswerKey(UUID roomId, Integer round) {
        this.usedAnswersKey = RedisKeys.formatUsedAnswers(roomId, round);
    }

}
