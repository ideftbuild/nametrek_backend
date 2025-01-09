package com.nametrek.api.service;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisServer;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.nametrek.api.model.Player;
import com.nametrek.api.model.PlayerSession;

@Service
public class DisconnectedPlayersCleanup {
    private final RedisService redisService;
    private final RoomService roomService;
    private static final int CLEANUP_CHECK_SECONDS = 30; // Check 30 seconds difference
    public final String PLAYER_SESSION_KEY = "player:session:";
                                                         
    @Autowired
    public DisconnectedPlayersCleanup(RoomService roomService, RedisService redisService) {
        this.roomService = roomService;
        this.redisService = redisService;
    }

    @Scheduled(fixedRate = 30000) // execute every 30 seconds
    public void cleanUpSessions() {
        Set<String> sessionKeys = redisService.template.keys(PLAYER_SESSION_KEY + "*");

        if (sessionKeys == null || sessionKeys.isEmpty()) return;

        long currentTime = System.currentTimeMillis();

        for (String sessionKey : sessionKeys) {
            PlayerSession session = (PlayerSession) redisService.getValue(sessionKey);
            if (session == null) continue;

            if (currentTime + TimeUnit.SECONDS.toMillis(CLEANUP_CHECK_SECONDS) >= session.getExpiryTime()) {
                roomService.leave(session.getRoomId(), session.getPlayerId());
            }
        }
    }

}
