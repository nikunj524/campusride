package com.campusride.userservice.exception;

public class VehicleNumberAlreadyExistsException extends RuntimeException {

    public VehicleNumberAlreadyExistsException(String vehicleNumber) {
        super("Vehicle number " + vehicleNumber + " is already registered");
    }
}
