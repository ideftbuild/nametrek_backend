package com.nametrek.api.service;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nametrek.api.dto.PlayerDto;
import com.nametrek.api.dto.RoomEventResponse;
import com.nametrek.api.dto.RoomPlayerResponse;
import com.nametrek.api.exception.RoomFullException;
import com.nametrek.api.model.Player;
import com.nametrek.api.model.Room;
import com.nametrek.api.service.RoomPlayerService;
import com.nametrek.api.service.PlayerService;
import com.nametrek.api.service.RoomService;

import lombok.extern.slf4j.Slf4j;

import com.nametrek.api.service.RedisService;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class RoomPlayerServiceTest {

    @Mock
    private Player player;

    @Mock
    NotificationService notificationService;

    @Mock
    private RoomService roomService;

    @Mock
    private RedisService redisService;

    @Mock
    private PlayerService playerService;

    @InjectMocks
    private RoomPlayerService roomPlayerService;

    private Room room;

    @BeforeEach
    public void setUp() {
        room = new Room();
    }

    /**
     * Verify that the create method creates the Room and add a new player in it
     */
    @Test
    public void testCreate() {
        doNothing().when(roomService).save(any());
        doNothing().when(playerService).save(any());
        doNothing().when(redisService).addToSortedSet(any(), any(), any());

        RoomPlayerResponse res = roomPlayerService.create("Akan");

        verify(roomService).save(any());
        verify(playerService).save(any());
        verify(redisService).addToSortedSet(any(), any(), any());

        assertEquals(res.getPlayer().getUsername(), "Akan");
        assertEquals(res.getPlayer().getRoomId(), res.getRoom().getId());
    }

    /**
     * Test that the updatePlayer method updates player username and score
     */
    @Test
    public void testUpdatePlayerUsernamAndScore() {
        Player originalplayer = new Player("John", room.getId(), 201);

        PlayerDto playerDto = new PlayerDto();
        playerDto.setUsername("Akan");

        when(playerService.get(originalplayer.getId())).thenReturn(originalplayer);
        when(roomService.get(room.getId())).thenReturn(room);
        doNothing().when(redisService).updateSortedSetMemberAndHash(any(), any(), any(), any());
        doNothing().when(notificationService).sendMessageToTopic(any(), any());

        RoomPlayerResponse res = roomPlayerService.updatePlayer(room.getId(), originalplayer.getId(), playerDto);

        verify(playerService).get(originalplayer.getId());
        verify(roomService).get(room.getId());
        verify(redisService).updateSortedSetMemberAndHash(any(), any(), any(), any());
        verify(notificationService).sendMessageToTopic(any(), any());

        assertEquals(res.getPlayer().getUsername(), "Akan");
        assertEquals(res.getPlayer().getScore(), 201);
    }

    /**
     * Test that the method updates player username
     */
    @Test
    public void testUpdatePlayerUsernam() {
        Player originalplayer = new Player("John", room.getId());
        PlayerDto playerDto = new PlayerDto();
        playerDto.setUsername("Akan");

        when(playerService.get(originalplayer.getId())).thenReturn(originalplayer);
        when(roomService.get(room.getId())).thenReturn(room);
        doNothing().when(redisService).updateSortedSetMemberAndHash(any(), any(), any(), any());
        doNothing().when(notificationService).sendMessageToTopic(any(), any());

        RoomPlayerResponse res = roomPlayerService.updatePlayer(room.getId(), originalplayer.getId(), playerDto);

        verify(playerService).get(originalplayer.getId());
        verify(roomService).get(room.getId());
        verify(redisService).updateSortedSetMemberAndHash(any(), any(), any(), any());
        verify(notificationService).sendMessageToTopic(any(), any());

        assertEquals(res.getPlayer().getUsername(), "Akan");
        assertEquals(res.getPlayer().getScore(), 0);
    }

    /**
     * Test that the method adds a player to a room
     */
    @Test
    public void testAddPlayerToRoom() {
        when(roomService.get(room.getId())).thenReturn(room);
        doNothing().when(roomService).save(room);
        doNothing().when(playerService).save(any());
        doNothing().when(redisService).addToSortedSet(any(), any(), any());
        doNothing().when(notificationService).sendMessageToTopic(any(), any());

        RoomPlayerResponse res = roomPlayerService.addPlayerToRoom(room.getId(), "Akan");

        assertEquals(room.getActivePlayerCount(), 1);
        verify(roomService).save(room);
        verify(playerService).save(any());
        verify(redisService).addToSortedSet(any(), any(), any());
        verify(notificationService).sendMessageToTopic(any(), any());

        assertEquals(res.getRoom(), room);
        assertEquals(res.getPlayer().getUsername(), "Akan");
    }
   
    /**
     * Verify that the method throws an IllegalArgumentException when player is not in the room 
     */
    @Test
    public void testDeletePlayerThrowsException() {
        room.setActivePlayerCount(1);

        Player player = new Player("Akan", room.getId());
        String key = String.format("rooms:%s:players", room.getId());

        when(playerService.get(player.getId())).thenReturn(player);

        assertThrows(IllegalArgumentException.class, () -> {
            roomPlayerService.deletePlayer("1234", player.getId());
        });
        verify(playerService).get(player.getId());
    }

    /**
     * Verify that method deletes the player
     */
    @Test
    public void testDeletePlayer() {
        room.setActivePlayerCount(1);

        Player player = new Player("Akan", room.getId());
        String key = String.format("rooms:%s:players", room.getId());

        when(playerService.get(player.getId())).thenReturn(player);
        doNothing().when(redisService).deleteMemberFromSortedSetAndHash(key, "players", player);
        when(roomService.get(room.getId())).thenReturn(room);
        doNothing().when(roomService).save(room);
        doNothing().when(notificationService).sendMessageToTopic(any(), any());

        roomPlayerService.deletePlayer(room.getId(), player.getId());

        assertEquals(room.getActivePlayerCount(), 0);
        verify(playerService).get(player.getId());
        verify(redisService).deleteMemberFromSortedSetAndHash(key, "players", player);
        verify(roomService).get(room.getId());
        verify(roomService).save(room);
        verify(notificationService).sendMessageToTopic(any(), any());
   
    }

    /**
     * Test that the method get players by descending or ascending order
     */
    @Test
    public void testGetPlayerOrderBy() {
        String key = String.format("rooms:%s:players", room.getId());

        Set<Object> set = new HashSet<>();
        when(redisService.getSortedSet("DESC", key))
            .thenReturn(set);

        List<Player> players = roomPlayerService.getPlayersOrderBy("DESC", room.getId());

        verify(redisService).getSortedSet("DESC", key);
    }
}

