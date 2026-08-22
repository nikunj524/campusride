package com.campusride.userservice.service.impl;

import com.campusride.userservice.dto.VehicleCreateRequest;
import com.campusride.userservice.dto.VehicleResponse;
import com.campusride.userservice.dto.VehicleUpdateRequest;
import com.campusride.userservice.entity.User;
import com.campusride.userservice.entity.Vehicle;
import com.campusride.userservice.exception.UserNotFoundException;
import com.campusride.userservice.exception.VehicleAlreadyRegisteredException;
import com.campusride.userservice.exception.VehicleNotFoundException;
import com.campusride.userservice.exception.VehicleNumberAlreadyExistsException;
import com.campusride.userservice.repository.UserRepository;
import com.campusride.userservice.repository.VehicleRepository;
import com.campusride.userservice.service.VehicleService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class VehicleServiceImpl implements VehicleService {

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;

    public VehicleServiceImpl(UserRepository userRepository, VehicleRepository vehicleRepository) {
        this.userRepository = userRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    @Transactional
    public VehicleResponse createVehicle(String email, VehicleCreateRequest request) {
        User user = findDriver(email);
        String vehicleNumber = request.vehicleNumber().trim();
        if (vehicleRepository.existsByUserId(user.getId())) {
            throw new VehicleAlreadyRegisteredException(user.getId());
        }
        if (vehicleRepository.existsByVehicleNumber(vehicleNumber)) {
            throw new VehicleNumberAlreadyExistsException(vehicleNumber);
        }

        Vehicle vehicle = Vehicle.register(
                user.getId(),
                vehicleNumber,
                request.vehicleType(),
                request.vehicleModel().trim(),
                request.vehicleColor().trim(),
                request.totalSeats(),
                request.ownershipType()
        );
        return toResponse(vehicleRepository.save(vehicle));
    }

    @Override
    public VehicleResponse getMyVehicle(String email) {
        return toResponse(findVehicleForDriver(email));
    }

    @Override
    @Transactional
    public VehicleResponse updateMyVehicle(String email, VehicleUpdateRequest request) {
        Vehicle vehicle = findVehicleForDriver(email);
        vehicle.update(
                request.vehicleModel().trim(),
                request.vehicleColor().trim(),
                request.totalSeats(),
                request.ownershipType()
        );
        return toResponse(vehicle);
    }

    @Override
    @Transactional
    public void deleteMyVehicle(String email) {
        Vehicle vehicle = findVehicleForDriver(email);
        vehicleRepository.delete(vehicle);
    }

    private User findDriver(String email) {
        User user = findUser(email);
        if (!user.isDriverEligible()) {
            throw new AccessDeniedException("Only drivers can manage vehicles");
        }
        return user;
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    private Vehicle findVehicleForDriver(String email) {
        User user = findDriver(email);
        return vehicleRepository.findByUserId(user.getId())
                .orElseThrow(() -> new VehicleNotFoundException(user.getId()));
    }

    private VehicleResponse toResponse(Vehicle vehicle) {
        return new VehicleResponse(
                vehicle.getId(),
                vehicle.getVehicleNumber(),
                vehicle.getVehicleType(),
                vehicle.getVehicleModel(),
                vehicle.getVehicleColor(),
                vehicle.getTotalSeats(),
                vehicle.getOwnershipType(),
                vehicle.getStatus(),
                vehicle.getCreatedAt(),
                vehicle.getUpdatedAt()
        );
    }
}
