package com.nametrek.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

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
     */
    public Player create(String username) {
        Player player = new Player(username);
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
     * @param key The key that mapped to the player
     */
    public Player get(String key) {
        Player player = (Player) this.redisService.getField("players", key);
        if (player == null) {
            throw new ObjectNotFoundException("Player with id: " + key + " not found");
        }
        return player;
    }

    /**
     * Delete a player
     *
     * @param key the key of the field to delete
     */
    public void delete(String key) {
        if (redisService.deleteField("players", key) == 0) {
            throw new ObjectNotFoundException("Player wtih id: " + key + " not found");
        }
    }
}
