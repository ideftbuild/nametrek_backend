package com.nametrek.api.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nametrek.api.service.GameService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/games")
public class GameRestController {

    private final GameService gameService;

    @Autowired
    public GameRestController (GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<Void> startGame(@PathVariable String roomId, HttpServletRequest request) {
        try {
            String url = request.getRequestURI().toString();
            gameService.play(UUID.fromString(roomId), url);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

}
