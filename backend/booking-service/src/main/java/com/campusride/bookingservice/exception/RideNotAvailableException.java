package com.campusride.bookingservice.exception;

public class RideNotAvailableException extends RuntimeException {
    public RideNotAvailableException(String message) {
        super(message);
    }
}
