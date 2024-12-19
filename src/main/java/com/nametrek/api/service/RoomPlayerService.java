package com.nametrek.api.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.management.Notification;
import javax.management.openmbean.InvalidKeyException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.SerializationUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nametrek.api.dto.RoomEventResponse;
import com.nametrek.api.dto.RoomPlayerResponse;
import com.nametrek.api.dto.PlayerDto;
import com.nametrek.api.dto.UsernameDto;
import com.nametrek.api.exception.ObjectNotFoundException;
import com.nametrek.api.exception.RoomFullException;
import com.nametrek.api.model.Player;
import com.nametrek.api.model.Room;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing players in rooms, including adding, removing, and other related operations.
 */
@Service
@Slf4j
public class RoomPlayerService {

    private String keyFormat = "rooms:%s:players";
    private RedisService redisService;
    private RoomService roomService;
    private PlayerService playerService;
	private ObjectMapper objectMapper;
	private final NotificationService notificationService;

    @Autowired
    public RoomPlayerService(
            RedisService redisService,
            RoomService roomService,
            PlayerService playerService,
			NotificationService notificationService) {
        this.redisService = redisService;
        this.roomService = roomService;
        this.playerService = playerService;
		this.notificationService = notificationService;
		this.objectMapper = new ObjectMapper();
    }
    
    private RoomEventResponse createEventResponse(Player player, String type) {
        return new RoomEventResponse(player, type, LocalDateTime.now().toString());
    }

	/**
	 * Creates a new room and adds the player who created it as the first member.
	 *
	 * @param username the name of the player creating the room
	 */
    public RoomPlayerResponse create(String username) {
        Player player = new Player(username);
        Room room = new Room(1, player.getId());

        player.setRoomId(room.getId());

        roomService.save(room);
        playerService.save(player);
        redisService.addToSortedSet(String.format(keyFormat, room.getId()), player, player.getScore());

        return new RoomPlayerResponse(player, room);
    }

    /**
     * Update player fields
     *
     * @param playerDto A DTO containing the update fields
     */
    public RoomPlayerResponse updatePlayer(String roomId, String playerId, PlayerDto playerDto) {
        Player originalPlayer = playerService.get(playerId);
		if (!originalPlayer.getRoomId().equals(roomId)) {
			throw new IllegalArgumentException("Player not in room " + roomId);
		}

        Room room = roomService.get(roomId);
        if (room == null) {
            throw new ObjectNotFoundException("Room with " + roomId + " not found");
        }

		Player updatedPlayer = originalPlayer.deepCopy(objectMapper);

        Optional.ofNullable(playerDto.getUsername()).ifPresent(updatedPlayer::setUsername);
        Optional.ofNullable(playerDto.getScore()).ifPresent(updatedPlayer::setScore);

        redisService.updateSortedSetMemberAndHash(
				String.format(keyFormat, roomId),
				"players",
				originalPlayer, 
				updatedPlayer);

		notificationService.sendMessageToTopic("/rooms/" + roomId, createEventResponse(updatedPlayer, "update"));
        return new RoomPlayerResponse(updatedPlayer, room);
    }

	/**
	 * Retrieves the list of players in a specified room, sorted in the given order.
	 *
	 * @param order  the sorting order, either "asc" for ascending or "desc" for descending
	 * @param roomId the unique identifier of the room
	 */
    public List<Player> getPlayersOrderBy(String order, String roomId) {
        return redisService.getSortedSet(order, String.format(keyFormat, roomId))
            .stream()
            .map(obj -> (Player) obj)
            .collect(Collectors.toList());
    }

    /**
     * Add Player to a Room
     *
     * @param roomId The room id
     * @param playerDto Player Dto
     */
    public RoomPlayerResponse addPlayerToRoom(String roomId, String username) {
        Room room = roomService.get(roomId);
        Player player = new Player(username, room.getId());

        Integer count = room.getActivePlayerCount();
        if (count >= 4) {
            throw new  RoomFullException("The room is full. No more players can join");
        }

        room.setActivePlayerCount(count + 1);

        roomService.save(room);
        playerService.save(player);
        redisService.addToSortedSet(String.format(keyFormat, roomId), player, player.getScore());

		notificationService.sendMessageToTopic("/rooms/" + roomId, createEventResponse(player, "join"));
        return new RoomPlayerResponse(player, room);
    }

	/**
	 * Delete a player 
	 *
	 * @param roomId The room id
	 * @param playerId The player id
	 */
    public void deletePlayer(String roomId, String playerId) {
		Player player = playerService.get(playerId);

		if (!player.getRoomId().equals(roomId)) {
			throw new IllegalArgumentException("Player not in room " + roomId);
		}

        redisService.deleteMemberFromSortedSetAndHash(
                String.format(keyFormat, roomId), "players", player);

		Room room = roomService.get(roomId);
		room.setActivePlayerCount(room.getActivePlayerCount() - 1);
		roomService.save(room);

		notificationService.sendMessageToTopic("/rooms/" + roomId, createEventResponse(player, "leave"));
    }
}
