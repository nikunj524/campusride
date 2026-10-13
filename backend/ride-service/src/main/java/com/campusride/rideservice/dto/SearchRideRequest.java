package com.campusride.rideservice.dto;

import com.campusride.rideservice.enums.RideType;

public record SearchRideRequest(
        RideType type,
        String source,
        String destination
) {
}
