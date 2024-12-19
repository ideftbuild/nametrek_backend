package com.nametrek.api.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nametrek.api.model.Room;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest
@Slf4j
public class ActualDataTempTest {

    @Autowired
    private RoomService roomService;



    // @Test
    // public void testUpdateRoom() {
    //     // Room room = new Room();
    //     roomService.delete("66c71828-431c-4e46-96ad-32b42fbb7951");
    // }
}
