package com.campusride.bookingservice.service;

import com.campusride.bookingservice.dto.BookingRequest;
import com.campusride.bookingservice.dto.BookingResponse;
import com.campusride.bookingservice.enums.BookingStatus;
import com.campusride.bookingservice.security.AuthenticatedUser;

import java.util.List;

public interface BookingService {

    BookingResponse createBooking(BookingRequest request, AuthenticatedUser currentUser);

    BookingResponse getBookingById(Long id, AuthenticatedUser currentUser);

    List<BookingResponse> getBookingsByPassengerId(Long passengerId, AuthenticatedUser currentUser);

    List<BookingResponse> getBookingsByRideId(Long rideId, AuthenticatedUser currentUser);

    BookingResponse updateBookingStatus(Long id, BookingStatus status, AuthenticatedUser currentUser);

    BookingResponse cancelBooking(Long id, AuthenticatedUser currentUser);
}
