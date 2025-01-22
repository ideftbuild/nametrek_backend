package com.nametrek.api.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

import com.nametrek.api.dto.AnswerDto;
import com.nametrek.api.service.GameService;
import com.nametrek.api.service.RedisService;


@Controller
public class GameWebSocketController {
    private final GameService gameService;
    private final RedisService redisService;

    @Autowired
    public GameWebSocketController (GameService gameService, RedisService redisService) {
        this.gameService = gameService;
		this.redisService = redisService;
    }


    @MessageMapping("/game/{id}/answer")
    public void getAnswer(@DestinationVariable String id, @Payload AnswerDto answerDto) {
        UUID roomId = UUID.fromString(id);
        Long playerTurn = gameService.getPlayerTurn(roomId);

        if (playerTurn == answerDto.getPlayerId()) {
            gameService.getCountDownService().stopCountDown(roomId);
            gameService.saveAnswer(answerDto);
        } else {
            System.out.println("Not Player " + answerDto.getPlayerId() + " turn");
        }
    }
}
