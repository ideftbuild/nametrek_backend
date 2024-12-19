package com.nametrek.api.exception;


public class RoomFullException extends RuntimeException {
    public RoomFullException(String message) {
        super(message);
    }
}
