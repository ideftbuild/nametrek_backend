package com.nametrek.api.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nametrek.api.dto.PlayerDto;
import com.nametrek.api.dto.UsernameDto;
import com.nametrek.api.exception.ObjectNotFoundException;
import com.nametrek.api.model.Player;
import com.nametrek.api.service.PlayerService;
import com.nametrek.api.service.RoomService;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/players")
@Slf4j
@Controller
public class PlayerController {

    private PlayerService playerService;
    

    @Autowired
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    /**
     * Retrive player
     *
     * @param id the id of the player
     *
     * @return the player structure on success otherwise not found (404) status code
     */
    @GetMapping("/{id}")
    public ResponseEntity<Player> get(@PathVariable String id) {
        try {
            return ResponseEntity.ok(playerService.get(id));
        } catch (ObjectNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create a player
     *
     * @param usernameDto dto containing the username
     *
     * @rturn the player structure on success 
     */
    @PostMapping("")
    public ResponseEntity<Player> create(@RequestBody UsernameDto usernameDto) {
        return ResponseEntity.status(HttpStatus.CREATED).
            body(playerService.create(usernameDto.getUsername()));
    }

    /**
     * Delete a player
     *
     * @param id the player id
     *
     * @return no content (404) stauts code
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        playerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
