package com.nametrek.api.service;

import java.util.HashSet;
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

    private Room room;

    @InjectMocks
    private PlayerService playerService;

    @BeforeEach
    public void setUp() {
        player = new Player("Akan");
        room = new Room(1, player.getId());
        player.setRoomId(room.getId());
    }

    /**
     * Verify that the create method adds a new field to set hash
     */
    @Test
    public void testCreateWithUsername() {
        doNothing().when(redisService).setField(any(), any());
        Player player = playerService.create("Akan");

        verify(redisService).setField(any(), any());
        assertEquals(player.getUsername(), "Akan");
    }

    /**
     * Verify that the create method adds a new field to set hash
     */
    @Test
    public void testCreateWithUsernameAndRoomId() {
        doNothing().when(redisService).setField(any(), any());

        Player player = playerService.create("Akan", room.getId());

        verify(redisService).setField(any(), any());

        assertEquals(player.getUsername(), "Akan");
        assertEquals(player.getRoomId(), room.getId());
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
        when(redisService.getField("players", "1234")).thenReturn(this.player);

        Player player = playerService.get("1234");

        verify(redisService).getField("players", "1234");
        assertEquals(player, this.player);
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

    /**
     * Test that the delete method throws an exception when the deleteField method fails
     */
    @Test
    public void testDeleteThrowsException(){
        when(redisService.deleteField("players", player.getId())).thenReturn(0L);


        assertThrows(ObjectNotFoundException.class, () -> {
            playerService.delete(player.getId());
        });

        verify(redisService).deleteField("players", player.getId());
    }

    /**
     * Test that the update persist accross the collection
     */
    @Test
    public void testPersistUpdate() {
        ObjectMapper objMapper = new ObjectMapper();
        String key = String.format("rooms:%s:players", player.getRoomId());

        Player originalPlayer = player.deepCopy(objMapper);
        player.setUsername("Doe");
        when(redisService.getField("players", player.getId())).thenReturn(originalPlayer);
        doNothing().when(redisService).updateSortedSetMemberAndHash(
                key, "players", originalPlayer, player);

        playerService.persistUpdate(player);

        verify(redisService).getField("players", player.getId());
        verify(redisService).updateSortedSetMemberAndHash(key, "players", originalPlayer, player);
    }

    /**
     * Test that the deletion persist accross the collections
     */
    @Test
    public void testPersistDelete() {
        String key = String.format("rooms:%s:players", player.getRoomId());
        doNothing().when(redisService).deleteMemberFromSortedSetAndHash(key, "players", player);

        playerService.persistDelete(room.getId(), player);

        verify(redisService).deleteMemberFromSortedSetAndHash(key, "players", player);
    }

    /**
     * Verify that the method retrieves a player in ascending or descending order
     * by converting the result returned to a list to maintain the order
     */
    @Test
    public void testGetPlayersOrderBy() {
        String key = String.format("rooms:%s:players", player.getRoomId());

        Set<Object> set = new HashSet<>();
        set.add((Object) player);
        
        when(redisService.getSortedSet("DESC", key)).thenReturn(set);
        playerService.getPlayersOrderBy("DESC", room.getId());

        verify(redisService).getSortedSet("DESC", key);
    }

    // @Test
    // public void testIncrementScore() {
    //     PlayerService mockPlayerService = mock(PlayerService.class);
    //     when(mockPlayerService.get(player.getId())).thenReturn(player);
    //     doNothing().when(mockPlayerService).persistUpdate(player);
    //     
    //     mockPlayerService.incrementScore(player.getId(), 10);
    //
    //
    //     verify(mockPlayerService).get(player.getId());
    //     verify(mockPlayerService).persistUpdate(player);
    //
    //     assertEquals(player.getScore(), 10);
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


