package com.nametrek.api.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.mockito.internal.matchers.Any;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.cache.CacheProperties.Redis;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nametrek.api.dto.PlayerDto;
import com.nametrek.api.exception.ObjectNotFoundException;
import com.nametrek.api.model.Player;
import com.nametrek.api.model.Room;
import com.nametrek.api.repository.PlayerRepository;
import com.nametrek.api.service.PlayerService;

import com.nametrek.api.service.RedisService;

import lombok.extern.slf4j.Slf4j;

/**
 * Test the PlayerService Class
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
public class PlayerServiceTest {

    @Mock
    private RedisService redisService;

    @Mock
    private PlayerRepository playerRepository;

    private Player player;

    private Room room;

    @InjectMocks
    private PlayerService playerService;

    @BeforeEach
    public void setUp() {
        player = new Player("Akan");
        room = new Room(1);
        player.setRoom(room);
    }
    
    /**
     * Test the the method returns a player using the room
     */
    @Test
    public void testGetPlayerByRoom() {
        when(playerRepository.findByRoom(room)).thenReturn(Optional.of(player));

        Player player = playerService.getPlayerByRoom(room);

        verify(playerRepository).findByRoom(room);
        assertEquals(player, this.player);
    }

    /**
     * Test that the method saves the player object in the database
     */
    @Test
    public void testSave() {
        when(playerRepository.save(player)).thenReturn(player);
        Player player = playerService.save(this.player);

        verify(playerRepository).save(this.player);
        assertEquals(player, this.player);
    }

    /**
     * Verify that the method returns a player using it id
     */
    @Test
    public void testGetPlayerById() {
        Long playerId = player.getId();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));

        Player player = playerService.getPlayerById(playerId);
        
        // Ensure the calls are made
        verify(playerRepository).findById(playerId);
        assertEquals(player, this.player);
    }

    /**
     * Test that method checks if the player exists or not by returning a boolean indicating it existence
     */
    @Test
    public void testExistsById() {
        Long playerId = player.getId();

        when(playerRepository.existsById(playerId)).thenReturn(true);

        Boolean exists = playerService.existsById(playerId);

        verify(playerRepository).existsById(playerId);
        assertEquals(exists, true);
    }

    /**
     * Test that the method get and delete a player object from the database
     */
    @Test
    public void testDeleteAndGet() {
        Long playerId = player.getId();

        when(playerRepository.findById(playerId)).thenReturn(Optional.of(player));
        doNothing().when(playerRepository).delete(player);

        Optional<Player> player = playerService.deleteAndGet(playerId);

        verify(playerRepository).findById(playerId);
        verify(playerRepository).delete(this.player);

        assertEquals(player.get(), this.player);

    }

    /**
     * Verify that the method deletes a player using the room id
     */
    @Test
    public void testDeleteByRoomId() {
        UUID roomId = player.getRoom().getId();
        doNothing().when(playerRepository).deleteByRoomId(roomId);

        playerService.deleteByRoomId(roomId);

        verify(playerRepository).deleteByRoomId(roomId);
    }

    /**
     * Verify that the method creates a player session with an expiry time
     */
    @Test
    public void testCreatePlayerSession() {
        doNothing().when(redisService).setValueExp(any(), any(), any(), any());

        playerService.createPlayerSession(player.getRoom().getId(), player.getId());

        verify(redisService).setValueExp(any(), any(), any(), any());
    }

    /**
     * Test that the method increments player score in redis
     */
}
