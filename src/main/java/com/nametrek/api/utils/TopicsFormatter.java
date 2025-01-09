package com.nametrek.api.utils;

import java.util.UUID;

public class TopicsFormatter {
    private final static String QUESTION_TOPIC = "/rooms/%s/question";
    private final static String ANSWER_TOPIC = "/rooms/%s/answer"; 
    private final static String ROOM_UPDATES_TOPIC = "/rooms/%s";
    private final static String GAME_UPDATE = "/rooms/%s/game";


    public static String formatQuestionTopic(UUID roomId) {
        return String.format(QUESTION_TOPIC, roomId);
    }

    public static String formatAnswerTopic(UUID roomId) {
        return String.format(ANSWER_TOPIC, roomId);
    }

    public static String formatRoomUpdatesTopic(UUID roomId) {
        return String.format(ROOM_UPDATES_TOPIC, roomId);
    }

    public static String formatGameUpdateTopic(UUID roomId) {
        return String.format(GAME_UPDATE, roomId);
    }
}
