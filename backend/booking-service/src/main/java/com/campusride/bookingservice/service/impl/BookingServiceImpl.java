package com.campusride.bookingservice.service.impl;

import com.campusride.bookingservice.dto.BookingRequest;
import com.campusride.bookingservice.dto.BookingResponse;
import com.campusride.bookingservice.entity.Booking;
import com.campusride.bookingservice.enums.BookingStatus;
import com.campusride.bookingservice.exception.BookingNotFoundException;
import com.campusride.bookingservice.exception.InvalidBookingOperationException;
import com.campusride.bookingservice.repository.BookingRepository;
import com.campusride.bookingservice.service.BookingService;
import com.campusride.bookingservice.service.RideServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final RideServiceClient rideServiceClient;

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Creating booking for ride {} by passenger {}", request.getRideId(), request.getPassengerId());

        // Fetch ride details from Ride Service
        var rideDetails = rideServiceClient.getRideById(request.getRideId());

        // Validate ride availability
        if (rideDetails.getAvailableSeats() < request.getSeats()) {
            throw new InvalidBookingOperationException("Not enough seats available");
        }

        // Calculate total price
        BigDecimal totalPrice = rideDetails.getPricePerSeat()
                .multiply(BigDecimal.valueOf(request.getSeats()));

        // Create booking
        Booking booking = Booking.create(
                request.getRideId(),
                request.getPassengerId(),
                request.getSeats(),
                totalPrice
        );

        booking = bookingRepository.save(booking);

        // Update ride with passenger info via Ride Service
        rideServiceClient.acceptRide(request.getRideId(), request.getPassengerId());

        log.info("Booking created successfully with ID: {}", booking.getId());
        return mapToResponse(booking);
    }

    @Override
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + id));
        return mapToResponse(booking);
    }

    @Override
    public List<BookingResponse> getBookingsByPassengerId(Long passengerId) {
        return bookingRepository.findByPassengerId(passengerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getBookingsByRideId(Long rideId) {
        return bookingRepository.findByRideId(rideId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponse updateBookingStatus(Long id, BookingStatus status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + id));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingOperationException("Cannot update a cancelled booking");
        }

        booking.setStatus(status);
        booking = bookingRepository.save(booking);

        log.info("Booking {} status updated to {}", id, status);
        return mapToResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + id));

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new InvalidBookingOperationException("Cannot cancel a completed booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingOperationException("Booking is already cancelled");
        }

        booking.cancel();
        booking = bookingRepository.save(booking);

        // Optionally notify Ride Service about cancellation
        // rideServiceClient.cancelRide(booking.getRideId());

        log.info("Booking {} cancelled successfully", id);
        return mapToResponse(booking);
    }

    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .rideId(booking.getRideId())
                .passengerId(booking.getPassengerId())
                .status(booking.getStatus())
                .seats(booking.getSeats())
                .totalPrice(booking.getTotalPrice())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }
}
