package com.campusride.userservice.repository;

import com.campusride.userservice.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    Optional<Vehicle> findByUserId(Long userId);

    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);

    boolean existsByUserId(Long userId);

    boolean existsByVehicleNumber(String vehicleNumber);
}
