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
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import com.nametrek.api.exception.ObjectNotFoundException;
import com.nametrek.api.dto.EventType;
import com.nametrek.api.model.Player;
import com.nametrek.api.model.Room;
import com.nametrek.api.repository.RoomRepository;
import com.nametrek.api.utils.CodeGenerator;
import com.nametrek.api.utils.RedisKeys;

import com.nametrek.api.dto.PlayerDto;
import com.nametrek.api.dto.RoomDto;
import com.nametrek.api.dto.RoomEventResponse;
import com.nametrek.api.dto.RoomPlayerInfo;

import lombok.extern.slf4j.Slf4j;

/**
 * Test RoomService class
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    private Room room;

    private Player player;

    @Mock
    private RedisService redisService;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private PlayerService playerService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private RoomService roomService;


    @BeforeEach
    public void saveUp() {
        room = new Room(1);
        room.setId(UUID.randomUUID());
        player = new Player("Akan", room, 10D);
        player.setId(1L);
    }

    /**
     * Test that the method returns the owner of the room
     */
    @Test
    public void testGetOwner() {
        when(playerService.getPlayerByRoom(room)).thenReturn(player);

        roomService.getOwner(room);

        verify(playerService).getPlayerByRoom(room);
    }


    /**
     * Test that the method returns the room based on the code
     */
    @Test
    public void testGetRoomByCode() {
        when(roomRepository.findByCode(room.getCode())).thenReturn(Optional.of(room));

        roomService.getRoomByCode(room.getCode());
        verify(roomRepository).findByCode(room.getCode());
    }

    /**
     * Verify that the method checks if a room exists using an id
     */
    @Test
    public void testExistsById() {
        when(roomRepository.existsById(room.getId())).thenReturn(true);

        roomService.existsById(room.getId());
        verify(roomRepository).existsById(room.getId());
    }

    /**
     * Test that all operations are being being carried out during room creation
     */
    @Test
    public void testCreate() {
        when(roomRepository.save(any())).thenReturn(room);
        when(playerService.save(any())).thenReturn(player);
        doNothing().when(redisService).setFields(any(), any());
        doNothing().when(redisService).addToSortedSet(any(), any(), any());

        RoomPlayerInfo roomPlayerInfo = roomService.create("Akan", 2);

        verify(roomRepository).save(any());
        verify(playerService).save(any());
        verify(redisService).setFields(any(), any());
        verify(redisService).addToSortedSet(any(), any(), any());

        assertEquals(roomPlayerInfo.getRoomCode().length(), CodeGenerator.CODE_LENGTH);
        assertEquals(roomPlayerInfo.getPlayer().getName(), "Akan");
    }

    /**
     * Test that the adds player to room
     */
    @Test
    public void testAddPlayerToRoom() {

        doNothing().when(redisService).addToSortedSet(RedisKeys.formatInGamePlayersKey(room.getId()), player.getId(), player.getScore());

        roomService.addPlayerToRoom(room.getId(), player.getId(), player.getScore());

        verify(redisService).addToSortedSet(RedisKeys.formatInGamePlayersKey(room.getId()), player.getId(), player.getScore());
    }

    /**
     * Verify that the method returns a player in a room 
     */
    @Test
    public void testGetPlayer() {
        UUID roomId = room.getId();
        Long playerId = player.getId();
        when(roomRepository.existsById(roomId)).thenReturn(true);

        when(playerService.getPlayerById(playerId)).thenReturn(player);

        System.out.println("roomId: " + roomId);
        Player player = roomService.getPlayer(roomId, playerId);

        verify(roomRepository).existsById(roomId);
        verify(playerService).getPlayerById(playerId);

        assertEquals(player.getName(), this.player.getName());
    }

    /**
     * Verify that a room can be retrieved using the id
     */
    @Test
    public void testGetRoomById() {

        UUID roomId = this.room.getId();
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        Room room = roomService.getRoomById(roomId);

        verify(roomRepository).findById(roomId);
        assertEquals(room, this.room);
    }

    /**
     * Test that the method handles Connection properly
     */
    @Test
    public void testReconnect() {
        String disconnectedSessionKey = RedisKeys.formatPlayerSessionKey(player.getId());
        UUID roomId = room.getId();
        Long playerId = player.getId();
        String roomKey = RedisKeys.formatRoomKey(roomId);
        String inGamePlayersKey = RedisKeys.formatInGamePlayersKey(roomId);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(redisService.getField(roomKey, RedisKeys.IN_GAME)).thenReturn(false);

        when(redisService.keyExists(disconnectedSessionKey)).thenReturn(true);

        when(redisService.delete(disconnectedSessionKey)).thenReturn(true);
        doNothing().when(redisService).addToSortedSet(inGamePlayersKey, playerId, player.getScore());
        doNothing().when(notificationService).sendMessageToTopic(any(), any());
        when(playerService.getPlayers("DESC", roomId))
            .thenReturn(List.of(mock(PlayerDto.class)));
        when(playerService.getPlayerById(playerId)).thenReturn(player);

        roomService.connect("1234", roomId, playerId);

        verify(roomRepository).findById(roomId);
        verify(redisService).getField(roomKey, RedisKeys.IN_GAME);
        verify(redisService).keyExists(disconnectedSessionKey);
        verify(redisService).delete(disconnectedSessionKey);
        verify(redisService).addToSortedSet(inGamePlayersKey, playerId, player.getScore());
        verify(playerService).getPlayers("DESC", roomId);
        verify(notificationService).sendMessageToTopic(any(), any());
    }

    /**
     * Test that the method handles Connection properly
     */
    @Test
    public void testConnect() {
        String disconnectedSessionKey = RedisKeys.formatPlayerSessionKey(player.getId());
        UUID roomId = room.getId();
        Long playerId = player.getId();
        String roomKey = RedisKeys.formatRoomKey(roomId);
        String inGamePlayersKey = RedisKeys.formatInGamePlayersKey(roomId);

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(redisService.getField(roomKey, RedisKeys.IN_GAME)).thenReturn(false);

        when(redisService.keyExists(disconnectedSessionKey)).thenReturn(false);

        doNothing().when(redisService).addToSortedSet(inGamePlayersKey, playerId, 0d);
        doNothing().when(notificationService).sendMessageToTopic(any(), any());
        when(playerService.getPlayers("DESC", roomId))
            .thenReturn(List.of(mock(PlayerDto.class)));

        
        roomService.connect("1234", roomId, playerId);

        verify(roomRepository).findById(roomId);
        verify(redisService).getField(roomKey, RedisKeys.IN_GAME);
        verify(redisService).keyExists(disconnectedSessionKey);
        verify(redisService).addToSortedSet(inGamePlayersKey, playerId, 0d);
        verify(playerService).getPlayers("DESC", roomId);
        verify(notificationService).sendMessageToTopic(any(), any());
    }

    // @Test
    // public void testDisconnect() {
    //     UUID roomId = room.getId();
    //     Long playerId = player.getId();
    //     String inGamePlayersKey = RedisKeys.formatInGamePlayersKey(roomId);
    //
    //     when(roomRepository.existsById(roomId)).thenReturn(true);
    //     when(playerService.getPlayerById(playerId)).thenReturn(player);
    //     doNothing().when(playerService).createPlayerSession(roomId, playerId);
    //     when(redisService.getMemberScore(inGamePlayersKey, playerId)).thenReturn(0D);
    //     when(playerService.save(player)).thenReturn(player);
    //     when(redisService.deleteMemberFromSortedSet(inGamePlayersKey, playerId)).thenReturn(1L);
    //     
    //     doNothing().when(notificationService).sendMessageToTopic(any(), any());
    //
    //     roomService.disconnect(roomId, playerId);
    //
    //     // Ensure the methods are called
    //     verify(roomRepository).existsById(roomId);
    //     verify(playerService).getPlayerById(playerId);
    //     verify(playerService).createPlayerSession(roomId, playerId);
    //     verify(redisService).getMemberScore(inGamePlayersKey, playerId);
    //     verify(playerService).save(player);
    //     verify(redisService).deleteMemberFromSortedSet(inGamePlayersKey, playerId);
    //     verify(notificationService).sendMessageToTopic(any(), any());
   

    /**
     * Verify that the method allows player to join using the room id
     */
    @Test
    public void testJoinRoomById() {
        UUID roomId = room.getId();
        Long playerId = player.getId();
        String inGamePlayersKey = RedisKeys.formatInGamePlayersKey(roomId);
        String roomKey = RedisKeys.formatRoomKey(roomId);
        String playerNameKey = RedisKeys.formatPlayerNameKey(playerId);
        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));
        when(redisService.sortedSetLength(inGamePlayersKey)).thenReturn(1L);

        when(playerService.save(any())).thenReturn(player);

        doNothing().when(redisService).setField(
                roomKey,
                playerNameKey,
                player.getName());


        RoomPlayerInfo roomPlayerInfo = roomService.joinRoomById(roomId, "Akan");

        verify(redisService).sortedSetLength(inGamePlayersKey);
        verify(playerService).save(any());
        verify(redisService).setField(roomKey, playerNameKey, player.getName());
        verify(roomRepository).findById(roomId);

        assertEquals(roomPlayerInfo.getRoomId(), roomId);
        assertEquals(roomPlayerInfo.getPlayer().getId(), playerId);
    }
    
    /**
     * Verify that the method allows player to join using the room code
     */
    @Test
    public void testJoinRoomByCode() {
        UUID roomId = room.getId();
        Long playerId = player.getId();
        String inGamePlayersKey = RedisKeys.formatInGamePlayersKey(roomId);
        String roomCode = "RESIRISE";
        String roomKey = RedisKeys.formatRoomKey(roomId);
        String playerNameKey = RedisKeys.formatPlayerNameKey(playerId);
        when(roomRepository.findByCode(roomCode)).thenReturn(Optional.of(room));
        when(redisService.sortedSetLength(inGamePlayersKey)).thenReturn(1L);

        when(playerService.save(any())).thenReturn(player);

        doNothing().when(redisService).setField(
                roomKey,
                playerNameKey,
                player.getName());


        RoomPlayerInfo roomPlayerInfo = roomService.joinRoomByCode(roomCode, "Akan");

        verify(redisService).sortedSetLength(inGamePlayersKey);
        verify(playerService).save(any());
        verify(redisService).setField(roomKey, playerNameKey, player.getName());
        verify(roomRepository).findByCode(roomCode);

        assertEquals(roomPlayerInfo.getRoomId(), roomId);
        assertEquals(roomPlayerInfo.getPlayer().getId(), playerId);
    }

    /**
     * Test that the method deletes a room
     */
    @Test
    public void testDeleteRoom() {
        UUID roomId = room.getId();
        doNothing().when(playerService).deleteByRoomId(roomId);
        doNothing().when(roomRepository).deleteById(roomId);

        roomService.deleteRoom(roomId);

        verify(playerService).deleteByRoomId(roomId);
        verify(roomRepository).deleteById(roomId);
    }

    /**
     * Verify that the method removes the player from the room and notifies other players of their removal
     */
    @Test
    public void testLeave() {
        Long playerId = player.getId();
        UUID roomId = room.getId();

        String roomKey = RedisKeys.formatRoomKey(roomId);
        String playerNameKey = RedisKeys.formatPlayerNameKey(playerId);
        String playerLostStatusKey = RedisKeys.formatPlayerLostStatus(playerId);
        String inGamePlayersKey = RedisKeys.formatInGamePlayersKey(roomId);

        when(playerService.deleteAndGet(playerId)).thenReturn(Optional.of(player));
        when(redisService.deleteField(roomKey, playerNameKey)).thenReturn(1L);
        when(redisService.deleteField(roomKey, playerLostStatusKey)).thenReturn(1L);
        when(redisService.sortedSetLength(inGamePlayersKey)).thenReturn(1L);

        when(playerService.getPlayers("DESC", roomId)).thenReturn(List.of(mock(PlayerDto.class)));
        doNothing().when(notificationService).sendMessageToTopic(any(), any());

        roomService.leave(roomId, playerId);

        verify(playerService).deleteAndGet(playerId);
        verify(redisService).deleteField(roomKey, playerNameKey);
        verify(redisService).deleteField(roomKey, playerLostStatusKey);
        verify(redisService).sortedSetLength(inGamePlayersKey);
        verify(playerService).getPlayers("DESC", roomId);
        verify(notificationService).sendMessageToTopic(any(), any());
    }

    /**
     * Verify that the leave method deletes the room when room is empty
     */
    @Test
    public void testLeaveWhenRoomIsEmpty() {
        Long playerId = player.getId();
        UUID roomId = room.getId();

        String roomKey = RedisKeys.formatRoomKey(roomId);
        String playerNameKey = RedisKeys.formatPlayerNameKey(playerId);
        String playerLostStatusKey = RedisKeys.formatPlayerLostStatus(playerId);
        String inGamePlayersKey = RedisKeys.formatInGamePlayersKey(roomId);

        when(playerService.deleteAndGet(playerId)).thenReturn(Optional.of(player));
        when(redisService.deleteField(roomKey, playerNameKey)).thenReturn(1L);
        when(redisService.deleteField(roomKey, playerLostStatusKey)).thenReturn(1L);
        when(redisService.sortedSetLength(inGamePlayersKey)).thenReturn(0L);
        doNothing().when(playerService).deleteByRoomId(roomId);
        doNothing().when(roomRepository).deleteById(roomId);

        roomService.leave(roomId, playerId);

        verify(playerService).deleteAndGet(playerId);
        verify(redisService).deleteField(roomKey, playerNameKey);
        verify(redisService).deleteField(roomKey, playerLostStatusKey);
        verify(redisService).sortedSetLength(inGamePlayersKey);
        verify(playerService).deleteByRoomId(roomId);
        verify(roomRepository).deleteById(roomId);
    }

    /**
     * Test that the method returns room update that properly includes rom metadata
     */
    @Test
    public void testGetRoomUpdate() {
        UUID roomId = room.getId();
        Long playerId = player.getId();
        String roomKey = RedisKeys.formatRoomKey(roomId);

        List<PlayerDto> players = List.of(mock(PlayerDto.class));

        when(roomRepository.findById(roomId)).thenReturn(Optional.of(room));

        when(playerService.getPlayerById(playerId)).thenReturn(player);
        when(redisService.getField(roomKey, RedisKeys.ROUND)).thenReturn(1);
        when(playerService.getPlayers("DESC", roomId)).thenReturn(players);
        when(redisService.getField(roomKey, RedisKeys.OWNER)).thenReturn(playerId);


        RoomEventResponse roomEventResponse = roomService.getRoomUpdate(roomId, playerId);


        // Ensure calls are made
        verify(roomRepository).findById(roomId);
        verify(playerService).getPlayerById(playerId);
        verify(redisService).getField(roomKey, RedisKeys.ROUND);
        verify(playerService).getPlayers("DESC", roomId);
        verify(redisService).getField(roomKey, RedisKeys.OWNER);

        // Ensure the data retrieved is correct
        assertEquals(roomEventResponse.getEventType(), EventType.GET);
        assertEquals(roomEventResponse.getRoom().getRound(), 1);
        assertEquals(roomEventResponse.getRoom().getRounds(), room.getRounds());
        assertEquals(roomEventResponse.getRoom().getMaxPlayers(), room.getCapacity());

        assertEquals(roomEventResponse.getPlayers(), players);
    }
}
