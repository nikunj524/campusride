package com.campusride.bookingservice.service;

import com.campusride.bookingservice.dto.BookingRequest;
import com.campusride.bookingservice.dto.BookingResponse;
import com.campusride.bookingservice.enums.BookingStatus;

import java.util.List;

public interface BookingService {

    BookingResponse createBooking(BookingRequest request);

    BookingResponse getBookingById(Long id);

    List<BookingResponse> getBookingsByPassengerId(Long passengerId);

    List<BookingResponse> getBookingsByRideId(Long rideId);

    BookingResponse updateBookingStatus(Long id, BookingStatus status);

    BookingResponse cancelBooking(Long id);
}
