package com.campusride.rideservice.service.impl;

import com.campusride.rideservice.client.UserServiceClient;
import com.campusride.rideservice.entity.Ride;
import com.campusride.rideservice.entity.RideReservation;
import com.campusride.rideservice.enums.RideType;
import com.campusride.rideservice.repository.RideRepository;
import com.campusride.rideservice.repository.RideReservationRepository;
import com.campusride.rideservice.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RideReservationWorkflowTest {

    private final RideRepository rideRepository = mock(RideRepository.class);
    private final RideReservationRepository reservationRepository = mock(RideReservationRepository.class);
    private final RideServiceImpl service = new RideServiceImpl(rideRepository, reservationRepository,
            mock(UserServiceClient.class));
    private final AuthenticatedUser driver = new AuthenticatedUser(10L, "driver@test", "DRIVER");
    private final AuthenticatedUser passenger = new AuthenticatedUser(20L, "student@test", "STUDENT");

    @Test
    void confirmationReservesExactSeatsAndReleaseIsIdempotent() {
        Ride ride = ride(4);
        RideReservation reservation = RideReservation.historicalActive(7L, 101L, 20L, 2);
        when(rideRepository.findByIdForCapacityUpdate(7L)).thenReturn(Optional.of(ride));
        when(reservationRepository.findByBookingId(101L)).thenReturn(Optional.empty(), Optional.of(reservation));

        service.reserveBookingSeats(7L, 101L, 20L, 2, driver);
        assertEquals(2, ride.getReservedSeats());
        assertEquals(2, ride.getAvailableSeats());
        verify(reservationRepository).save(any(RideReservation.class));

        service.releaseBookingSeats(7L, 101L, passenger);
        assertEquals(0, ride.getReservedSeats());
        assertEquals(4, ride.getAvailableSeats());

        service.releaseBookingSeats(7L, 101L, passenger);
        assertEquals(0, ride.getReservedSeats());
        assertEquals(4, ride.getAvailableSeats());
    }

    @Test
    void duplicateConfirmationDoesNotReserveSeatsTwice() {
        Ride ride = ride(4);
        RideReservation reservation = RideReservation.historicalActive(7L, 101L, 20L, 2);
        when(rideRepository.findByIdForCapacityUpdate(7L)).thenReturn(Optional.of(ride));
        when(reservationRepository.findByBookingId(101L)).thenReturn(Optional.of(reservation));

        service.reserveBookingSeats(7L, 101L, 20L, 2, driver);

        assertEquals(0, ride.getReservedSeats());
        assertEquals(4, ride.getAvailableSeats());
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void preventsOverbookingUnderTheCapacityLock() {
        Ride ride = ride(2);
        when(rideRepository.findByIdForCapacityUpdate(7L)).thenReturn(Optional.of(ride));
        when(reservationRepository.findByBookingId(101L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class,
                () -> service.reserveBookingSeats(7L, 101L, 20L, 3, driver));
        assertEquals(0, ride.getReservedSeats());
        assertEquals(2, ride.getAvailableSeats());
    }

    private Ride ride(int seats) {
        Ride ride = Ride.builder().id(7L).driverId(10L).type(RideType.OFFERING).availableSeats(seats).build();
        ride.initializeCapacity(seats);
        return ride;
    }
}
