package com.campusride.userservice.exception;

public class VehicleAlreadyRegisteredException extends RuntimeException {

    public VehicleAlreadyRegisteredException(Long userId) {
        super("Driver " + userId + " already has a registered vehicle");
    }
}
