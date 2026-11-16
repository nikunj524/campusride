package com.campusride.rideservice.dto;

import com.campusride.rideservice.enums.RideType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SearchRideRequest(
        RideType type,
        @Size(max = 255) String source,
        @Size(max = 255) String destination,
        LocalDate travelDate,
        @Min(1) Integer minimumAvailableSeats,
        @Size(max = 50) String vehicleType,
        @DecimalMin(value = "0.0", inclusive = true) BigDecimal minimumPrice,
        @DecimalMin(value = "0.0", inclusive = true) BigDecimal maximumPrice,
        SortOrder sortOrder
) {
    public enum SortOrder {
        LOWEST_PRICE, HIGHEST_PRICE, LATEST_RIDES
    }
}
