package com.campusride.bookingservice.service.impl;

import com.campusride.bookingservice.dto.BookingRequest;
import com.campusride.bookingservice.dto.RideResponse;
import com.campusride.bookingservice.entity.Booking;
import com.campusride.bookingservice.enums.BookingStatus;
import com.campusride.bookingservice.repository.BookingRepository;
import com.campusride.bookingservice.security.AuthenticatedUser;
import com.campusride.bookingservice.service.RideServiceClient;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BookingServiceImplSecurityTest {

    private final BookingRepository bookingRepository = mock(BookingRepository.class);
    private final RideServiceClient rideServiceClient = mock(RideServiceClient.class);
    private final BookingServiceImpl bookingService = new BookingServiceImpl(bookingRepository, rideServiceClient);

    @Test
    void rejectsBookingForAnotherPassengerId() {
        BookingRequest request = new BookingRequest();
        request.setRideId(7L);
        request.setPassengerId(99L);
        request.setSeats(1);

        assertThrows(AccessDeniedException.class,
                () -> bookingService.createBooking(request, new AuthenticatedUser(42L, "student@test", "STUDENT")));
        verifyNoInteractions(bookingRepository, rideServiceClient);
    }

    @Test
    void rejectsBookingDetailsForAnotherPassenger() throws Exception {
        Booking booking = booking(7L, 20L);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThrows(AccessDeniedException.class,
                () -> bookingService.getBookingById(1L, new AuthenticatedUser(30L, "other@test", "STUDENT")));
        verifyNoInteractions(rideServiceClient);
    }

    @Test
    void rejectsDriverStatusUpdateForRideOwnedByAnotherDriver() throws Exception {
        Booking booking = booking(7L, 20L);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        RideResponse ride = RideResponse.builder().id(7L).driverId(10L).build();
        when(rideServiceClient.getRideById(7L)).thenReturn(ride);

        assertThrows(AccessDeniedException.class,
                () -> bookingService.updateBookingStatus(1L, BookingStatus.CONFIRMED,
                        new AuthenticatedUser(30L, "driver@test", "DRIVER")));
    }

    private Booking booking(Long rideId, Long passengerId) throws Exception {
        Booking booking = Booking.create(rideId, passengerId, 1, BigDecimal.TEN);
        Field id = Booking.class.getDeclaredField("id");
        id.setAccessible(true);
        id.set(booking, 1L);
        return booking;
    }
}
