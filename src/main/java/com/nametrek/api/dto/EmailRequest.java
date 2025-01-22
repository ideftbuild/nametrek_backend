package com.nametrek.api.dto;

import io.micrometer.common.lang.NonNull;
import lombok.Data;

@Data
public class EmailRequest {
    @NonNull
    String to;

	@NonNull
	String email;

    @NonNull
    String subject;

    @NonNull
    String text;
}
