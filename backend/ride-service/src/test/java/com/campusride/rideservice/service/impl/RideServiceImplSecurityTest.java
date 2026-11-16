package com.campusride.rideservice.service.impl;

import com.campusride.rideservice.client.UserServiceClient;
import com.campusride.rideservice.dto.CreateRideRequest;
import com.campusride.rideservice.entity.Ride;
import com.campusride.rideservice.enums.RideStatus;
import com.campusride.rideservice.enums.RideType;
import com.campusride.rideservice.repository.RideRepository;
import com.campusride.rideservice.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class RideServiceImplSecurityTest {

    private final RideRepository rideRepository = mock(RideRepository.class);
    private final UserServiceClient userServiceClient = mock(UserServiceClient.class);
    private final RideServiceImpl rideService = new RideServiceImpl(rideRepository, userServiceClient);

    @Test
    void rejectsRideCreationForAnotherDriverId() {
        CreateRideRequest request = new CreateRideRequest(
                99L, RideType.OFFERING, "Source", "Destination", LocalDateTime.now().plusHours(1),
                2, BigDecimal.TEN, null
        );

        assertThrows(AccessDeniedException.class,
                () -> rideService.createRide(request, new AuthenticatedUser(42L, "driver@test", "DRIVER")));
        verifyNoInteractions(rideRepository);
    }

    @Test
    void rejectsAcceptanceForAnotherPassengerId() {
        assertThrows(AccessDeniedException.class,
                () -> rideService.acceptRide(7L, 99L, new AuthenticatedUser(42L, "student@test", "STUDENT")));
        verifyNoInteractions(rideRepository);
    }

    @Test
    void rejectsPrivateRideDetailsForNonParticipant() {
        Ride acceptedRide = Ride.builder()
                .id(7L)
                .driverId(10L)
                .passengerId(20L)
                .type(RideType.OFFERING)
                .source("Source")
                .destination("Destination")
                .departureTime(LocalDateTime.now().plusHours(1))
                .availableSeats(1)
                .pricePerSeat(BigDecimal.TEN)
                .status(RideStatus.ACCEPTED)
                .build();
        when(rideRepository.findById(7L)).thenReturn(Optional.of(acceptedRide));

        assertThrows(AccessDeniedException.class,
                () -> rideService.getRide(7L, new AuthenticatedUser(30L, "other@test", "STUDENT")));
    }
}
