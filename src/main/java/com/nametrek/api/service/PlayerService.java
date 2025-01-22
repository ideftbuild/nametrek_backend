package com.nametrek.api.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import com.nametrek.api.dto.PlayerDto;
import com.nametrek.api.exception.ObjectNotFoundException;
import com.nametrek.api.model.Player;
import com.nametrek.api.model.PlayerSession;
import com.nametrek.api.model.Room;
import com.nametrek.api.model.Player.EventType;
import com.nametrek.api.repository.PlayerRepository;
import com.nametrek.api.utils.RedisKeys;

import jakarta.transaction.Transactional;

/**
 * Service for performing CRUD operations on player entities.
 */
@Service
@Slf4j
public class PlayerService {

    private PlayerRepository playerRepository;
    public final long WAIT_TIME_MINUTES = 5;
    private final RedisService redisService;
    private final Integer scoreUnit = 10;

    @Autowired
    public PlayerService(PlayerRepository playerRepository, RedisService redisService) {
        this.playerRepository = playerRepository;
        this.redisService = redisService;
    }

    public Player getPlayerByRoom(Room room) {
        return playerRepository.findByRoom(room).orElse(null);
    }

    public List<Player> getPlayerByStatus(Room room, EventType status) {
        return playerRepository.findByRoomAndStatus(room, status);
    }

    public Player save(Player player) {
        return playerRepository.save(player);
    }

    public Player getPlayerById(Long id) {
        Player player = playerRepository.findById(id).orElse(null);
        if (player == null) {
            throw new ObjectNotFoundException("Player doesn't exists");
        }
        return player;
    }

    public boolean existsById(Long id) {
        return playerRepository.existsById(id);
    }

    public Optional<Player> deleteAndGet(Long id) {
        Optional<Player> player = playerRepository.findById(id);
        player.ifPresent(playerRepository::delete);
        return player;
    }

    @Transactional
    public void deleteByRoomId(UUID roomId) {
        playerRepository.deleteByRoomId(roomId);
    }

    public void createPlayerSession(UUID roomId, Long playerId) {
        PlayerSession session = new PlayerSession(
                playerId, 
                roomId, 
                System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(WAIT_TIME_MINUTES));  // store expiry time
        redisService.setValueExp(RedisKeys.formatPlayerSessionKey(playerId), session, WAIT_TIME_MINUTES, TimeUnit.MINUTES);
    }

    public void incrementScore(UUID roomId, Long playerId, Double delta) {
        redisService.incrementValue(RedisKeys.formatInGamePlayersKey(roomId), playerId, delta);
    }

    public String getName(UUID roomId, Long playerId) {
        return (String) redisService.getField(RedisKeys.formatRoomKey(roomId), RedisKeys.formatPlayerNameKey(playerId));
    }

    public Queue<Long> getInGamePlayersIds(String order, UUID roomId) {
        return redisService.getSortedSet(order, RedisKeys.formatInGamePlayersKey(roomId))
            .stream()
            .map(obj ->  Long.valueOf(obj.toString()))
            .collect(Collectors.toCollection(ArrayDeque::new));
    }

    public List<PlayerDto> getPlayers(String order, UUID roomId) {
        // Fetch the sorted set of player IDs and their scores
        Set<ZSetOperations.TypedTuple<Object>> playerIdsWithScores = 
            redisService.getSortedSetWithScores(order, RedisKeys.formatInGamePlayersKey(roomId));

        // Extract player IDs and scores from the sorted set
        List<Long> playerIds = new ArrayList<>();
        List<Double> scores = new ArrayList<>();
        for (ZSetOperations.TypedTuple<Object> obj : playerIdsWithScores) {
            playerIds.add(((Number) obj.getValue()).longValue());
            scores.add(obj.getScore());  // Score
        }

        // Generate the keys for the player's name and lost status fields
        List<Object> fields = new ArrayList<>();
        for (Long playerId : playerIds) {
            fields.addAll(List.of(RedisKeys.formatPlayerNameKey(playerId), RedisKeys.formatPlayerLostStatus(playerId)));
        }


        List<Object> fieldValues = redisService.getFields(RedisKeys.formatRoomKey(roomId), fields);
        // Map the values to PlayerDto objects
        List<PlayerDto> players = new ArrayList<>();
        for (int i = 0; i < playerIds.size(); i++) {
            Long playerId = playerIds.get(i);
            String name = (String) fieldValues.get(i * 2); // name
            Boolean lost = (Boolean) fieldValues.get(i * 2 + 1); // lost status
            Double score = scores.get(i) * scoreUnit; // score from the sorted set
            players.add(new PlayerDto(Long.valueOf(playerId), name, score, lost));
        }

        return players;
    }

    public boolean isInGame(UUID roomId, Long playerId) {
        return redisService.getMemberScore(RedisKeys.formatInGamePlayersKey(roomId), playerId) != null;
    }
    // public List<PlayerDto> getPlayers(String order, UUID roomId) {
    //     return redisService.getSortedSetWithScores(order, )
    //         .stream()
    //         .map(obj ->  {
    //             Long playerId = Long.valueOf(obj.getValue().toString());
    //             String name = (String) redisService.getField(RedisKeys.formatRoomKey(roomId), 
    //                             RedisKeys.formatPlayerNameKey(playerId));
    //             Boolean lost = (Boolean) redisService.getField(RedisKeys.formatRoomKey(roomId),
    //                     RedisKeys.formatPlayerLostStatus(playerId));
    //             return new PlayerDto(playerId, name, obj.getScore(), lost);
    //         })
    //         .collect(Collectors.toList());
    // }
}
