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
import com.campusride.bookingservice.security.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.security.access.AccessDeniedException;
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
    public BookingResponse createBooking(BookingRequest request, AuthenticatedUser currentUser) {
        requirePassengerRole(currentUser);
        requireMatchingUserId(request.getPassengerId(), currentUser.userId());
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

        log.info("Booking created successfully with ID: {}", booking.getId());
        return mapToResponse(booking);
    }

    @Override
    public BookingResponse getBookingById(Long id, AuthenticatedUser currentUser) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + id));
        requireBookingAccess(booking, currentUser);
        return mapToResponse(booking);
    }

    @Override
    public List<BookingResponse> getBookingsByPassengerId(Long passengerId, AuthenticatedUser currentUser) {
        if (!isAdmin(currentUser)) {
            requirePassengerRole(currentUser);
            requireMatchingUserId(passengerId, currentUser.userId());
        }
        return bookingRepository.findByPassengerId(passengerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingResponse> getBookingsByRideId(Long rideId, AuthenticatedUser currentUser) {
        requireRideOwnerOrAdmin(rideId, currentUser);
        return bookingRepository.findByRideId(rideId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BookingResponse updateBookingStatus(Long id, BookingStatus status, AuthenticatedUser currentUser) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + id));
        requireDriverRideOwner(booking.getRideId(), currentUser);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingOperationException("Cannot update a cancelled booking");
        }
        if (status == BookingStatus.CONFIRMED) {
            if (booking.getStatus() == BookingStatus.PENDING) {
                rideServiceClient.reserveBookingSeats(
                        booking.getRideId(), booking.getId(), booking.getPassengerId(), booking.getSeats());
                booking.confirm();
            } else if (booking.getStatus() != BookingStatus.CONFIRMED) {
                throw new InvalidBookingOperationException("Only pending bookings can be confirmed");
            }
        } else if (status == BookingStatus.CANCELLED) {
            if (booking.getStatus() == BookingStatus.CONFIRMED) {
                rideServiceClient.releaseBookingSeats(booking.getRideId(), booking.getId());
            }
            booking.cancel();
        } else if (status == BookingStatus.COMPLETED && booking.getStatus() == BookingStatus.CONFIRMED) {
            booking.complete();
        } else if (status != booking.getStatus()) {
            throw new InvalidBookingOperationException("Unsupported booking status transition");
        }
        booking = bookingRepository.save(booking);

        log.info("Booking {} status updated to {}", id, status);
        return mapToResponse(booking);
    }

    @Override
    @Transactional
    public BookingResponse cancelBooking(Long id, AuthenticatedUser currentUser) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found with ID: " + id));
        if (!booking.getPassengerId().equals(currentUser.userId())) {
            requireDriverRideOwner(booking.getRideId(), currentUser);
        }

        if (booking.getStatus() == BookingStatus.COMPLETED) {
            throw new InvalidBookingOperationException("Cannot cancel a completed booking");
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingOperationException("Booking is already cancelled");
        }

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            rideServiceClient.releaseBookingSeats(booking.getRideId(), booking.getId());
        }
        booking.cancel();
        booking = bookingRepository.save(booking);

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

    private void requireBookingAccess(Booking booking, AuthenticatedUser currentUser) {
        if (isAdmin(currentUser) || booking.getPassengerId().equals(currentUser.userId())) {
            return;
        }
        requireDriverRideOwner(booking.getRideId(), currentUser);
    }

    private void requireRideOwnerOrAdmin(Long rideId, AuthenticatedUser currentUser) {
        if (!isAdmin(currentUser)) {
            requireDriverRideOwner(rideId, currentUser);
        }
    }

    private void requireDriverRideOwner(Long rideId, AuthenticatedUser currentUser) {
        requireRole(currentUser, "DRIVER");
        Long driverId = rideServiceClient.getRideById(rideId).getDriverId();
        requireMatchingUserId(driverId, currentUser.userId());
    }

    private void requirePassengerRole(AuthenticatedUser currentUser) {
        if (!"STUDENT".equals(currentUser.role()) && !"DRIVER".equals(currentUser.role())) {
            throw new AccessDeniedException("Only students or drivers can manage passenger bookings");
        }
    }

    private boolean isAdmin(AuthenticatedUser currentUser) {
        return "ADMIN".equals(currentUser.role());
    }

    private void requireRole(AuthenticatedUser currentUser, String role) {
        if (!role.equals(currentUser.role())) {
            throw new AccessDeniedException("Required role: " + role);
        }
    }

    private void requireMatchingUserId(Long requestedUserId, Long authenticatedUserId) {
        if (!requestedUserId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("You cannot act on behalf of another user");
        }
    }
}
