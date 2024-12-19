package com.nametrek.api.service;

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

import com.nametrek.api.dto.PlayerDto;
import com.nametrek.api.exception.ObjectNotFoundException;
import com.nametrek.api.model.Player;
import com.nametrek.api.model.Room;
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

    private Player player;

    @InjectMocks
    private PlayerService playerService;

    @BeforeEach
    public void setUp() {
        this.player = new Player("username");
        log.info("id for the player: " + player.getId());
    }

    /**
     * Verify that the create method adds a new field to set hash
     */
    @Test
    public void testCreate() {
        doNothing().when(redisService).setField(any(), any());
        Player player = playerService.create("Akan");

        verify(redisService).setField(any(), any());
        assertEquals(player.getUsername(), "Akan");
    }

    /**
	 * Verify that the set method set a new field
     */
    @Test
    public void testSave() {
        doNothing().when(redisService).setField("players", this.player);
        playerService.save(player);

        verify(redisService).setField("players", this.player);
    }

    /**
	 * Test that the get method returns the player associated with a key
     */
    @Test
    public void testGet() {
        when(redisService.getField("players", "key")).thenReturn(this.player);

        playerService.get("key");

        verify(redisService).getField("players", "key");
    }

    /**
     * Test that the delete method removes the player from the hash
     */
    @Test
    public void testDelete(){
        when(redisService.deleteField("players", player.getId())).thenReturn(1L);

        playerService.delete(player.getId());

        verify(redisService).deleteField("players", player.getId());
    }

    @Test
    public void testDeleteThrowsException(){
        when(redisService.deleteField("players", player.getId())).thenReturn(0L);


        assertThrows(ObjectNotFoundException.class, () -> {
            playerService.delete(player.getId());
        });

        verify(redisService).deleteField("players", player.getId());
    }
    // @Test
    // public void testDelete() {
    //     Player player = new Player("username");
    //     Room room = new Room();
    //     String key = String.format("rooms:%s:players", room.getId());
    //
    //     when(redisService.deleteMemberFromSortedSet(key, player)).thenReturn(1L);
    //
    //     playerService.delete(room.getId(), player);
    //
    //     verify(redisService).deleteMemberFromSortedSet(key, player);
    // }
    //
    // @Test
    // public void tetDeleteThrowsAnException() {
    //     Player player = new Player("username");
    //     Room room = new Room();
    //     String key = String.format("rooms:%s:players", room.getId());
    //
    //     when(redisService.deleteMemberFromSortedSet(key, player)).thenReturn(0L);
    //
    //     assertThrows(ObjectNotFoundException.class, () -> {
    //        playerService.delete(room.getId(), player); 
    //     });
    //
    //     verify(redisService).deleteMemberFromSortedSet(key, player);
    // }

    // @Test
    // public void integration() {
    //     Room room = Room.builder().build();
    //
    //     Player player = new Player("74b914e5-c4ae-4c1c-b6be-bc724e83a712", "username", 100);
    //     // log.info("first room id: " + id);
    //     // log.info("second room id: " + id);
    //
    //     playerService.delete("c3150990-3f0a-4ce9-8bc9-45a249b02055", player);
    //     log.info("Players gotten" + playerService.getPlayersOrderBy("DESC", "c3150990-3f0a-4ce9-8bc9-45a249b02055"));
    // }
}


