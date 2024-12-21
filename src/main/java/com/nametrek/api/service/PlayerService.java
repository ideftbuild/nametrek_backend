package com.nametrek.api.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import com.nametrek.api.dto.PlayerDto;
import com.nametrek.api.exception.ObjectNotFoundException;
import com.nametrek.api.model.Player;

/**
 * Service for performing CRUD operations on player entities.
 */
@Service
@Slf4j
public class PlayerService {

    private RedisService redisService;
    private String keyFormat = "rooms:%s:players";

    @Autowired
    public PlayerService(RedisService redisService) {
        this.redisService = redisService;
    }

    /**
     * Create a new player
     *
     * @param username player's username
     *
     * @return The newly created player
     */
    public Player create(String username) {
        Player player = new Player(username);
        save(player);
        return player;
    }

    /**
     * Create a new player
     *
     * @param username player's username
     * @param roomId the room id
     *
     * @return The newly created player
     */
    public Player create(String username, String roomId) {
        Player player = new Player(username, roomId);
        save(player);
        return player;
    }

    /**
     * Save a player 
     *
     * @param player The player
     */
	public void save(Player player) {
		this.redisService.setField("players", player);
	}

    /**
     * Retrieve a player from the hash that maps to the key
     *
     * @param id The key that mapped to the player
     */
    public Player get(String id) {
        Player player = (Player) this.redisService.getField("players", id);
        if (player == null) {
            throw new ObjectNotFoundException("Player with id: " + id + " not found");
        }
        return player;
    }

    /**
     * Delete a player
     *
     * @param id the key of the field to delete
     */
    public void delete(String id) {
        if (redisService.deleteField("players", id) == 0) {
            throw new ObjectNotFoundException("Player wtih id: " + id + " not found");
        }
    }

    /**
     * Update player fields
     *
     * @param updatedPlayer Update a player accross the collections
     */
    public void persistUpdate(Player updatedPlayer) {
        Player originalPlayer = get(updatedPlayer.getId());

        redisService.updateSortedSetMemberAndHash(
				String.format(keyFormat, updatedPlayer.getRoomId()),
				"players",
				originalPlayer, 
				updatedPlayer);
    }

    /**
     * Increment a player score by the score step
     *
     * @param id the player id
     * @param step the step to increment by
     */
    public void incrementScore(String id, Integer step) {
        // set default value for step
        if (step == null) {
            step = 10;
        }
        Player player = get(id);
        player.incrementScore(step);
        persistUpdate(player);
    }

    /**
     * Update a player username
     *
     * @param id player id
     * @param username player username
     */
    public void updateUsername(String id, String username) {
        Player player = get(id);
        player.setUsername(username);
        persistUpdate(player);
    }

    /**
     * Update a player field(s) using the dto passed
     *
     * @param player the player to update
     * @param playerDto the dto containing the updated fiels
     */
    public void updateFromDto(Player player, PlayerDto playerDto) {
        Optional.ofNullable(playerDto.getUsername()).ifPresent(player::setUsername);
        Optional.ofNullable(playerDto.getScore()).ifPresent(player::setScore);
        persistUpdate(player);
    }

	/**
	 * Delete a player 
	 *
	 * @param roomId The room id
	 * @param player The player
	 */
    public void persistDelete(String roomId, Player player) {

        redisService.deleteMemberFromSortedSetAndHash(
                String.format(keyFormat, roomId), "players", player);
    }

	/**
	 * Retrieves the list of players in a specified room, sorted in the given order.
	 *
	 * @param order  the sorting order, either "asc" for ascending or "desc" for descending
	 * @param roomId the unique identifier of the room
     *
     * @return the list of players in descending or ascending order
	 */
    public List<Player> getPlayersOrderBy(String order, String roomId) {
        return redisService.getSortedSet(order, String.format(keyFormat, roomId))
            .stream()
            .map(obj -> (Player) obj)
            .collect(Collectors.toList());
    }
}
