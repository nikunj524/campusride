package com.campusride.rideservice.service;

import com.campusride.rideservice.dto.CreateRideRequest;
import com.campusride.rideservice.dto.RideResponse;
import com.campusride.rideservice.dto.SearchRideRequest;
import com.campusride.rideservice.security.AuthenticatedUser;

import java.util.List;

public interface RideService {
    RideResponse createRide(CreateRideRequest request, AuthenticatedUser currentUser);
    RideResponse getRide(Long rideId, AuthenticatedUser currentUser);
    List<RideResponse> getAllAvailableRides(AuthenticatedUser currentUser);
    List<RideResponse> searchRides(SearchRideRequest request, AuthenticatedUser currentUser);
    RideResponse acceptRide(Long rideId, Long passengerId, AuthenticatedUser currentUser);
    void reserveBookingSeats(Long rideId, Long bookingId, Long passengerId, Integer seats, AuthenticatedUser currentUser);
    void releaseBookingSeats(Long rideId, Long bookingId, AuthenticatedUser currentUser);
    RideResponse startRide(Long rideId, AuthenticatedUser currentUser);
    RideResponse completeRide(Long rideId, AuthenticatedUser currentUser);
    RideResponse cancelRide(Long rideId, AuthenticatedUser currentUser);
    List<RideResponse> getDriverRides(Long driverId, AuthenticatedUser currentUser);
    List<RideResponse> getPassengerRides(Long passengerId, AuthenticatedUser currentUser);
}
