package com.campusride.rideservice.service.impl;

import com.campusride.rideservice.dto.CreateRideRequest;
import com.campusride.rideservice.dto.RideResponse;
import com.campusride.rideservice.dto.SearchRideRequest;
import com.campusride.rideservice.entity.Ride;
import com.campusride.rideservice.enums.RideStatus;
import com.campusride.rideservice.exception.InvalidRideStatusException;
import com.campusride.rideservice.exception.RideNotFoundException;
import com.campusride.rideservice.repository.RideRepository;
import com.campusride.rideservice.service.RideService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;

    public RideServiceImpl(RideRepository rideRepository) {
        this.rideRepository = rideRepository;
    }

    @Override
    @Transactional
    public RideResponse createRide(CreateRideRequest request) {
        Ride ride = Ride.builder()
                .driverId(request.driverId())
                .type(request.type())
                .source(request.source())
                .destination(request.destination())
                .departureTime(request.departureTime())
                .availableSeats(request.availableSeats())
                .pricePerSeat(request.pricePerSeat())
                .notes(request.notes())
                .build();

        Ride savedRide = rideRepository.save(ride);
        return toResponse(savedRide);
    }

    @Override
    public RideResponse getRide(Long rideId) {
        return toResponse(findRide(rideId));
    }

    @Override
    public List<RideResponse> searchRides(SearchRideRequest request) {
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
    public RideResponse acceptRide(Long rideId, Long passengerId) {
        Ride ride = findRide(rideId);
        if (ride.getStatus() != RideStatus.PENDING) {
            throw new InvalidRideStatusException("Ride cannot be accepted in current status");
        }
        ride.acceptRide(passengerId);
        return toResponse(ride);
    }

    @Override
    @Transactional
    public RideResponse startRide(Long rideId) {
        Ride ride = findRide(rideId);
        if (ride.getStatus() != RideStatus.ACCEPTED) {
            throw new InvalidRideStatusException("Ride must be accepted before starting");
        }
        ride.startRide();
        return toResponse(ride);
    }

    @Override
    @Transactional
    public RideResponse completeRide(Long rideId) {
        Ride ride = findRide(rideId);
        if (ride.getStatus() != RideStatus.STARTED) {
            throw new InvalidRideStatusException("Ride must be started before completing");
        }
        ride.completeRide();
        return toResponse(ride);
    }

    @Override
    @Transactional
    public RideResponse cancelRide(Long rideId) {
        Ride ride = findRide(rideId);
        if (ride.getStatus() == RideStatus.COMPLETED) {
            throw new InvalidRideStatusException("Cannot cancel a completed ride");
        }
        ride.cancelRide();
        return toResponse(ride);
    }

    @Override
    public List<RideResponse> getDriverRides(Long driverId) {
        return rideRepository.findByDriverId(driverId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<RideResponse> getPassengerRides(Long passengerId) {
        return rideRepository.findByPassengerId(passengerId).stream()
                .map(this::toResponse)
                .toList();
    }

    private Ride findRide(Long rideId) {
        return rideRepository.findById(rideId)
                .orElseThrow(() -> new RideNotFoundException(rideId));
    }

    private RideResponse toResponse(Ride ride) {
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
                ride.getCreatedAt()
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
