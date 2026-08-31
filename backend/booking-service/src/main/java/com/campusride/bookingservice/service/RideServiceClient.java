package com.campusride.bookingservice.service;

import com.campusride.bookingservice.dto.RideResponse;

public interface RideServiceClient {

    RideResponse getRideById(Long rideId);

    void acceptRide(Long rideId, Long passengerId);
}
