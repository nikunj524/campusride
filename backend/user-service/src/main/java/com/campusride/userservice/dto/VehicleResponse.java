package com.campusride.userservice.dto;

import com.campusride.userservice.enums.OwnershipType;
import com.campusride.userservice.enums.VehicleStatus;
import com.campusride.userservice.enums.VehicleType;

import java.time.LocalDateTime;

public record VehicleResponse(
        Long id,
        String vehicleNumber,
        VehicleType vehicleType,
        String vehicleModel,
        String vehicleColor,
        Integer totalSeats,
        OwnershipType ownershipType,
        VehicleStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
