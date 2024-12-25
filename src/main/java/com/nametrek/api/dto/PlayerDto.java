package com.nametrek.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PlayerDto {
    // private String id;
    private String username;
    private Integer score;
    // private String roomId/* ; */
}

