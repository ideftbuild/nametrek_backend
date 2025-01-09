package com.nametrek.api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.any;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import com.nametrek.api.exception.ObjectNotFoundException;
import com.nametrek.api.dto.EventType;
import com.nametrek.api.model.Player;
import com.nametrek.api.model.Room;
import com.nametrek.api.utils.CodeGenerator;
import com.nametrek.api.dto.PlayerDto;
import com.nametrek.api.dto.RoomDto;
import com.nametrek.api.dto.RoomEventResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * Test RoomService class
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {
    //
    // private Room room;
    //
    // private Player player;
    //
    // @Mock
    // private RedisService redisService;
    // 
    // @Mock
    // private PlayerService playerService;
    //
    // @Mock
    // private NotificationService notificationService;
    //
    // @Mock
    // private CodeGenerator codeGenerator;
    //
    // @InjectMocks
    // private RoomService roomService;
    //
    //
    // @BeforeEach
    // public void saveUp() {
    //     this.room = new Room();
    //     room.setCode("1234");
    //     this.player = new Player("Akan", room.getId(), 10);
    // }
    //
	// /**
	//  * Verify that the save method sets a new field
	//  */
	// @Test
	// public void testsave() {
	// 	doNothing().when(redisService).setField("rooms", this.room);
	//
	// 	roomService.save(this.room);
	// 	verify(redisService).setField("rooms", this.room);
	// }
	//
	// /**
	//  * Test that the get method returns the room associated with a key
	//  */
	// @Test
	// public void testGet() {
	// 	when(redisService.getField("rooms", "key")).thenReturn(this.room);
	//
	// 	roomService.get("key");
	//
	// 	verify(redisService).getField("rooms", "key");
	// }
	//
 //    /**
 //     * Test that the delete method delete the room 
 //     */
 //    @Test
 //    public void testDelete() {
 //        List<Player> players = new ArrayList<>();
 //        players.add(player);
	//
 //        when(playerService.getPlayersOrderBy(
 //                    "ASC", room.getId())).thenReturn(players);
 //        when(redisService.getField("rooms", room.getId())).thenReturn(room);
 //        when(redisService.delete("room:" + room.getCode())).thenReturn(true);
 //        doNothing().when(playerService).persistDelete(room.getId(), player);
 //        when(redisService.deleteField("rooms", room.getId())).thenReturn(1L);
	//
 //        roomService.delete(room.getId());
	//
 //        verify(redisService).deleteField("rooms", room.getId());
 //        verify(redisService).getField("rooms", room.getId());
 //        verify(redisService).delete("room:" + room.getCode());
 //    }
	//
	//
 //    /**
 //     * Test that delete method throws ObjectNotFoundException when room is not found
 //     */
 //    @Test
 //    public void testDeleteThrowsException() {
 //        List<Player> players = new ArrayList<>();
 //        players.add(player);
	//
 //        when(playerService.getPlayersOrderBy(
 //                    "ASC", room.getId())).thenReturn(players);
 //        when(redisService.getField("rooms", room.getId())).thenReturn(room);
 //        doNothing().when(playerService).persistDelete(room.getId(), player);
	//
 //        when(redisService.deleteField("rooms", room.getId())).thenReturn(0L);
	//
 //        assertThrows(ObjectNotFoundException.class, () -> {
 //            roomService.delete(room.getId());
 //        });
	//
 //        verify(redisService).deleteField("rooms", room.getId());
 //        verify(redisService).getField("rooms", room.getId());
 //    }
	//
	// /**
	//  * Test that the create method creates a new Room
	//  */
 //    @Test
 //    public void testCreate() {
 //        when(playerService.create(any(), any())).thenReturn(player);
 //        doNothing().when(redisService).addToSortedSet(any(), any(), any());
 //        doNothing().when(redisService).setField(any(), any());
 //        when(codeGenerator.generateCode()).thenReturn("GY4LQCZF");
	//
 //        RoomEventResponse roomEventResponse = roomService.create("John", 5);
	//
 //        verify(redisService).setField(any(), any());
 //        verify(redisService).addToSortedSet(any(), any(), any());
 //        verify(playerService).create(any(), any());
	//
 //        assertNotNull(roomEventResponse);
 //        assertEquals(roomEventResponse.getRoom().getActivePlayerCount(), 1);
 //        assertEquals(roomEventResponse.getRoom().getCode(), "GY4LQCZF");
 //        assertInstanceOf(Room.class, roomEventResponse.getRoom());
 //        assertEquals(roomEventResponse.getPlayer(), player);
 //        assertEquals(roomEventResponse.getEventType(), EventType.CREATE);
 //        assertInstanceOf(String.class, roomEventResponse.getTimestamp());
 //    }
	//
 //    /**
 //     * Test that the method adds a player to a room using the room id
 //     * Players can be said to be in a room when added to the sorted set
 //     */
 //    @Test
 //    public void testJoinRoomById() {
 //        String key = String.format("rooms:%s:players", room.getId());
 //        RoomEventResponse roomEventResponse = 
 //            new RoomEventResponse(room, player, EventType.JOIN, LocalDateTime.now().toString()); 
	//
 //        when(redisService.getField("rooms", room.getId())).thenReturn(room);
 //        when(playerService.create("Akan", room.getId())).thenReturn(player);
 //        doNothing().when(redisService).setField("rooms", room);
 //        doNothing().when(redisService).addToSortedSet(any(), any(), any());
 //        when(notificationService.sendMessage(any(), any(), any())).thenReturn(roomEventResponse);
	//
 //        RoomEventResponse res = roomService.joinRoomById(room.getId(), "Akan");
	//
 //        verify(redisService).getField("rooms", room.getId());
 //        verify(playerService).create("Akan", room.getId());
 //        verify(redisService).addToSortedSet(any(), any(), any());
 //        verify(redisService).setField("rooms", room);
 //        verify(notificationService).sendMessage(any(), any(), any());
	//
 //        assertEquals(res.getRoom(), room);
 //        assertEquals(res.getPlayer(), player);
 //        assertEquals(res.getEventType(), EventType.JOIN);
 //    }
	//
 //    /**
 //     * Test that the method adds a player to a room using the room code
 //     * Players can be said to be in a room when added to the sorted set
 //     *
 //     */
 //    @Test
 //    public void testJoinRoomByCode() {
 //        String key = String.format("rooms:%s:players", room.getId());
 //        RoomEventResponse roomEventResponse = 
 //            new RoomEventResponse(room, player, EventType.JOIN, LocalDateTime.now().toString()); 
	//
 //        when(redisService.getField("rooms", room.getId())).thenReturn(room);
 //        when(redisService.getValue("room:" + room.getCode())).thenReturn(room.getId());
 //        when(playerService.create("Akan", room.getId())).thenReturn(player);
 //        doNothing().when(redisService).setField("rooms", room);
 //        doNothing().when(redisService).addToSortedSet(any(), any(), any());
 //        when(notificationService.sendMessage(any(), any(), any())).thenReturn(roomEventResponse);
	//
 //        RoomEventResponse res = roomService.joinRoomByCode(room.getCode(), "Akan");
	//
 //        verify(redisService).getField("rooms", room.getId());
 //        verify(playerService).create("Akan", room.getId());
 //        verify(redisService).addToSortedSet(any(), any(), any());
 //        verify(redisService).setField("rooms", room);
 //        verify(notificationService).sendMessage(any(), any(), any());
 //        verify(redisService).getValue("room:" + room.getCode());
	//
 //        assertEquals(res.getRoom(), room);
 //        assertEquals(res.getPlayer(), player);
 //        assertEquals(res.getEventType(), EventType.JOIN);
 //    }
	//
	//
 //    /**
 //     * Verify that the updatePlayer method correctly updates player 
 //     * using a dto
 //     */
 //    public void testUpdatePlayer() {
 //        Player player = new Player("Doe", room.getId());
 //        PlayerDto playerDto = new PlayerDto("Akan", 30);
	//
 //        when(playerService.get(player.getId())).thenReturn(player);
 //        when(redisService.getField("rooms", room.getId())).thenReturn(room);
 //        doNothing().when(playerService).updateFromDto(player, playerDto);
 //        doNothing().when(notificationService).sendMessageToTopic(any(), any());
	//
 //        roomService.updatePlayer(room.getId(), player.getId(), playerDto);
	//
 //        verify(playerService).get(player.getId());
 //        verify(redisService).getField("rooms", room.getId());
 //        verify(playerService).updateFromDto(player, playerDto);
 //        verify(notificationService).sendMessageToTopic(any(), any());
 //    }
	//
 //    @Test
 //    public void testDisconnect() {
 //        room.setActivePlayerCount(2);
 //        Player player = new Player("Doe", room.getId());
 //        RoomEventResponse roomEventResponse = mock(RoomEventResponse.class);
 //        String key = String.format("rooms:%s:players", room.getId());
	//
 //        when(playerService.get(player.getId())).thenReturn(player);
 //        when(redisService.getField("rooms", room.getId())).thenReturn(room);
 //        when(notificationService.sendMessage(any(), any(), any())).thenReturn(roomEventResponse);
 //        doNothing().when(redisService).setField("rooms", room);
 //        doNothing().when(playerService).createPlayerSession(room.getId(), player.getId());
	//
 //        roomService.disconnect(room.getId(), player.getId());
	//
 //        verify(playerService).get(player.getId());
 //        verify(redisService).getField("rooms", room.getId());
 //        verify(redisService).setField("rooms", room);
 //        verify(playerService).createPlayerSession(room.getId(), player.getId());
 //        verify(notificationService).sendMessage(any(), any(), any());
	//
 //        assertEquals(room.getActivePlayerCount(), 1);
 //    }
}
