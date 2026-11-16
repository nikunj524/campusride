package com.campusride.rideservice.service.impl;

import com.campusride.rideservice.dto.CreateRideRequest;
import com.campusride.rideservice.dto.RideResponse;
import com.campusride.rideservice.dto.SearchRideRequest;
import com.campusride.rideservice.entity.Ride;
import com.campusride.rideservice.enums.RideStatus;
import com.campusride.rideservice.exception.InvalidRideStatusException;
import com.campusride.rideservice.exception.RideNotFoundException;
import com.campusride.rideservice.repository.RideRepository;
import com.campusride.rideservice.security.AuthenticatedUser;
import com.campusride.rideservice.service.RideService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;
    private final com.campusride.rideservice.client.UserServiceClient userServiceClient;

    public RideServiceImpl(RideRepository rideRepository, com.campusride.rideservice.client.UserServiceClient userServiceClient) {
        this.rideRepository = rideRepository;
        this.userServiceClient = userServiceClient;
    }

    @Override
    @Transactional
    public RideResponse createRide(CreateRideRequest request, AuthenticatedUser currentUser) {
        requireRole(currentUser, "DRIVER");
        requireMatchingUserId(request.driverId(), currentUser.userId());
        Ride ride = Ride.builder()
                .driverId(request.driverId())
                .type(request.type())
                .source(request.source())
                .destination(request.destination())
                .departureTime(request.departureTime())
                .availableSeats(request.availableSeats())
                .totalSeats(request.availableSeats())
                .reservedSeats(0)
                .capacityReviewRequired(false)
                .pricePerSeat(request.pricePerSeat())
                .notes(request.notes())
                .build();

        Ride savedRide = rideRepository.save(ride);
        return toResponse(savedRide);
    }

    @Override
    public RideResponse getRide(Long rideId, AuthenticatedUser currentUser) {
        Ride ride = findRide(rideId);
        if (!isPublicPendingRide(ride) && !isAdmin(currentUser)
                && !isRideParticipant(ride, currentUser.userId())) {
            throw new AccessDeniedException("You do not have access to this ride");
        }
        return toResponse(ride);
    }

    @Override
    public List<RideResponse> getAllAvailableRides(AuthenticatedUser currentUser) {
        return rideRepository.findByTypeAndStatusAndDepartureTimeAfter(
                com.campusride.rideservice.enums.RideType.OFFERING,
                RideStatus.PENDING,
                LocalDateTime.now()
        ).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<RideResponse> searchRides(SearchRideRequest request, AuthenticatedUser currentUser) {
        return rideRepository.findByTypeAndStatusAndDepartureTimeAfter(
                request.type(),
                RideStatus.PENDING,
                LocalDateTime.now()
        ).stream()
                .filter(ride -> matchesLocation(ride, request.source(), request.destination()))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public RideResponse acceptRide(Long rideId, Long passengerId, AuthenticatedUser currentUser) {
        requirePassengerRole(currentUser);
        requireMatchingUserId(passengerId, currentUser.userId());
        Ride ride = findRide(rideId);
        if (ride.getDriverId().equals(currentUser.userId())) {
            throw new AccessDeniedException("Drivers cannot accept their own rides");
        }
        if (ride.getStatus() != RideStatus.PENDING) {
            throw new InvalidRideStatusException("Ride cannot be accepted in current status");
        }
        ride.acceptRide(passengerId);
        return toResponse(ride);
    }

    @Override
    @Transactional
    public RideResponse startRide(Long rideId, AuthenticatedUser currentUser) {
        Ride ride = findRide(rideId);
        requireDriverOwnership(ride, currentUser);
        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new InvalidRideStatusException("Ride must be accepted before starting");
        }
        ride.startRide();
        return toResponse(ride);
    }

    @Override
    @Transactional
    public RideResponse completeRide(Long rideId, AuthenticatedUser currentUser) {
        Ride ride = findRide(rideId);
        requireDriverOwnership(ride, currentUser);
        if (ride.getStatus() != RideStatus.STARTED) {
            throw new InvalidRideStatusException("Ride must be started before completing");
        }
        ride.completeRide();
        return toResponse(ride);
    }

    @Override
    @Transactional
    public RideResponse cancelRide(Long rideId, AuthenticatedUser currentUser) {
        Ride ride = findRide(rideId);
        if (!isAdmin(currentUser) && !isRideParticipant(ride, currentUser.userId())) {
            throw new AccessDeniedException("Only ride participants can cancel this ride");
        }
        if (ride.getStatus() == RideStatus.COMPLETED) {
            throw new InvalidRideStatusException("Cannot cancel a completed ride");
        }
        ride.cancelRide();
        return toResponse(ride);
    }

    @Override
    public List<RideResponse> getDriverRides(Long driverId, AuthenticatedUser currentUser) {
        if (!isAdmin(currentUser)) {
            requireRole(currentUser, "DRIVER");
            requireMatchingUserId(driverId, currentUser.userId());
        }
        return rideRepository.findByDriverId(driverId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<RideResponse> getPassengerRides(Long passengerId, AuthenticatedUser currentUser) {
        if (!isAdmin(currentUser)) {
            requirePassengerRole(currentUser);
            requireMatchingUserId(passengerId, currentUser.userId());
        }
        return rideRepository.findByPassengerId(passengerId).stream()
                .map(this::toResponse)
                .toList();
    }

    private Ride findRide(Long rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException(rideId));
    }

    private boolean isPublicPendingRide(Ride ride) {
        return ride.getType() == com.campusride.rideservice.enums.RideType.OFFERING
                && ride.getStatus() == RideStatus.PENDING;
    }

    private boolean isRideParticipant(Ride ride, Long userId) {
        return ride.getDriverId().equals(userId) || userId.equals(ride.getPassengerId());
    }

    private boolean isAdmin(AuthenticatedUser currentUser) {
        return "ADMIN".equals(currentUser.role());
    }

    private void requireDriverOwnership(Ride ride, AuthenticatedUser currentUser) {
        requireRole(currentUser, "DRIVER");
        requireMatchingUserId(ride.getDriverId(), currentUser.userId());
    }

    private void requirePassengerRole(AuthenticatedUser currentUser) {
        if (!"STUDENT".equals(currentUser.role()) && !"DRIVER".equals(currentUser.role())) {
            throw new AccessDeniedException("Only students or drivers can book rides");
        }
    }

    private void requireRole(AuthenticatedUser currentUser, String role) {
        if (!role.equals(currentUser.role())) {
            throw new AccessDeniedException("Required role: " + role);
        }
    }

    private void requireMatchingUserId(Long requestedUserId, Long authenticatedUserId) {
        if (!requestedUserId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("You cannot act on behalf of another user");
        }
    }

    private RideResponse toResponse(Ride ride) {
        com.campusride.rideservice.dto.VehicleInfo vehicleInfo = null;
        
        // Fetch vehicle info if ride has a driver
        if (ride.getDriverId() != null) {
            vehicleInfo = userServiceClient.getDriverVehicle(ride.getDriverId());
        }
        
        return new RideResponse(
                ride.getId(),
                ride.getDriverId(),
                ride.getPassengerId(),
                ride.getType(),
                ride.getSource(),
                ride.getDestination(),
                ride.getDepartureTime(),
                ride.getAvailableSeats(),
                ride.getPricePerSeat(),
                ride.getStatus(),
                ride.getNotes(),
                ride.getCreatedAt(),
                vehicleInfo
        );
    }

    private boolean matchesLocation(Ride ride, String source, String destination) {
        if (source != null && !ride.getSource().toLowerCase().contains(source.toLowerCase())) {
            return false;
        }
        if (destination != null && !ride.getDestination().toLowerCase().contains(destination.toLowerCase())) {
            return false;
        }
        return true;
    }
}
