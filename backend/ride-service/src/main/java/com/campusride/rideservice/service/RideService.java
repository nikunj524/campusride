package com.campusride.rideservice.service;

import com.campusride.rideservice.dto.CreateRideRequest;
import com.campusride.rideservice.dto.RideResponse;
import com.campusride.rideservice.dto.SearchRideRequest;

import java.util.List;

public interface RideService {
    RideResponse createRide(CreateRideRequest request);
    RideResponse getRide(Long rideId);
    List<RideResponse> searchRides(SearchRideRequest request);
    RideResponse acceptRide(Long rideId, Long passengerId);
    RideResponse startRide(Long rideId);
    RideResponse completeRide(Long rideId);
    RideResponse cancelRide(Long rideId);
    List<RideResponse> getDriverRides(Long driverId);
    List<RideResponse> getPassengerRides(Long passengerId);
}
