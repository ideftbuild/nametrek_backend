package com.nametrek.api.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nametrek.api.dto.PlayerDto;
import com.nametrek.api.dto.RoomDto;
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

    private RedisService redisService;

    @Autowired
    public RoomService(RedisService redisService) {
        this.redisService = redisService;
    }

    /**
     * Create a new Room
     */
    public Room create() {
        Room room = new Room();
        save(room);
        return room;
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
     * @param key The key used to retrieve the room
     */
    public void delete(String key) {
        if (redisService.deleteField("rooms", key) == 0) {
            throw new ObjectNotFoundException("Failed to delete Room with id: " + key);
        }
    }

}
