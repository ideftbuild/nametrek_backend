package com.nametrek.api.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nametrek.api.model.Room;
import com.nametrek.api.model.Player;

/**
 * RoomRepository
 */
public interface RoomRepository extends JpaRepository<Room, UUID> {
    Optional<Room> findByCode(String code);
}

