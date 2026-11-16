package com.campusride.rideservice.controller;

import com.campusride.rideservice.dto.CreateRideRequest;
import com.campusride.rideservice.dto.RideResponse;
import com.campusride.rideservice.dto.SearchRideRequest;
import com.campusride.rideservice.security.AuthenticatedUser;
import com.campusride.rideservice.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rides")
public class RideController {

    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    @PostMapping
    public ResponseEntity<RideResponse> createRide(@Valid @RequestBody CreateRideRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rideService.createRide(request, currentUser(authentication)));
    }

    @GetMapping
    public ResponseEntity<List<RideResponse>> getAllAvailableRides(Authentication authentication) {
        return ResponseEntity.ok(rideService.getAllAvailableRides(currentUser(authentication)));
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<RideResponse> getRide(@PathVariable Long rideId, Authentication authentication) {
        return ResponseEntity.ok(rideService.getRide(rideId, currentUser(authentication)));
    }

    @PostMapping("/search")
    public ResponseEntity<List<RideResponse>> searchRides(@Valid @RequestBody SearchRideRequest request, Authentication authentication) {
        return ResponseEntity.ok(rideService.searchRides(request, currentUser(authentication)));
    }

    @PatchMapping("/{rideId}/accept")
    public ResponseEntity<RideResponse> acceptRide(
            @PathVariable Long rideId,
            @RequestParam Long passengerId,
            Authentication authentication) {
        return ResponseEntity.ok(rideService.acceptRide(rideId, passengerId, currentUser(authentication)));
    }

    @PostMapping("/{rideId}/reservations/{bookingId}")
    public ResponseEntity<Void> reserveBookingSeats(
            @PathVariable Long rideId,
            @PathVariable Long bookingId,
            @RequestParam Long passengerId,
            @RequestParam Integer seats,
            Authentication authentication) {
        rideService.reserveBookingSeats(rideId, bookingId, passengerId, seats, currentUser(authentication));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{rideId}/reservations/{bookingId}/release")
    public ResponseEntity<Void> releaseBookingSeats(
            @PathVariable Long rideId,
            @PathVariable Long bookingId,
            Authentication authentication) {
        rideService.releaseBookingSeats(rideId, bookingId, currentUser(authentication));
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{rideId}/start")
    public ResponseEntity<RideResponse> startRide(@PathVariable Long rideId, Authentication authentication) {
        return ResponseEntity.ok(rideService.startRide(rideId, currentUser(authentication)));
    }

    @PatchMapping("/{rideId}/complete")
    public ResponseEntity<RideResponse> completeRide(@PathVariable Long rideId, Authentication authentication) {
        return ResponseEntity.ok(rideService.completeRide(rideId, currentUser(authentication)));
    }

    @PatchMapping("/{rideId}/cancel")
    public ResponseEntity<RideResponse> cancelRide(@PathVariable Long rideId, Authentication authentication) {
        return ResponseEntity.ok(rideService.cancelRide(rideId, currentUser(authentication)));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<RideResponse>> getDriverRides(@PathVariable Long driverId, Authentication authentication) {
        return ResponseEntity.ok(rideService.getDriverRides(driverId, currentUser(authentication)));
    }

    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<RideResponse>> getPassengerRides(@PathVariable Long passengerId, Authentication authentication) {
        return ResponseEntity.ok(rideService.getPassengerRides(passengerId, currentUser(authentication)));
    }

    private AuthenticatedUser currentUser(Authentication authentication) {
        return (AuthenticatedUser) authentication.getPrincipal();
    }
}
