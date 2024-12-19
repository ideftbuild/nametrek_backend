package com.nametrek.api.controller;

import java.security.InvalidKeyException;
import java.util.List;
import java.util.Set;

import org.apache.catalina.connector.Response;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

import com.nametrek.api.dto.RoomPlayerResponse;
import com.nametrek.api.dto.RoomPlayerResponse;
import com.nametrek.api.dto.PlayerDto;
import com.nametrek.api.dto.RoomDto;
import com.nametrek.api.dto.UsernameDto;
import com.nametrek.api.exception.ObjectNotFoundException;
import com.nametrek.api.service.RoomPlayerService;
import com.nametrek.api.exception.RoomFullException;
import com.nametrek.api.model.Player;
import com.nametrek.api.model.Room;
import com.nametrek.api.service.RoomService;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;


/**
 * Controller class for managing rooms and player interactions, 
 * providing endpoints for CRUD operations.
 */
@RequestMapping("/rooms")
@RestController
public class RoomController {


    private RoomService roomService;
    private RoomPlayerService roomPlayerService;

    @Autowired
    public RoomController(RoomService roomService, RoomPlayerService roomPlayerService) {
        this.roomService = roomService;
        this.roomPlayerService = roomPlayerService;
    }

    /**
     * Retrieve a room
     *
     * @param id the id of the room
     *
     * @return the room object otherwise not found (404) status code
     */
    @GetMapping("/{id}")
    public ResponseEntity<Room> get(@PathVariable String id) {
        try {
            return ResponseEntity.ok(roomService.get(id));
        } catch (ObjectNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Create a room
     *
     * @param usernameDto the username of the player
     * 
     * @return the newly created room with a status code of 201 
     */
    @PostMapping("")
    public ResponseEntity<RoomPlayerResponse> create(@Valid @RequestBody UsernameDto usernameDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                roomPlayerService.create(usernameDto.getUsername()));
    }

    /**
     * Update a room
     *
     * @param id the id of ther oom
     * @param roomDto fields to object the roomDto with
     *
     * @return A message on success otherwise a not found (404) status code
     */
    @PutMapping("/{id}")
    public ResponseEntity<String> updateRoom(@PathVariable String id, @RequestBody RoomDto roomDto) {
        try {
            roomService.update(id, roomDto);
            return ResponseEntity.ok("Room with " + id + " updated");
        } catch (ObjectNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room not found");
        }
    }

    /**
     * Update player fields 
     *
     * @param roomId the id of the room
     * @param playerId the id of the player 
     * @param playerDto contains the updated fields
     * 
     * @return A message on success otherwise a bad request (401) status code
     */
    @PutMapping("/{roomId}/players/{playerId}")
    public ResponseEntity<RoomPlayerResponse> updatePlayer(
            @PathVariable String roomId, 
            @PathVariable String playerId,
            @Valid @RequestBody PlayerDto playerDto) {
        try {
            return ResponseEntity.ok(roomPlayerService.updatePlayer(roomId, playerId, playerDto));
        } catch (ObjectNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Remove player from a room
     *
     * @param roomId the room id
     * @param playerId the player id
     *
     * @return A message on success otherwise not found (401) status code
     */
    @DeleteMapping("/{roomId}/players/{playerId}")
    public ResponseEntity<String> removePlayerFromRoom(@PathVariable String roomId, @PathVariable String playerId) {
        try {
            roomPlayerService.deletePlayer(roomId, playerId);
            return ResponseEntity.ok("Player with " + playerId + " deleted");
        } catch (ObjectNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Player or Room not found");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Player not in room " + roomId);
        } 
    }

    /**
     * Add player to a room
     *
     * @param roomId the room id
     * @param usernameDto dto containing the username
     *
     * @return The RoomPlayerResponse otherwise forbidden (404) status code
     */
    @PutMapping("/{id}/join")
    public ResponseEntity<RoomPlayerResponse> addPlayerToRoom(@PathVariable String id, @Valid @RequestBody UsernameDto usernameDto) {
        try {
            return ResponseEntity.ok(
                    roomPlayerService.addPlayerToRoom(id, usernameDto.getUsername()));
        } catch (RoomFullException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * Retrieves players in ascending or descending order.
     *
     * @param sort the sort order (ascending or descending)
     * @param id the ID of the room
     * @return a list of players
     */
    @GetMapping("/{roomId}/players")
    public  ResponseEntity<List<Player>> getPlayers(@PathVariable String roomId, @RequestParam String sort) {
        sort = sort.toUpperCase().trim();
        if (!sort.equals("DESC")) {
            sort = "ASC";
        }
        return ResponseEntity.ok(roomPlayerService.getPlayersOrderBy(sort, roomId));
    }
}
