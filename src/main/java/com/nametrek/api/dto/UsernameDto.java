package com.nametrek.api.dto;

import io.micrometer.common.lang.NonNull;
import lombok.Getter;

@Getter
public class UsernameDto {
    @NonNull
    private String username;
}
