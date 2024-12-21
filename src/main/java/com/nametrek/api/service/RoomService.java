package com.nametrek.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nametrek.api.dto.PlayerDto;
import com.nametrek.api.dto.RoomDto;
import com.nametrek.api.dto.RoomEventResponse;
import com.nametrek.api.exception.ObjectNotFoundException;
import com.nametrek.api.exception.RoomFullException;
import com.nametrek.api.model.Player;
import com.nametrek.api.model.Room;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for performing CRUD operations on room entities.
 */
@Service
@Slf4j
public class RoomService {

    private final RedisService redisService;
    private final String keyFormat = "rooms:%s:players";
    private PlayerService playerService;
    private NotificationService notificationService;


    @Autowired
    public RoomService(RedisService redisService, PlayerService playerService, NotificationService notificationService) {
        this.redisService = redisService;
        this.playerService = playerService;
        this.notificationService = notificationService;
    }
    
    private RoomEventResponse sendMessage(Room room, Player player, String eventType) {
        // full topic path -> /rooms/{id}/updates
        RoomEventResponse res = new RoomEventResponse(room, player, eventType, LocalDateTime.now().toString());
        notificationService.sendMessageToTopic("/" + room.getId() + "/updates", res);
        return res;
    }

    /**
     * Get a sorted set key from room id
     *
     * @param roomId the room id
     *
     * @return the sorted set key
     */
    private String setKey(String roomId) {
        return String.format(keyFormat, roomId);
    }

    /**
     * Determines whether a player is in a specific room by comparing the player's current room ID
     * with the given room ID.
     *
     * @param playerRoomId the ID of the room the player is currently in
     * @param roomId       the ID of the room to check against
     * @return true if the player is in the specified room, false otherwise
     */
    public boolean isPlayerInRoom(String playerRoomId, String roomId) {
        return playerRoomId.equals(roomId);
    }

    /**
     * Update a Room field such as it current round and number of active players
     *
     * @param roomId id of the room
     * @param roomDto Object use to update the room
     */
    public void update(String roomId, RoomDto roomDto) {
        Room room = get(roomId);

        Optional.ofNullable(roomDto.getCurrentRound()).ifPresent(room::setCurrentRound);
        Optional.ofNullable(roomDto.getActivePlayerCount()).ifPresent(room::setActivePlayerCount);

        save(room);
    }

    /**
     * Save Room
     */
    public void save(Room room) {
        redisService.setField("rooms", room);
    }

    /**
     * Get a room based on it key
     * @param key The key used to retrieve the room
     */
    public Room get(String key) {
        Room room = (Room) redisService.getField("rooms", key);
        if (room == null) {
            throw new ObjectNotFoundException("Room with " + key + " not found");
        }
        return room;
    }

    /**
     * Delete a Room
     *
     * @param id The key used to retrieve the room
     */
    public void delete(String id) {
        playerService.getPlayersOrderBy("ASC", id).forEach(player -> {
            playerService.persistDelete(id, player);
        });;

        if (redisService.deleteField("rooms", id) == 0) {
            throw new ObjectNotFoundException("Failed to delete Room with id: " + id);
        }

    }

	/**
	 * Creates a new room and adds the player who created it as the first member.
	 *
	 * @param username the name of the player creating the room
     *
     * @return A object containing the room, player, event type and timestamp of the room creation
	 */
    public RoomEventResponse create(String username) {
        Room room = new Room();
        
        Player player = playerService.create(username, room.getId());

        room.incrementPlayerCount();
        room.setOwner(player.getId());

        save(room);
        playerService.save(player);
        redisService.addToSortedSet(setKey(room.getId()), player, player.getScore());

        return new RoomEventResponse(room, player, "create", LocalDateTime.now().toString());
    }

    /**
     * Add Player to a Room
     *
     * @param roomId the room id
     * @param username the username of the player
     *
     * @return A object containing the room, player, event type and timestamp of the room creation
     */
    public RoomEventResponse addPlayerToRoom(String roomId, String username) {
        Room room = get(roomId);
        Player player = playerService.create(username, room.getId());

        room.incrementPlayerCount();
        save(room);
        redisService.addToSortedSet(setKey(roomId), player, player.getScore());

        return sendMessage(room, player, "join");
    }

    /**
     * Update a player attributes using a dto
     *
     * @param roomId the room id
     * @param playerId the player id
     * @param playerDto the object containing the updated fields
     */
    public void updatePlayer(String roomId, String playerId, PlayerDto playerDto) {
        Player player = playerService.get(playerId);
        Room room = get(roomId);

        if (!isPlayerInRoom(player.getRoomId(), roomId)) {
            throw new IllegalArgumentException("Player not in room");
        }
        playerService.updateFromDto(player, playerDto);

        sendMessage(room, player, "update") ;
    }

    /**
     * Remove player from a room
     *
     * @param roomId the room id
     * @param playerId the player id
     */
    public void removePlayerFromRoom(String roomId, String playerId) {
        Player player = playerService.get(playerId);
        Room room = get(roomId);

        if (!isPlayerInRoom(player.getRoomId(), roomId)) {
            throw new IllegalArgumentException("Player not in room");
        }
        room.decrementPlayerCount();
        save(room);
        redisService.deleteMemberFromSortedSetAndHash(setKey(roomId), "players", player);

        sendMessage(room, player, "leave");
    }
}
