package com.nametrek.api.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PlayerDto {
    private Long id;
    private String name;
    private Double score;
    private Boolean lost;

    public PlayerDto(Long id, String name, Double score, Boolean lost) {
        this.id = id;
        this.name = name;
        this.score = score;
        this.lost = lost;
    }
}

