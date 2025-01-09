package com.nametrek.api.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nametrek.api.dto.AnswerDto;
import com.nametrek.api.service.GameService;
import com.nametrek.api.utils.RedisKeys;

@Controller
public class GameWebSocketController {
    private final GameService gameService;

    @Autowired
    public GameWebSocketController (GameService gameService) {
        this.gameService = gameService;
    }


    @MessageMapping("/game/{roomId}/answer")
    public void getAnswer(@DestinationVariable String roomId, @Payload AnswerDto answerDto) {
        System.out.println("Answer received safely: " + answerDto);
        UUID roomIdUUID = UUID.fromString(roomId);
        Long playerTurn = 
            Long.valueOf(gameService.redisService.getField(RedisKeys.formatRoomKey(roomIdUUID), RedisKeys.PLAYER_TURN).toString());

        if (playerTurn == answerDto.getPlayerId()) {
            gameService.getCountDownService().stopCountDown(roomIdUUID);
            gameService.saveAnswer(answerDto);
        } else {
            System.out.println("Not Player " + answerDto.getPlayerId() + " turn");
        }
    }
}
