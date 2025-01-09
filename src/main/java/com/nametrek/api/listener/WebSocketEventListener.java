package com.nametrek.api.listener;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.nametrek.api.service.RoomService;

@Service
public class WebSocketEventListener {

    private final RoomService roomService;

    @Autowired
    public WebSocketEventListener(RoomService roomService) {
        this.roomService = roomService;
    }

    @EventListener
    public void handleWebSocketConnectionListener(SessionConnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        String sessionId = headerAccessor.getSessionId();
        String playerId = headerAccessor.getNativeHeader("playerId").get(0);
        String roomId = headerAccessor.getNativeHeader("roomId").get(0);

        headerAccessor.getSessionAttributes().put("playerId", playerId);
        headerAccessor.getSessionAttributes().put("roomId", roomId);

        roomService.connect(sessionId, UUID.fromString(roomId), Long.valueOf(playerId));
    }

    @EventListener
    public void handleWebSocketDisconnectionListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String sessionId = headerAccessor.getSessionId();
        String playerId = (String) headerAccessor.getSessionAttributes().get("playerId");
        String roomId = (String) headerAccessor.getSessionAttributes().get("roomId");

        roomService.disconnect(UUID.fromString(roomId), Long.valueOf(playerId));
    }
}
