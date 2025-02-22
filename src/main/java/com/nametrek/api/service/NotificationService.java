package com.nametrek.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.nametrek.api.dto.RoomEventResponse;
import com.nametrek.api.dto.EventType;
import com.nametrek.api.dto.PlayerDto;

/**
 * Service class for handling notifications.
 */
@Service
public class NotificationService {
    
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public NotificationService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Send message to a topic
     *
     * @param topic the topic to send the message to
     * @param message the message
     */
    public <T> void sendMessageToTopic(String topic, T message) {
        messagingTemplate.convertAndSend(topic, message);
    }
}
