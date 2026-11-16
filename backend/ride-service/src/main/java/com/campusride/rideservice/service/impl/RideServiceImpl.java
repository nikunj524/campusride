package com.campusride.rideservice.service.impl;

import com.campusride.rideservice.dto.CreateRideRequest;
import com.campusride.rideservice.dto.RideResponse;
import com.campusride.rideservice.dto.SearchRideRequest;
import com.campusride.rideservice.entity.Ride;
import com.campusride.rideservice.enums.RideStatus;
import com.campusride.rideservice.exception.InvalidRideStatusException;
import com.campusride.rideservice.exception.RideNotFoundException;
import com.campusride.rideservice.repository.RideRepository;
import com.campusride.rideservice.repository.RideReservationRepository;
import com.campusride.rideservice.entity.RideReservation;
import com.campusride.rideservice.security.AuthenticatedUser;
import com.campusride.rideservice.service.RideService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class RideServiceImpl implements RideService {

    private final RideRepository rideRepository;
    private final RideReservationRepository reservationRepository;
    private final com.campusride.rideservice.client.UserServiceClient userServiceClient;

    public RideServiceImpl(
            RideRepository rideRepository,
            RideReservationRepository reservationRepository,
            com.campusride.rideservice.client.UserServiceClient userServiceClient) {
        this.rideRepository = rideRepository;
        this.reservationRepository = reservationRepository;
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
        validateSearch(request);
        String source = normalize(request.source());
        String destination = normalize(request.destination());
        String vehicleType = normalize(request.vehicleType());
        LocalDateTime now = LocalDateTime.now();

        Specification<Ride> criteria = Specification.<Ride>where((root, query, cb) -> cb.equal(root.get("type"),
                        request.type() == null ? com.campusride.rideservice.enums.RideType.OFFERING : request.type()))
                .and((root, query, cb) -> cb.equal(root.get("status"), RideStatus.PENDING))
                .and((root, query, cb) -> cb.greaterThan(root.get("departureTime"), now));
        if (source != null) {
            criteria = criteria.and((root, query, cb) -> cb.like(cb.lower(cb.trim(root.get("source"))), "%" + escapeLike(source.toLowerCase(Locale.ROOT)) + "%", '\\'));
        }
        if (destination != null) {
            criteria = criteria.and((root, query, cb) -> cb.like(cb.lower(cb.trim(root.get("destination"))), "%" + escapeLike(destination.toLowerCase(Locale.ROOT)) + "%", '\\'));
        }
        if (request.travelDate() != null) {
            LocalDateTime start = request.travelDate().atStartOfDay();
            LocalDateTime end = start.plusDays(1);
            criteria = criteria.and((root, query, cb) -> cb.and(
                    cb.greaterThanOrEqualTo(root.get("departureTime"), start),
                    cb.lessThan(root.get("departureTime"), end)));
        }
        if (request.minimumAvailableSeats() != null) {
            criteria = criteria.and((root, query, cb) -> cb.and(
                    cb.isNotNull(root.get("totalSeats")),
                    cb.isNotNull(root.get("reservedSeats")),
                    cb.isNotNull(root.get("availableSeats")),
                    cb.greaterThanOrEqualTo(cb.diff(root.<Integer>get("totalSeats"), root.<Integer>get("reservedSeats")), request.minimumAvailableSeats()),
                    cb.greaterThanOrEqualTo(root.<Integer>get("availableSeats"), request.minimumAvailableSeats())));
        }
        if (request.minimumPrice() != null) {
            criteria = criteria.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("pricePerSeat"), request.minimumPrice()));
        }
        if (request.maximumPrice() != null) {
            criteria = criteria.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("pricePerSeat"), request.maximumPrice()));
        }

        List<RideResponse> results = new ArrayList<>();
        for (Ride ride : rideRepository.findAll(criteria, searchSort(request.sortOrder()))) {
            // A PENDING ride is only bookable if the reservation-managed capacity is internally valid.
            if (!isSearchAvailable(ride, request) || !matchesPrice(ride, request) || !matchesRouteAndDate(ride, source, destination, request.travelDate())) {
                continue;
            }
            RideResponse response = toResponse(ride);
            if (vehicleType == null || (response.vehicleInfo() != null
                    && vehicleType.equalsIgnoreCase(normalize(response.vehicleInfo().vehicleType())))) {
                results.add(response);
            }
        }
        results.sort(responseComparator(request.sortOrder()));
        return results;
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
    public void reserveBookingSeats(
            Long rideId, Long bookingId, Long passengerId, Integer seats, AuthenticatedUser currentUser) {
        requireRole(currentUser, "DRIVER");
        Ride ride = findRideForCapacityUpdate(rideId);
        requireMatchingUserId(ride.getDriverId(), currentUser.userId());
        if (ride.getStatus() == RideStatus.CANCELLED || ride.getStatus() == RideStatus.COMPLETED) {
            throw new IllegalStateException("Cannot reserve seats for an inactive ride");
        }

        var existing = reservationRepository.findByBookingId(bookingId);
        if (existing.isPresent()) {
            RideReservation reservation = existing.get();
            if (reservation.isActive()
                    && reservation.getRideId().equals(rideId)
                    && reservation.getPassengerId().equals(passengerId)
                    && reservation.getSeats().equals(seats)) {
                return;
            }
            throw new IllegalStateException("Booking reservation already exists with different details");
        }

        ride.reserveSeats(seats);
        reservationRepository.save(RideReservation.historicalActive(rideId, bookingId, passengerId, seats));
    }

    @Override
    @Transactional
    public void releaseBookingSeats(Long rideId, Long bookingId, AuthenticatedUser currentUser) {
        Ride ride = findRideForCapacityUpdate(rideId);
        RideReservation reservation = reservationRepository.findByBookingId(bookingId)
                .orElse(null);
        if (reservation == null || !reservation.isActive()) {
            return;
        }
        if (!reservation.getRideId().equals(rideId)) {
            throw new IllegalArgumentException("Booking reservation does not belong to this ride");
        }
        boolean isPassenger = reservation.getPassengerId().equals(currentUser.userId());
        boolean isDriver = ride.getDriverId().equals(currentUser.userId()) && "DRIVER".equals(currentUser.role());
        if (!isAdmin(currentUser) && !isPassenger && !isDriver) {
            throw new AccessDeniedException("Only the booking passenger or ride driver can release reserved seats");
        }
        ride.releaseSeats(reservation.getSeats());
        reservation.release();
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

    private Ride findRideForCapacityUpdate(Long rideId) {
        return rideRepository.findByIdForCapacityUpdate(rideId)
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

    private void validateSearch(SearchRideRequest request) {
        if (request.minimumPrice() != null && request.maximumPrice() != null
                && request.minimumPrice().compareTo(request.maximumPrice()) > 0) {
            throw new IllegalArgumentException("Minimum price cannot exceed maximum price");
        }
        if (request.travelDate() != null && request.travelDate().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Travel date cannot be in the past");
        }
    }

    private Sort searchSort(SearchRideRequest.SortOrder sortOrder) {
        if (sortOrder == SearchRideRequest.SortOrder.LOWEST_PRICE) return Sort.by("pricePerSeat").ascending().and(Sort.by("id").ascending());
        if (sortOrder == SearchRideRequest.SortOrder.HIGHEST_PRICE) return Sort.by("pricePerSeat").descending().and(Sort.by("id").ascending());
        return Sort.by("departureTime").descending().and(Sort.by("id").ascending());
    }

    private boolean isSearchAvailable(Ride ride, SearchRideRequest request) {
        if (!ride.hasCompleteAndConsistentCapacity() || ride.requiresCapacityReview() || ride.getAvailableSeats() < 1) {
            return false;
        }
        return request.minimumAvailableSeats() == null || ride.getAvailableSeats() >= request.minimumAvailableSeats();
    }

    private boolean matchesPrice(Ride ride, SearchRideRequest request) {
        BigDecimal price = ride.getPricePerSeat();
        if (price == null) {
            return request.minimumPrice() == null && request.maximumPrice() == null;
        }
        return (request.minimumPrice() == null || price.compareTo(request.minimumPrice()) >= 0)
                && (request.maximumPrice() == null || price.compareTo(request.maximumPrice()) <= 0);
    }

    private boolean matchesRouteAndDate(Ride ride, String source, String destination, LocalDate travelDate) {
        if (source != null && !ride.getSource().trim().toLowerCase(Locale.ROOT).contains(source.toLowerCase(Locale.ROOT))) {
            return false;
        }
        if (destination != null && !ride.getDestination().trim().toLowerCase(Locale.ROOT).contains(destination.toLowerCase(Locale.ROOT))) {
            return false;
        }
        return travelDate == null || ride.getDepartureTime().toLocalDate().equals(travelDate);
    }

    private Comparator<RideResponse> responseComparator(SearchRideRequest.SortOrder sortOrder) {
        if (sortOrder == SearchRideRequest.SortOrder.LOWEST_PRICE) {
            return Comparator.comparing(RideResponse::pricePerSeat, Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(RideResponse::id);
        }
        if (sortOrder == SearchRideRequest.SortOrder.HIGHEST_PRICE) {
            return Comparator.comparing(RideResponse::pricePerSeat, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(RideResponse::id);
        }
        return Comparator.comparing(RideResponse::departureTime, Comparator.reverseOrder())
                .thenComparing(RideResponse::id);
    }

    private String normalize(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private String escapeLike(String value) {
        return value.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
