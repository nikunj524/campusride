package com.campusride.rideservice.entity;

import com.campusride.rideservice.enums.RideType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RideCapacityTest {

    @Test
    void maintainsAvailabilityInvariantForVerifiedReservation() {
        Ride ride = Ride.builder()
                .driverId(10L)
                .type(RideType.OFFERING)
                .availableSeats(4)
                .build();

        ride.initializeCapacity(4);
        ride.applyVerifiedReservation(2);

        assertEquals(4, ride.getTotalSeats());
        assertEquals(2, ride.getReservedSeats());
        assertEquals(2, ride.getAvailableSeats());
    }

    @Test
    void rejectsVerifiedReservationLargerThanCapacity() {
        Ride ride = Ride.builder()
                .driverId(10L)
                .type(RideType.OFFERING)
                .availableSeats(2)
                .build();
        ride.initializeCapacity(2);

        assertThrows(IllegalArgumentException.class, () -> ride.applyVerifiedReservation(3));
    }
}
