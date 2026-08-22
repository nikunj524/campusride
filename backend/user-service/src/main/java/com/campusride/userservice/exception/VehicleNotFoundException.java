package com.campusride.userservice.exception;

public class VehicleNotFoundException extends RuntimeException {

    public VehicleNotFoundException(Long userId) {
        super("Vehicle for user " + userId + " was not found");
    }
}
