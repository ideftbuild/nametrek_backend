package com.nametrek.api.utils;

import java.util.UUID;

public final class RedisKeys {
    private RedisKeys() {}

    // Player related keys
    public static final String ACTIVE_PLAYER_COUNT = "active:player:count";
    public static final String PLAYER_SESSION = "player:session:";
    public static final String PLAYER_NAME = "player:name:";
    public static final String PLAYER_SCORE = "player:score:";
    public static final String PLAYER_ANSWER = "player:answer:";
    public static final String PLAYER_TURN = "player:turn";
    public static final String PLAYER_LOST_STATUS = "player:lost:";

    // Room related keys
    public static final String ROOM = "rooms:";
    public static final String ROOM_CODE = "room:code";
    public static final String IN_GAME_PLAYERS_KEY = "rooms:%s:players";
    public static final String ROUND = "round";
    public static final String OWNER = "owner";
    public static final String COUNT_DOWN_TASK = "answer:countdown:task:";
    public static final String USED_ANSWERS = "rooms:%s:round:%d";

    // Game related keys
    public static final String IN_GAME = "inGame";

    public static String formatPlayerLostStatus(Long playerId) {
        return PLAYER_LOST_STATUS + playerId;
    }

    public static String formatPlayerSessionKey(Long playerId) {
        return PLAYER_SESSION + playerId;
    }

    public static String formatPlayerNameKey(Long playerId) {
        return PLAYER_NAME + playerId;
    }

    public static String formatRoomKey(UUID roomId) {
        return ROOM + roomId;
    }

    public static String formatRoomCodeKey(String code) {
        return ROOM_CODE + code;
    }

    public static String formatInGamePlayersKey(UUID roomId) {
        return String.format(IN_GAME_PLAYERS_KEY, roomId);
    }

    public static String formatPlayerAnswerKey(Long playerId) {
        return PLAYER_ANSWER + playerId;
    }

    public static String formatPlayerScoreKey(Long playerId) {
        return PLAYER_SCORE + playerId;
    }

    public static String formatCountDownTaskKey(UUID roomId) {
        return COUNT_DOWN_TASK + roomId;
    }

    public static String formatUsedAnswers(UUID roomId, Integer round) {
        return String.format(USED_ANSWERS, roomId, round);
    }
}
