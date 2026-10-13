package com.campusride.userservice.dto;

import com.campusride.userservice.enums.OwnershipType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record VehicleUpdateRequest(
        @NotBlank(message = "Vehicle model is required")
        @Size(max = 80, message = "Vehicle model must not exceed 80 characters")
        String vehicleModel,

        @NotBlank(message = "Vehicle color is required")
        @Size(max = 40, message = "Vehicle color must not exceed 40 characters")
        String vehicleColor,

        @NotNull(message = "Total seats is required")
        @Min(value = 1, message = "Total seats must be at least 1")
        Integer totalSeats,

        @NotNull(message = "Ownership type is required")
        OwnershipType ownershipType
) {
}
