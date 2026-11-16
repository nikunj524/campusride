package com.campusride.rideservice.repository;

import com.campusride.rideservice.entity.RideReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RideReservationRepository extends JpaRepository<RideReservation, Long> {

    Optional<RideReservation> findByBookingId(Long bookingId);
}
