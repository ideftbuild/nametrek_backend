package com.nametrek.api.listener;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.nametrek.api.service.RoomService;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class WebSocketEventListener {
    private final RoomService roomService;

    @Autowired
    public WebSocketEventListener(RoomService roomService) {
        this.roomService = roomService;
    }

    @EventListener
    public void handleWebSocketConnectionListener(SessionConnectEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            String sessionId = headerAccessor.getSessionId();
            
            List<String> playerIds = headerAccessor.getNativeHeader("playerId");
            List<String> roomIds = headerAccessor.getNativeHeader("roomId");
            
            if (playerIds == null || roomIds == null || playerIds.isEmpty() || roomIds.isEmpty()) {
                log.error("Missing required headers. SessionId: {}", sessionId);
                return;
            }

            String playerId = playerIds.get(0);
            String roomId = roomIds.get(0);
            
            log.info("New WebSocket connection - SessionId: {}, PlayerId: {}, RoomId: {}", 
                    sessionId, playerId, roomId);

            headerAccessor.getSessionAttributes().put("playerId", playerId);
            headerAccessor.getSessionAttributes().put("roomId", roomId);
            
            roomService.connect(sessionId, UUID.fromString(roomId), Long.valueOf(playerId));
        } catch (Exception e) {
            log.error("Error in WebSocket connection handling", e);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectionListener(SessionDisconnectEvent event) {
        try {
            StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
            String sessionId = headerAccessor.getSessionId();
            String playerId = (String) headerAccessor.getSessionAttributes().get("playerId");
            String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");
            
            log.info("WebSocket disconnection - SessionId: {}, PlayerId: {}, RoomId: {}", 
                    sessionId, playerId, roomId);

            if (roomId != null && playerId != null) {
                roomService.disconnect(UUID.fromString(roomId), Long.valueOf(playerId));
            }
        } catch (Exception e) {
            log.error("Error in WebSocket disconnection handling", e);
        }
    }
}
