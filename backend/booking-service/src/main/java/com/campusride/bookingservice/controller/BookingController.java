package com.campusride.bookingservice.controller;

import com.campusride.bookingservice.dto.BookingRequest;
import com.campusride.bookingservice.dto.BookingResponse;
import com.campusride.bookingservice.dto.BookingUpdateRequest;
import com.campusride.bookingservice.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest request) {
        log.info("Received request to create booking for ride: {}", request.getRideId());
        BookingResponse response = bookingService.createBooking(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        log.info("Fetching booking with ID: {}", id);
        BookingResponse response = bookingService.getBookingById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/passenger/{passengerId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByPassenger(@PathVariable Long passengerId) {
        log.info("Fetching bookings for passenger: {}", passengerId);
        List<BookingResponse> bookings = bookingService.getBookingsByPassengerId(passengerId);
        return ResponseEntity.ok(bookings);
    }

    @GetMapping("/ride/{rideId}")
    public ResponseEntity<List<BookingResponse>> getBookingsByRide(@PathVariable Long rideId) {
        log.info("Fetching bookings for ride: {}", rideId);
        List<BookingResponse> bookings = bookingService.getBookingsByRideId(rideId);
        return ResponseEntity.ok(bookings);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BookingResponse> updateBookingStatus(
            @PathVariable Long id,
            @Valid @RequestBody BookingUpdateRequest request) {
        log.info("Updating booking {} status to: {}", id, request.getStatus());
        BookingResponse response = bookingService.updateBookingStatus(id, request.getStatus());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        log.info("Cancelling booking with ID: {}", id);
        BookingResponse response = bookingService.cancelBooking(id);
        return ResponseEntity.ok(response);
    }
}
