package com.campusride.rideservice.dto;

import com.campusride.rideservice.enums.RideStatus;
import com.campusride.rideservice.enums.RideType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RideResponse(
        Long id,
        Long driverId,
        Long passengerId,
        RideType type,
        String source,
        String destination,
        LocalDateTime departureTime,
        Integer availableSeats,
        BigDecimal pricePerSeat,
        RideStatus status,
        String notes,
        LocalDateTime createdAt
) {
}
