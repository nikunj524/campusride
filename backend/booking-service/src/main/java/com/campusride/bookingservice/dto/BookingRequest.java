package com.campusride.bookingservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequest {

    @NotNull(message = "Ride ID is required")
    private Long rideId;

    @NotNull(message = "Passenger ID is required")
    private Long passengerId;

    @NotNull(message = "Number of seats is required")
    @Min(value = 1, message = "At least 1 seat must be booked")
    private Integer seats;
}
