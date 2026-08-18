package com.campusride.rideservice.repository;

import com.campusride.rideservice.entity.Ride;
import com.campusride.rideservice.enums.RideStatus;
import com.campusride.rideservice.enums.RideType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long> {
    List<Ride> findByDriverId(Long driverId);
    List<Ride> findByPassengerId(Long passengerId);
    List<Ride> findByStatus(RideStatus status);
    List<Ride> findByTypeAndStatusAndDepartureTimeAfter(RideType type, RideStatus status, LocalDateTime time);
}
