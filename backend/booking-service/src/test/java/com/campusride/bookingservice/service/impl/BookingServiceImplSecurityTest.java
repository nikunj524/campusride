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
import static org.mockito.Mockito.verify;
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

    @Test
    void createsPendingBookingWithoutReservingSeats() {
        BookingRequest request = new BookingRequest();
        request.setRideId(7L);
        request.setPassengerId(20L);
        request.setSeats(2);
        RideResponse ride = RideResponse.builder().id(7L).availableSeats(4).pricePerSeat(BigDecimal.TEN).build();
        when(rideServiceClient.getRideById(7L)).thenReturn(ride);
        when(bookingRepository.save(org.mockito.ArgumentMatchers.any(Booking.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = bookingService.createBooking(request, new AuthenticatedUser(20L, "student@test", "STUDENT"));

        org.junit.jupiter.api.Assertions.assertEquals(BookingStatus.PENDING, response.getStatus());
        verify(bookingRepository).save(org.mockito.ArgumentMatchers.any(Booking.class));
        verify(rideServiceClient).getRideById(7L);
    }

    @Test
    void confirmsOnceAndReleasesOnlyConfirmedReservationOnCancellation() throws Exception {
        Booking booking = booking(7L, 20L);
        RideResponse ride = RideResponse.builder().id(7L).driverId(10L).build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(rideServiceClient.getRideById(7L)).thenReturn(ride);
        when(bookingRepository.save(booking)).thenReturn(booking);
        AuthenticatedUser driver = new AuthenticatedUser(10L, "driver@test", "DRIVER");

        bookingService.updateBookingStatus(1L, BookingStatus.CONFIRMED, driver);
        bookingService.updateBookingStatus(1L, BookingStatus.CONFIRMED, driver);
        bookingService.cancelBooking(1L, new AuthenticatedUser(20L, "student@test", "STUDENT"));

        verify(rideServiceClient).reserveBookingSeats(7L, 1L, 20L, 1);
        verify(rideServiceClient).releaseBookingSeats(7L, 1L);
        org.junit.jupiter.api.Assertions.assertEquals(BookingStatus.CANCELLED, booking.getStatus());
    }

    @Test
    void driverRejectionOfPendingBookingDoesNotReleaseCapacity() throws Exception {
        Booking booking = booking(7L, 20L);
        RideResponse ride = RideResponse.builder().id(7L).driverId(10L).build();
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(rideServiceClient.getRideById(7L)).thenReturn(ride);
        when(bookingRepository.save(booking)).thenReturn(booking);

        bookingService.updateBookingStatus(1L, BookingStatus.CANCELLED,
                new AuthenticatedUser(10L, "driver@test", "DRIVER"));

        org.junit.jupiter.api.Assertions.assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        org.mockito.Mockito.verify(rideServiceClient, org.mockito.Mockito.never())
                .releaseBookingSeats(org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyLong());
    }

    private Booking booking(Long rideId, Long passengerId) throws Exception {
        Booking booking = Booking.create(rideId, passengerId, 1, BigDecimal.TEN);
        Field id = Booking.class.getDeclaredField("id");
        id.setAccessible(true);
        id.set(booking, 1L);
        return booking;
    }
}
