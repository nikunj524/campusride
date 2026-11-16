package com.campusride.rideservice.repository;

import com.campusride.rideservice.entity.Ride;
import com.campusride.rideservice.enums.RideStatus;
import com.campusride.rideservice.enums.RideType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.time.LocalDateTime;
import java.util.List;

public interface RideRepository extends JpaRepository<Ride, Long>, JpaSpecificationExecutor<Ride> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select ride from Ride ride where ride.id = :rideId")
    java.util.Optional<Ride> findByIdForCapacityUpdate(@Param("rideId") Long rideId);

    List<Ride> findByDriverId(Long driverId);
    List<Ride> findByPassengerId(Long passengerId);
    List<Ride> findByStatus(RideStatus status);
    List<Ride> findByTypeAndStatusAndDepartureTimeAfter(RideType type, RideStatus status, LocalDateTime time);
}
