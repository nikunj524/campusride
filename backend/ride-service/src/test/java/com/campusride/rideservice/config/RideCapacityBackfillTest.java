package com.campusride.rideservice.config;

import com.campusride.rideservice.entity.Ride;
import com.campusride.rideservice.entity.RideReservation;
import com.campusride.rideservice.enums.RideStatus;
import com.campusride.rideservice.enums.RideType;
import com.campusride.rideservice.repository.RideRepository;
import com.campusride.rideservice.repository.RideReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RideCapacityBackfillTest {

    @Mock
    private RideRepository rideRepository;
    @Mock
    private RideReservationRepository reservationRepository;
    @Mock
    private JdbcTemplate jdbcTemplate;

    private RideCapacityBackfill backfill;

    @BeforeEach
    void setUp() {
        backfill = new RideCapacityBackfill(rideRepository, reservationRepository, jdbcTemplate);
    }

    @Test
    void flagsPartiallyPopulatedCapacityWithoutOverwritingIt() {
        Ride ride = acceptedRide(1L, 7L, 3, 5, null);
        when(rideRepository.findAll()).thenReturn(List.of(ride));

        backfill.run(null);

        assertEquals(5, ride.getTotalSeats());
        assertEquals(3, ride.getAvailableSeats());
        assertEquals(null, ride.getReservedSeats());
        assertTrue(ride.requiresCapacityReview());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void reconcilesAnExistingActiveReservationAndIsIdempotent() {
        Ride ride = acceptedRide(1L, 7L, 4, null, null);
        RideReservation reservation = RideReservation.historicalActive(1L, 11L, 7L, 2);
        when(rideRepository.findAll()).thenReturn(List.of(ride));
        matchingBooking(1L, 7L, 11L, 2);
        when(reservationRepository.findByBookingId(11L)).thenReturn(Optional.of(reservation));

        backfill.run(null);
        backfill.run(null);

        assertEquals(4, ride.getTotalSeats());
        assertEquals(2, ride.getReservedSeats());
        assertEquals(2, ride.getAvailableSeats());
        assertFalse(ride.requiresCapacityReview());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void flagsConflictingExistingReservationForManualReview() {
        Ride ride = acceptedRide(1L, 7L, 4, null, null);
        RideReservation reservation = RideReservation.historicalActive(1L, 11L, 7L, 3);
        when(rideRepository.findAll()).thenReturn(List.of(ride));
        matchingBooking(1L, 7L, 11L, 2);
        when(reservationRepository.findByBookingId(11L)).thenReturn(Optional.of(reservation));

        backfill.run(null);

        assertTrue(ride.requiresCapacityReview());
        assertEquals(4, ride.getTotalSeats());
        assertEquals(0, ride.getReservedSeats());
        assertEquals(4, ride.getAvailableSeats());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void flagsAcceptedRideWithoutVerifiedBookingAndDoesNotRetryIt() {
        Ride ride = acceptedRide(4L, 17L, 1, null, null);
        when(rideRepository.findAll()).thenReturn(List.of(ride));
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(4L), eq(17L))).thenReturn(List.of());

        backfill.run(null);
        backfill.run(null);

        assertTrue(ride.requiresCapacityReview());
        assertEquals(1, ride.getTotalSeats());
        assertEquals(0, ride.getReservedSeats());
        assertEquals(1, ride.getAvailableSeats());
        verify(reservationRepository, never()).save(any());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void matchingBooking(Long rideId, Long passengerId, Long bookingId, Integer seats) {
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), eq(rideId), eq(passengerId)))
                .thenReturn(List.of(new RideCapacityBackfill.ConfirmedBooking(bookingId, seats)));
    }

    private Ride acceptedRide(
            Long id, Long passengerId, Integer availableSeats, Integer totalSeats, Integer reservedSeats) {
        return Ride.builder()
                .id(id)
                .driverId(10L)
                .passengerId(passengerId)
                .type(RideType.OFFERING)
                .availableSeats(availableSeats)
                .totalSeats(totalSeats)
                .reservedSeats(reservedSeats)
                .status(RideStatus.ACCEPTED)
                .build();
    }
}
