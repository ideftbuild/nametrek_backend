package com.nametrek.api.model;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


import jakarta.persistence.*;
import com.nametrek.api.utils.CodeGenerator;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name="rooms")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

	private Integer capacity = 4;
	private Integer rounds = 4;

    @Column(length = 10)
    private String code = CodeGenerator.generateCode();

    public Room(Integer rounds) {
        this.rounds = rounds;
    }
}
