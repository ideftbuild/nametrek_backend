package com.nametrek.api.exception;

public class RoomEmptyException extends RuntimeException {
    public RoomEmptyException(String message) {
        super(message);
    }
}
