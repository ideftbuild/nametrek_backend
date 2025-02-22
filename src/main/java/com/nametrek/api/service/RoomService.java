package com.nametrek.api.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.yaml.snakeyaml.events.Event;

import com.nametrek.api.dto.RoomDto;
import com.nametrek.api.dto.RoomEventResponse;
import com.nametrek.api.dto.RoomPlayerInfo;
import com.nametrek.api.exception.ObjectNotFoundException;
import com.nametrek.api.exception.RoomFullException;
import com.nametrek.api.model.Player;
import com.nametrek.api.dto.EventType;
import com.nametrek.api.dto.PlayerDto;
import com.nametrek.api.model.PlayerSession;
import com.nametrek.api.model.Room;
import com.nametrek.api.repository.PlayerRepository;
import com.nametrek.api.repository.RoomRepository;
import com.nametrek.api.utils.CodeGenerator;
import com.nametrek.api.utils.RedisKeys;

import lombok.extern.slf4j.Slf4j;

/**
 * Service for performing CRUD operations on room entities.
 */
@Service
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;
    private final PlayerService playerService;
    private final RedisService redisService;
    private static final String roomTopic = "/rooms/";
    private final NotificationService notificationService;

    @Autowired
    public RoomService(RoomRepository roomRepository,
            PlayerService playerService,
            RedisService redisService,
            NotificationService notificationService) {
        this.roomRepository = roomRepository;
        this.playerService = playerService;
        this.redisService = redisService;
        this.notificationService = notificationService;
    }

    public Player getOwner(Room room) {
        return playerService.getPlayerByRoom(room);
    }

    public Room getRoomByCode(String code) {
        return roomRepository.findByCode(code).orElseThrow(() -> new ObjectNotFoundException("Room doesn't exists"));
    }

    public boolean existsById(UUID roomId) {
        return roomRepository.existsById(roomId);
    }

    @Transactional
    public RoomPlayerInfo create(String playerName, Integer rounds) {
        Room room = new Room(rounds);
        room = roomRepository.save(room);

        Player owner = new Player(playerName, room);
        owner.activate();
        owner = playerService.save(owner);

        String roomKey = RedisKeys.formatRoomKey(room.getId());
        Map<String, Object> fields = new HashMap<>();
        fields.put(RedisKeys.ROUND, 0);
        fields.put(RedisKeys.OWNER, owner.getId());
        fields.put(RedisKeys.formatPlayerNameKey(owner.getId()), owner.getName());

        redisService.setFields(roomKey, fields);
        // Add player to room sorted set
        addPlayerToRoom(room.getId(), owner.getId(), 0d);

        return new RoomPlayerInfo(room.getId(), room.getCode(), new PlayerDto(owner.getId(), owner.getName(), owner.getScore(), false));
    }

    @Transactional
    public Player getPlayer(UUID roomId, Long playerId) {
        if (!roomRepository.existsById(roomId)) {
            throw new ObjectNotFoundException("Room doesn't exists");
        }
        Player player = playerService.getPlayerById(playerId);
        if (!player.getRoom().getId().equals(roomId)) {
            throw new IllegalArgumentException("Player not in room");
        }

        return player;
    }

    public Room getRoomById(UUID id) {
        Room room = roomRepository.findById(id).orElse(null);
        if (room == null) {
            throw new ObjectNotFoundException("Room doesn't exists");
        }
        return room;
    }
    
    public void addPlayerToRoom(UUID roomId, Long playerId, Double score) {
        redisService.addToSortedSet(RedisKeys.formatInGamePlayersKey(roomId), playerId, score);
    }
    
    private EventType handleReconnection(String disconnectedSessionKey, UUID roomId, Long playerId) {
        redisService.delete(disconnectedSessionKey);
        addPlayerToRoom(roomId, playerId, playerService.getPlayerById(playerId).getScore());
        return EventType.RECONNECT;
    }

    private EventType handleJoining(UUID roomId, Long playerId) {
        addPlayerToRoom(roomId, playerId, 0d);
        return EventType.JOIN;
    }

    @Transactional
    public void connect(String sessionId, UUID roomId, Long playerId) {
        Room room = getRoomById(roomId);
        String roomKey = RedisKeys.formatRoomKey(roomId);
        if (Boolean.TRUE.equals((Boolean) redisService.getField(roomKey, RedisKeys.IN_GAME))) {
            throw new ObjectNotFoundException("A round is currently ongoing. Please wait for the next round.");
        }
        String disconnectedSessionKey = RedisKeys.formatPlayerSessionKey(playerId);
        EventType eventType = redisService.keyExists(disconnectedSessionKey) 
            ? handleReconnection(disconnectedSessionKey, roomId, playerId) 
            : handleJoining(roomId, playerId);

        Integer round = (Integer) redisService.getField(roomKey, RedisKeys.ROUND);
        List<PlayerDto> players = playerService.getPlayers("DESC", roomId);



        notificationService.sendMessageToTopic(
                roomTopic + roomId,
                new RoomEventResponse(playerId, players, eventType));
    }


    @Transactional
    public void disconnect(UUID roomId, Long playerId) {
        CompletableFuture.runAsync(() -> {
            System.out.println("Control is in disconnect method");
            if (!roomRepository.existsById(roomId)) {
                throw new ObjectNotFoundException("Room doesn't exists");
            }
            Player player = playerService.getPlayerById(playerId);
            playerService.createPlayerSession(roomId, playerId);

            String inGamePlayersKey = RedisKeys.formatInGamePlayersKey(roomId);
            Double score = redisService.getMemberScore(inGamePlayersKey, playerId);
            if (score != null) {
                player.setScore(score);
                playerService.save(player);  // persist player score
            }
            redisService.deleteMemberFromSortedSet(inGamePlayersKey, playerId);
            
            notificationService.sendMessageToTopic(
                    roomTopic + roomId,
                    new RoomEventResponse(playerId, playerService.getPlayers("DESC", roomId), EventType.DISCONNECT));
            }
        );
    }

    @Transactional
    private RoomPlayerInfo joinRoom(Room room, String playerName) {

        if (redisService.sortedSetLength(RedisKeys.formatInGamePlayersKey(room.getId())) >= room.getCapacity()) {
            throw new RoomFullException("Room is full");
        }
        Player player = new Player(playerName, room);
        player.activate();
        player = playerService.save(player);

        redisService.setField
            (RedisKeys.formatRoomKey(room.getId()),
             RedisKeys.formatPlayerNameKey(player.getId()),
             player.getName());
        return new RoomPlayerInfo(room.getId(), new PlayerDto(player.getId(), player.getName(), player.getScore(), false));
    }
	//
    /**
     * Add Player to a Room using an id
     *
     * @param roomId the room id
     * @param playerName the name of the player
     *
     * @return A object containing the room, player, event type and timestamp of the room creation
     */
    public RoomPlayerInfo joinRoomById(UUID roomId, String playerName) {
        return joinRoom(getRoomById(roomId), playerName);
    }

    /**
     * Add Player to a Room using the code
     *
     * @param roomId the room id
     * @param playerName the name of the player
     *
     * @return A object containing the room, player, event type and timestamp of the room creation
     */
    public RoomPlayerInfo joinRoomByCode(String roomCode, String playerName) {
        return joinRoom(getRoomByCode(roomCode), playerName);
    }

	@Transactional
	public void deleteRoom(UUID roomId) {
		playerService.deleteByRoomId(roomId);
		roomRepository.deleteById(roomId);
	}
 //   
 //    /**
 //     *
 //     * Update a player attributes using a dto
 //     *
 //     * @param roomId the room id
 //     * @param playerId the player id
 //     * @param playerDto the object containing the updated fields
 //     */
 //    public void updatePlayer(String roomId, String playerId, PlayerDto playerDto) {
 //        Player player = playerService.get(playerId);
 //        Room room = get(roomId);
	//
 //        if (!player.isSameRoom(roomId)) {
 //            throw new IllegalArgumentException("Player not in room");
 //        }
 //        playerService.updateFromDto(player, playerDto);
	//
 //        notificationService.sendRoomEvent(room, player, EventType.UPDATE);
 //    }

    /**
     * Remove player and persist accross room
     * @param roomId the room id
     * @param playerId the player id
     */
    public void leave(UUID roomId, Long playerId) {
        Player player = playerService.deleteAndGet(playerId).orElse(null);

        // remove player name mapping
        String roomKey = RedisKeys.formatRoomKey(roomId);
        redisService.deleteField(roomKey, RedisKeys.formatPlayerNameKey(playerId));
        redisService.deleteField(roomKey, RedisKeys.formatPlayerLostStatus(playerId));

        if (redisService.sortedSetLength(RedisKeys.formatInGamePlayersKey(roomId)) <= 0) {
			deleteRoom(roomId);
            redisService.delete(roomKey);
        } else {
            if (player != null && player.getRoom().getId().equals(roomId)) {
                notificationService.sendMessageToTopic(
                        roomTopic + roomId,
                        new RoomEventResponse(playerId, playerService.getPlayers("DESC", roomId), EventType.LEAVE));
            }
        }
    }

    public RoomEventResponse getRoomUpdate(UUID roomId, Long playerId) {
        Room room = getRoomById(roomId);
        Player player = playerService.getPlayerById(playerId);
        if (!player.getRoom().getId().equals(roomId)) {
            throw new IllegalArgumentException("Player not in room");
        }
		String roomKey = RedisKeys.formatRoomKey(roomId);
        Integer round = (Integer) redisService.getField(roomKey, RedisKeys.ROUND);
        List<PlayerDto> players = playerService.getPlayers("DESC", roomId);
		Long owner = ((Number) (redisService.getField(roomKey, RedisKeys.OWNER))).longValue();

        return new RoomEventResponse(new RoomDto(round, room.getRounds(), room.getCapacity(), owner), players, EventType.GET);
    }
}
