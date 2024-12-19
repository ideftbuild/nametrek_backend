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

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;

import com.nametrek.api.exception.ObjectNotFoundException;
import com.nametrek.api.model.Player;
import com.nametrek.api.model.Room;
import com.nametrek.api.dto.RoomDto;

import lombok.extern.slf4j.Slf4j;

/**
 * Test RoomService class
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
public class RoomServiceTest {

    private Room room;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private RoomService roomService;

    @BeforeEach
    public void saveUp() {
        this.room = new Room();
    }


	/**
	 * Verify that the save method sets a new field
	 */
	@Test
	public void testsave() {
		doNothing().when(redisService).setField("rooms", this.room);

		roomService.save(this.room);
		verify(redisService).setField("rooms", this.room);
	}

	/**
	 * Test that the get method returns the room associated with a key
	 */
	@Test
	public void testGet() {
		when(redisService.getField("rooms", "key")).thenReturn(this.room);

		roomService.get("key");

		verify(redisService).getField("rooms", "key");
	}

    /**
     * Test that the delete method delete the room 
     */
    @Test
    public void testDelete() {
        when(redisService.deleteField("rooms", this.room.getId())).thenReturn(1L);

        roomService.delete(this.room.getId());

        verify(redisService).deleteField("rooms", this.room.getId());
    }


    /**
     * Test that delete method throws ObjectNotFoundException when room is not found
     */
    @Test
    public void testDeleteThrowsException() {
        when(redisService.deleteField("rooms", this.room.getId())).thenReturn(0L);

        assertThrows(ObjectNotFoundException.class, () -> {
        roomService.delete(this.room.getId());
        });

        verify(redisService).deleteField("rooms", this.room.getId());
    }

	/**
	 * Test that the create method creates a new Room
	 */
    @Test
    public void testCreate() {
        doNothing().when(redisService).setField(any(), any());

        Room room = roomService.create();

        assertNotNull(room);
        verify(redisService).setField(any(), any());
    }

	/**
	 * Verify that the update method correctly updates a room based on the dto provided
	 */
    @Test
    public void testUpdate() {
        Room room = new Room();
        RoomDto roomDto = new RoomDto(2, 2);

        when(redisService.getField("rooms", room.getId())).thenReturn(room);
        doNothing().when(redisService).setField(any(), any());

        roomService.update(room.getId(), roomDto);
        assertEquals(room.getCurrentRound(), roomDto.getCurrentRound());
        assertEquals(room.getActivePlayerCount(), roomDto.getActivePlayerCount());

		verify(redisService).setField(any(), any());
		verify(redisService).getField("rooms", room.getId());
    }

}
