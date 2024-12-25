package com.nametrek.api.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private NotificationService notificationService;

    /**
     * Verify that method sends message to a topic
     */
    @Test
    public void testSendMessageToTopic() {
        doNothing().when(messagingTemplate).convertAndSend("/room/1", "Hello");

        notificationService.sendMessageToTopic("/room/1", "Hello");

        verify(messagingTemplate).convertAndSend("/room/1", "Hello");
    }
}
