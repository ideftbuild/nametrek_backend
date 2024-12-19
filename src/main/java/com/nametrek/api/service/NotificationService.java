package com.nametrek.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

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
        messagingTemplate.convertAndSend("/game" + topic, message);
    }
}
