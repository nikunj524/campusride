package com.campusride.rideservice.service.impl;

import com.campusride.rideservice.client.UserServiceClient;
import com.campusride.rideservice.dto.SearchRideRequest;
import com.campusride.rideservice.dto.VehicleInfo;
import com.campusride.rideservice.entity.Ride;
import com.campusride.rideservice.enums.RideStatus;
import com.campusride.rideservice.enums.RideType;
import com.campusride.rideservice.repository.RideRepository;
import com.campusride.rideservice.repository.RideReservationRepository;
import com.campusride.rideservice.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RideServiceImplSearchTest {
    private final RideRepository rides = mock(RideRepository.class);
    private final UserServiceClient users = mock(UserServiceClient.class);
    private final RideServiceImpl service = new RideServiceImpl(rides, mock(RideReservationRepository.class), users);
    private final AuthenticatedUser student = new AuthenticatedUser(9L, "student@test", "STUDENT");

    @Test
    void returnsOnlyReservationAvailableRidesMatchingVehicleType() {
        Ride available = ride(1L, 2, 3, 1, false);
        Ride bike = ride(3L, 2, 3, 1, false);
        Ride underReview = ride(2L, 2, 3, 1, true);
        when(rides.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(available, bike, underReview));
        when(users.getDriverVehicle(1L)).thenReturn(new VehicleInfo("GJ01AA1", "CAR", "Model", "Blue", 3));
        when(users.getDriverVehicle(3L)).thenReturn(new VehicleInfo("GJ01BB2", "BIKE", "Model", "Black", 3));

        var result = service.searchRides(new SearchRideRequest(RideType.OFFERING, " campus ", "city", null,
                2, "car", BigDecimal.TEN, BigDecimal.valueOf(100), SearchRideRequest.SortOrder.LOWEST_PRICE), student);

        assertEquals(List.of(1L), result.stream().map(response -> response.id()).toList());
        verify(rides).findAll(any(org.springframework.data.jpa.domain.Specification.class),
                eq(org.springframework.data.domain.Sort.by("pricePerSeat").ascending()
                        .and(org.springframework.data.domain.Sort.by("id").ascending())));

        var bikeRequest = new SearchRideRequest(RideType.OFFERING, " campus ", "city", null,
                2, "BIKE", BigDecimal.TEN, BigDecimal.valueOf(100), SearchRideRequest.SortOrder.LOWEST_PRICE);
        assertEquals(List.of(3L), ids(service.searchRides(bikeRequest, student)));
    }

    @Test
    void rejectsAnInvertedPriceRangeBeforeQuerying() {
        var request = new SearchRideRequest(RideType.OFFERING, null, null, null, null, null,
                BigDecimal.valueOf(200), BigDecimal.valueOf(100), null);
        assertThrows(IllegalArgumentException.class, () -> service.searchRides(request, student));
        verifyNoInteractions(rides);
    }

    @Test
    void threeOrMoreSeatsExcludesOneAndTwoSeatRidesAndKeepsExactBoundary() {
        when(rides.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(ride(1L, 1, 1, 0, false), ride(2L, 2, 2, 0, false), ride(3L, 3, 3, 0, false)));

        var result = search(3, null, null, null);

        assertEquals(List.of(3L), result.stream().map(response -> response.id()).toList());
    }

    @Test
    void everySeatThresholdUsesAvailableRatherThanTotalSeats() {
        Ride oneAvailable = ride(1L, 1, 4, 3, false);
        Ride twoAvailable = ride(2L, 2, 4, 2, false);
        Ride threeAvailable = ride(3L, 3, 4, 1, false);
        Ride fourAvailable = ride(4L, 4, 4, 0, false);
        when(rides.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(oneAvailable, twoAvailable, threeAvailable, fourAvailable));

        assertEquals(List.of(1L, 2L, 3L, 4L), ids(search(1, null, null, null)));
        assertEquals(List.of(2L, 3L, 4L), ids(search(2, null, null, null)));
        assertEquals(List.of(3L, 4L), ids(search(3, null, null, null)));
        assertEquals(List.of(4L), ids(search(4, null, null, null)));
    }

    @Test
    void rejectsPastTravelDateBeforeQuerying() {
        var request = new SearchRideRequest(RideType.OFFERING, null, null, LocalDate.now().minusDays(1), null, null,
                null, null, null);
        assertThrows(IllegalArgumentException.class, () -> service.searchRides(request, student));
        verifyNoInteractions(rides);
    }

    @Test
    void minimumPriceExcludesCheaperRides() {
        Ride cheap = pricedRide(1L, 50, LocalDateTime.now().plusDays(1));
        Ride matching = pricedRide(2L, 100, LocalDateTime.now().plusDays(1));
        when(rides.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(cheap, matching));

        assertEquals(List.of(2L), ids(search(null, BigDecimal.valueOf(100), null, null)));
    }

    @Test
    void maximumPriceExcludesMoreExpensiveRides() {
        Ride matching = pricedRide(1L, 50, LocalDateTime.now().plusDays(1));
        Ride expensive = pricedRide(2L, 100, LocalDateTime.now().plusDays(1));
        when(rides.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(matching, expensive));

        assertEquals(List.of(1L), ids(search(null, null, BigDecimal.valueOf(50), null)));
    }

    @Test
    void priceRangeUsesBothBoundaries() {
        when(rides.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(pricedRide(1L, 49, LocalDateTime.now().plusDays(1)), pricedRide(2L, 75, LocalDateTime.now().plusDays(1)), pricedRide(3L, 101, LocalDateTime.now().plusDays(1))));

        assertEquals(List.of(2L), ids(search(null, BigDecimal.valueOf(50), BigDecimal.valueOf(100), null)));
    }

    @Test
    void sortingIsAppliedAfterFiltering() {
        Ride fifty = pricedRide(1L, 50, LocalDateTime.now().plusDays(1));
        Ride hundred = pricedRide(2L, 100, LocalDateTime.now().plusDays(3));
        Ride seventyFive = pricedRide(3L, 75, LocalDateTime.now().plusDays(2));
        when(rides.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(hundred, fifty, seventyFive));

        assertEquals(List.of(1L, 3L, 2L), ids(search(null, null, null, SearchRideRequest.SortOrder.LOWEST_PRICE)));
        assertEquals(List.of(2L, 3L, 1L), ids(search(null, null, null, SearchRideRequest.SortOrder.HIGHEST_PRICE)));
        assertEquals(List.of(2L, 3L, 1L), ids(search(null, null, null, SearchRideRequest.SortOrder.LATEST_RIDES)));
    }

    @Test
    void sortingUsesRideIdAsADeterministicSecondaryOrder() {
        Ride laterId = pricedRide(2L, 50, LocalDateTime.now().plusDays(1));
        Ride earlierId = pricedRide(1L, 50, LocalDateTime.now().plusDays(1));
        when(rides.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(laterId, earlierId));

        assertEquals(List.of(1L, 2L), ids(search(null, null, null, SearchRideRequest.SortOrder.LOWEST_PRICE)));
        assertEquals(List.of(1L, 2L), ids(search(null, null, null, SearchRideRequest.SortOrder.HIGHEST_PRICE)));
        assertEquals(List.of(1L, 2L), ids(search(null, null, null, SearchRideRequest.SortOrder.LATEST_RIDES)));
    }

    @Test
    void combinedCriteriaAndNoMatchReturnExpectedResults() {
        LocalDate date = LocalDate.now().plusDays(2);
        Ride insufficient = ride(3L, 2, 2, 0, false);
        // Match is the only ride on the requested route/date/seat/price combination.
        Ride datedMatch = Ride.builder().id(1L).driverId(1L).type(RideType.OFFERING).source("Campus North").destination("City")
                .departureTime(date.atTime(10, 0)).availableSeats(3).totalSeats(3).reservedSeats(0).capacityReviewRequired(false).pricePerSeat(BigDecimal.valueOf(75)).status(RideStatus.PENDING).build();
        Ride otherRoute = Ride.builder().id(2L).driverId(2L).type(RideType.OFFERING).source("Hostel").destination("City")
                .departureTime(date.atTime(10, 0)).availableSeats(3).totalSeats(3).reservedSeats(0).capacityReviewRequired(false).pricePerSeat(BigDecimal.valueOf(75)).status(RideStatus.PENDING).build();
        when(rides.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(datedMatch, otherRoute, insufficient));

        var request = new SearchRideRequest(RideType.OFFERING, "campus", "city", date, 3, null, BigDecimal.valueOf(70), BigDecimal.valueOf(80), SearchRideRequest.SortOrder.LOWEST_PRICE);
        assertEquals(List.of(1L), ids(service.searchRides(request, student)));
        assertEquals(List.of(), ids(search(4, null, null, null)));
    }

    @Test
    void dateAndVehicleFilterExcludesPreviousAndNextDayRides() {
        LocalDate selectedDate = LocalDate.now().plusDays(2);
        Ride selectedCar = pricedRide(1L, 100, selectedDate.atTime(10, 0));
        Ride previousDayCar = pricedRide(2L, 100, selectedDate.minusDays(1).atTime(10, 0));
        Ride nextDayCar = pricedRide(3L, 100, selectedDate.plusDays(1).atTime(10, 0));
        Ride selectedDayBike = pricedRide(4L, 100, selectedDate.atTime(11, 0));
        when(rides.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(org.springframework.data.domain.Sort.class)))
                .thenReturn(List.of(previousDayCar, nextDayCar, selectedDayBike, selectedCar));
        when(users.getDriverVehicle(1L)).thenReturn(new VehicleInfo("CAR1", "CAR", "Model", "Blue", 3));
        when(users.getDriverVehicle(2L)).thenReturn(new VehicleInfo("CAR2", "CAR", "Model", "Blue", 3));
        when(users.getDriverVehicle(3L)).thenReturn(new VehicleInfo("CAR3", "CAR", "Model", "Blue", 3));
        when(users.getDriverVehicle(4L)).thenReturn(new VehicleInfo("BIKE1", "BIKE", "Model", "Black", 3));

        var carRequest = new SearchRideRequest(RideType.OFFERING, null, null, selectedDate, null, "car", null, null, null);
        var bikeRequest = new SearchRideRequest(RideType.OFFERING, null, null, selectedDate, null, "bike", null, null, null);
        assertEquals(List.of(1L), ids(service.searchRides(carRequest, student)));
        assertEquals(List.of(4L), ids(service.searchRides(bikeRequest, student)));
    }

    private List<Long> ids(List<com.campusride.rideservice.dto.RideResponse> responses) {
        return responses.stream().map(response -> response.id()).toList();
    }

    private List<com.campusride.rideservice.dto.RideResponse> search(Integer seats, BigDecimal min, BigDecimal max, SearchRideRequest.SortOrder sort) {
        return service.searchRides(new SearchRideRequest(RideType.OFFERING, null, null, null, seats, null, min, max, sort), student);
    }

    private Ride pricedRide(Long id, int price, LocalDateTime departureTime) {
        return Ride.builder().id(id).driverId(id).type(RideType.OFFERING).source("Campus North").destination("City")
                .departureTime(departureTime).availableSeats(3).totalSeats(3).reservedSeats(0).capacityReviewRequired(false)
                .pricePerSeat(BigDecimal.valueOf(price)).status(RideStatus.PENDING).build();
    }

    private Ride ride(Long id, int availableSeats, int totalSeats, int reservedSeats, boolean reviewRequired) {
        return Ride.builder().id(id).driverId(id).type(RideType.OFFERING).source("Campus North").destination("City")
                .departureTime(LocalDateTime.now().plusDays(1)).availableSeats(availableSeats).totalSeats(totalSeats)
                .reservedSeats(reservedSeats).capacityReviewRequired(reviewRequired).pricePerSeat(BigDecimal.valueOf(50))
                .status(RideStatus.PENDING).build();
    }
}
