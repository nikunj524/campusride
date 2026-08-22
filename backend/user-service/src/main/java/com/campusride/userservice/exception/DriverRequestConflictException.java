package com.campusride.userservice.exception;

public class DriverRequestConflictException extends RuntimeException {

    public DriverRequestConflictException(String message) {
        super(message);
    }
}
