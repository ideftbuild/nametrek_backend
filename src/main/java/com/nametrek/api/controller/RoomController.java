package com.nametrek.api.controller;

import java.security.InvalidKeyException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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

import com.nametrek.api.dto.CreateRoomDto;
import com.nametrek.api.dto.JoinRoomDto;
import com.nametrek.api.dto.PlayerDto;
import com.nametrek.api.dto.RoomDto;
import com.nametrek.api.dto.RoomEventResponse;
import com.nametrek.api.dto.RoomPlayerInfo;
import com.nametrek.api.dto.PlayerNameDto;
import com.nametrek.api.exception.ObjectNotFoundException;
import com.nametrek.api.exception.RoomEmptyException;
import com.nametrek.api.exception.RoomFullException;
import com.nametrek.api.model.Player;
import com.nametrek.api.model.Room;
import com.nametrek.api.service.PlayerService;
import com.nametrek.api.service.RoomService;
import com.nametrek.api.utils.CookieUtil;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;


/**
 * Controller class for managing rooms and player interactions,
 * providing endpoints for CRUD operations.
 */
@RequestMapping("/rooms")
@RestController
@Slf4j
public class RoomController {


    private final RoomService roomService;

    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }
    //
    // /**
    //  * Retrieve a room
    //  *
    //  * @param id the id of the room
    //  *
    //  * @return the room object otherwise not found (404) status code
    //  */
    // @GetMapping("/{id}")
    // public ResponseEntity<Room> get(@PathVariable String id) {
    //     try {
    //         return ResponseEntity.ok(roomService.get(id));
    //     } catch (ObjectNotFoundException e) {
    //         return ResponseEntity.notFound().build();
    //     }
    // }
    //
    /**
     * Create a room
     *
     * @param createRoomDto dto containing the username of the player creating the room
     * and number of rounds to play
     *
     * @return the newly created room with a status code of 201
     */
    @PostMapping("")
    public ResponseEntity<RoomPlayerInfo> create(
            @Valid @RequestBody CreateRoomDto createRoomDto,
            HttpServletResponse response) {

        RoomPlayerInfo roomPlayerInfo = roomService.create(createRoomDto.getPlayerName(), createRoomDto.getRounds());
        CookieUtil.addCookie(response, "player_id", roomPlayerInfo.getPlayer().getId().toString());
        return ResponseEntity.status(HttpStatus.CREATED).body(roomPlayerInfo);
    }

    /**
     * Add player to a room
     *
     * @param roomId the room id
     * @param playerNameDto dto containing the username
     *
     * @return The RoomPlayerResponse otherwise forbidden (404) status code
     */
    @PutMapping("/{roomId}/join")
    public ResponseEntity<RoomPlayerInfo> joinById(
            @PathVariable String roomId,
            @Valid @RequestBody PlayerNameDto playerNameDto,
            HttpServletResponse response
     ) {
        try {
            RoomPlayerInfo roomPlayerInfo = roomService.joinRoomById(UUID.fromString(roomId), playerNameDto.getPlayerName());
            CookieUtil.addCookie(response, "player_id", roomPlayerInfo.getPlayer().getId().toString());
            return ResponseEntity.status(HttpStatus.CREATED).body(roomPlayerInfo);
        } catch (RoomFullException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ObjectNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Add player to a room
     *
     * @param joinRoomDto containing the username
     *
     * @return The RoomPlayerResponse otherwise forbidden (404) status code
     */
    @PutMapping("/join")
    public ResponseEntity<RoomPlayerInfo> joinByCode(@Valid @RequestBody JoinRoomDto joinRoomDto, HttpServletResponse response) {
        try {
            RoomPlayerInfo roomPlayerInfo = roomService.joinRoomByCode(joinRoomDto.getRoomCode(), joinRoomDto.getPlayerName());
            CookieUtil.addCookie(response, "player_id", roomPlayerInfo.getPlayer().getId().toString());
            return ResponseEntity.status(HttpStatus.CREATED).body(roomPlayerInfo);
        } catch (RoomFullException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ObjectNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{roomId}/players/me")
    public ResponseEntity<PlayerDto> getPlayer(@PathVariable String roomId, HttpServletRequest request) {
        String playerId = CookieUtil.getCookieValue(request, "player_id");
        log.info("Player id is: {}", playerId);
        try {
            if (playerId == null) {
                log.debug("Player session is empty");
                throw new ObjectNotFoundException("Player session is empty");
            }
            Player player = roomService.getPlayer(UUID.fromString(roomId), Long.valueOf(playerId));
            log.info("Player {} found in room {}", player.getName(), roomId);
            return ResponseEntity.ok(new PlayerDto(player.getId(), player.getName(), player.getScore(), false));
        } catch (ObjectNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{roomId}/missed-update")
    public ResponseEntity<RoomEventResponse> getRoomUpdate(@PathVariable String roomId, HttpServletRequest request) {
        String playerId = CookieUtil.getCookieValue(request, "player_id");
        try {
            if (playerId == null) {
                throw new ObjectNotFoundException("Player session is empty");
            }
            return ResponseEntity.ok(roomService.getRoomUpdate(UUID.fromString(roomId), Long.valueOf(playerId)));
        } catch (ObjectNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // /**
    //  * Delete a room
    //  *
    //  * @param id the room id
    //  */
    // @DeleteMapping("/{id}")
    // public ResponseEntity<Void> deleteRoom(@PathVariable String id) {
    //     try {
    //         roomService.delete(id);
    //         return ResponseEntity.noContent().build();
    //     } catch (ObjectNotFoundException e) {
    //         return ResponseEntity.notFound().build();
    //     }
    // }
}
