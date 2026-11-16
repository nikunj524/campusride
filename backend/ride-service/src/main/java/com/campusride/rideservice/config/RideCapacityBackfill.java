package com.campusride.rideservice.config;

import com.campusride.rideservice.entity.Ride;
import com.campusride.rideservice.entity.RideReservation;
import com.campusride.rideservice.enums.ReservationStatus;
import com.campusride.rideservice.enums.RideStatus;
import com.campusride.rideservice.repository.RideRepository;
import com.campusride.rideservice.repository.RideReservationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RideCapacityBackfill implements ApplicationRunner {

    private final RideRepository rideRepository;
    private final RideReservationRepository reservationRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int migratedRides = 0;
        int migratedReservations = 0;
        int manualReviewCount = 0;

        for (Ride ride : rideRepository.findAll()) {
            if (ride.requiresCapacityReview()) {
                continue;
            }

            if (hasPartiallyPopulatedCapacity(ride)) {
                manualReviewCount += markForManualReview(ride,
                        "capacity fields are only partially populated; existing values were preserved");
                continue;
            }

            if (ride.getTotalSeats() != null) {
                if (!ride.hasCompleteAndConsistentCapacity()) {
                    manualReviewCount += markForManualReview(ride,
                            "capacity fields are inconsistent; existing values were preserved");
                } else if (ride.getStatus() == RideStatus.ACCEPTED) {
                    manualReviewCount += validateExistingAcceptedRide(ride);
                }
                continue;
            }

            Integer originalAvailableSeats = ride.getAvailableSeats();
            if (originalAvailableSeats == null || originalAvailableSeats < 0) {
                manualReviewCount += markForManualReview(ride,
                        "availableSeats is invalid; capacity was not backfilled");
                continue;
            }

            ride.initializeCapacity(originalAvailableSeats);
            migratedRides++;

            if (ride.getStatus() != RideStatus.ACCEPTED) {
                continue;
            }

            List<ConfirmedBooking> bookings = findMatchingConfirmedBookings(ride);
            if (bookings.size() != 1) {
                manualReviewCount += markForManualReview(ride,
                        "has " + bookings.size() + " matching CONFIRMED bookings; no reservation was inferred");
                continue;
            }

            ConfirmedBooking booking = bookings.get(0);
            if (booking.seats() < 1 || booking.seats() > ride.getTotalSeats()) {
                manualReviewCount += markForManualReview(ride,
                        "and booking " + booking.id() + " have incompatible seat counts; no reservation was inferred");
                continue;
            }

            var existingReservation = reservationRepository.findByBookingId(booking.id());
            if (existingReservation.isPresent()) {
                RideReservation reservation = existingReservation.get();
                if (!matchesVerifiedReservation(ride, booking, reservation)) {
                    manualReviewCount += markForManualReview(ride,
                            "and existing reservation for booking " + booking.id() + " conflict");
                    continue;
                }
                ride.applyVerifiedReservation(reservation.getSeats());
                continue;
            }

            reservationRepository.save(RideReservation.historicalActive(
                    ride.getId(), booking.id(), ride.getPassengerId(), booking.seats()));
            ride.applyVerifiedReservation(booking.seats());
            migratedReservations++;
        }

        log.info("Ride capacity backfill complete: rides={}, reservations={}, manualReview={}",
                migratedRides, migratedReservations, manualReviewCount);
    }

    private int validateExistingAcceptedRide(Ride ride) {
        List<ConfirmedBooking> bookings = findMatchingConfirmedBookings(ride);
        if (bookings.size() != 1) {
            return markForManualReview(ride,
                    "has " + bookings.size() + " matching CONFIRMED bookings; existing capacity was preserved");
        }

        ConfirmedBooking booking = bookings.get(0);
        var existingReservation = reservationRepository.findByBookingId(booking.id());
        if (existingReservation.isEmpty()) {
            return markForManualReview(ride,
                    "has no reservation for matching booking " + booking.id() + "; existing capacity was preserved");
        }

        if (!matchesVerifiedReservation(ride, booking, existingReservation.get())
                || !ride.getReservedSeats().equals(existingReservation.get().getSeats())) {
            return markForManualReview(ride,
                    "and existing reservation for booking " + booking.id() + " conflict; existing capacity was preserved");
        }
        return 0;
    }

    private List<ConfirmedBooking> findMatchingConfirmedBookings(Ride ride) {
        return jdbcTemplate.query(
                "SELECT id, seats FROM bookings WHERE ride_id = ? AND passenger_id = ? AND status = 'CONFIRMED'",
                (resultSet, rowNum) -> new ConfirmedBooking(resultSet.getLong("id"), resultSet.getInt("seats")),
                ride.getId(), ride.getPassengerId());
    }

    private boolean hasPartiallyPopulatedCapacity(Ride ride) {
        return (ride.getTotalSeats() == null) != (ride.getReservedSeats() == null);
    }

    private boolean matchesVerifiedReservation(
            Ride ride, ConfirmedBooking booking, RideReservation reservation) {
        return reservation.getStatus() == ReservationStatus.ACTIVE
                && ride.getId().equals(reservation.getRideId())
                && ride.getPassengerId().equals(reservation.getPassengerId())
                && booking.id().equals(reservation.getBookingId())
                && booking.seats().equals(reservation.getSeats())
                && reservation.getSeats() >= 1
                && reservation.getSeats() <= ride.getTotalSeats();
    }

    private int markForManualReview(Ride ride, String reason) {
        ride.markCapacityReviewRequired();
        log.warn("MANUAL_REVIEW: Ride {} {}; future reservations must be rejected until reviewed",
                ride.getId(), reason);
        return 1;
    }

    record ConfirmedBooking(Long id, Integer seats) {
    }
}
