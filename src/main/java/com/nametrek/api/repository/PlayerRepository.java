package com.nametrek.api.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nametrek.api.model.Player;
import com.nametrek.api.model.Room;
import com.nametrek.api.model.Player.EventType;


/**
 * PlayerRepository
 */
public interface PlayerRepository extends JpaRepository<Player, Long> {
    Optional<Player> findByRoom(Room room);

    @Modifying
    @Query("DELETE FROM Player p WHERE p.room.id = :roomId")
    void deleteByRoomId(@Param("roomId") UUID roomId);

    List<Player> findByRoomAndStatus(Room room, EventType status);
}
