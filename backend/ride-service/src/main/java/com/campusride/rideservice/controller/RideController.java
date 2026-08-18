package com.campusride.rideservice.controller;

import com.campusride.rideservice.dto.CreateRideRequest;
import com.campusride.rideservice.dto.RideResponse;
import com.campusride.rideservice.dto.SearchRideRequest;
import com.campusride.rideservice.service.RideService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<RideResponse> createRide(@Valid @RequestBody CreateRideRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rideService.createRide(request));
    }

    @GetMapping("/{rideId}")
    public ResponseEntity<RideResponse> getRide(@PathVariable Long rideId) {
        return ResponseEntity.ok(rideService.getRide(rideId));
    }

    @PostMapping("/search")
    public ResponseEntity<List<RideResponse>> searchRides(@RequestBody SearchRideRequest request) {
        return ResponseEntity.ok(rideService.searchRides(request));
    }

    @PatchMapping("/{rideId}/accept")
    public ResponseEntity<RideResponse> acceptRide(
            @PathVariable Long rideId,
            @RequestParam Long passengerId) {
        return ResponseEntity.ok(rideService.acceptRide(rideId, passengerId));
    }

    @PatchMapping("/{rideId}/start")
    public ResponseEntity<RideResponse> startRide(@PathVariable Long rideId) {
        return ResponseEntity.ok(rideService.startRide(rideId));
    }

    @PatchMapping("/{rideId}/complete")
    public ResponseEntity<RideResponse> completeRide(@PathVariable Long rideId) {
        return ResponseEntity.ok(rideService.completeRide(rideId));
    }

    @PatchMapping("/{rideId}/cancel")
    public ResponseEntity<RideResponse> cancelRide(@PathVariable Long rideId) {
        return ResponseEntity.ok(rideService.cancelRide(rideId));
    }

    @GetMapping("/driver/{driverId}")
    public ResponseEntity<List<RideResponse>> getDriverRides(@PathVariable Long driverId) {
        return ResponseEntity.ok(rideService.getDriverRides(driverId));
    }

    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<RideResponse>> getPassengerRides(@PathVariable Long passengerId) {
        return ResponseEntity.ok(rideService.getPassengerRides(passengerId));
    }
}
