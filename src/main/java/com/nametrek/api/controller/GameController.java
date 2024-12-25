package com.nametrek.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nametrek.api.dto.AnswerDto;
import com.nametrek.api.service.GameService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/game")
public class GameController {

    private GameService gameService;

    @Autowired
    public GameController (GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/rooms/{roomId}")
    public void startGame(@PathVariable String roomId, HttpServletRequest request) {
        String url = request.getRequestURI().toString();
        gameService.play(roomId, url);
    }

    @MessageMapping("/rooms/{roomId}/answer")
    public void getAnswer(@DestinationVariable String roomId, AnswerDto answerDto) {
        gameService.getCountDownService().stopCountDown(roomId);
        gameService.saveAnswer(answerDto);
    }
}
