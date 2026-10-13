package com.campusride.userservice.exception;

public class DriverRequestNotFoundException extends RuntimeException {

    public DriverRequestNotFoundException(Long requestId) {
        super("Driver request with id " + requestId + " was not found");
    }
}
