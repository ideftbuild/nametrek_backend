package com.nametrek.api.dto;

import io.micrometer.common.lang.NonNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PlayerNameDto {
    @NonNull
    private String playerName;
}
