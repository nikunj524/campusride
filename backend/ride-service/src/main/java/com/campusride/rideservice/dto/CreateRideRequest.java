package com.campusride.rideservice.dto;

import com.campusride.rideservice.enums.RideType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateRideRequest(
        @NotNull(message = "Driver ID is required")
        Long driverId,

        @NotNull(message = "Ride type is required")
        RideType type,

        @NotBlank(message = "Source location is required")
        @Size(max = 255, message = "Source must not exceed 255 characters")
        String source,

        @NotBlank(message = "Destination is required")
        @Size(max = 255, message = "Destination must not exceed 255 characters")
        String destination,

        @NotNull(message = "Departure time is required")
        @Future(message = "Departure time must be in the future")
        LocalDateTime departureTime,

        @NotNull(message = "Available seats is required")
        @Min(value = 1, message = "At least 1 seat must be available")
        Integer availableSeats,

        @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
        BigDecimal pricePerSeat,

        @Size(max = 500, message = "Notes must not exceed 500 characters")
        String notes
) {
}
