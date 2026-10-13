package com.campusride.rideservice.dto;

public record VehicleInfo(
        String vehicleNumber,
        String vehicleType,
        String vehicleModel,
        String vehicleColor,
        Integer totalSeats
) {
}
