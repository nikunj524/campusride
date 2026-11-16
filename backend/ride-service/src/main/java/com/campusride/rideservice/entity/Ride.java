package com.campusride.rideservice.entity;

import com.campusride.rideservice.enums.RideStatus;
import com.campusride.rideservice.enums.RideType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rides")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Ride {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long driverId;

    @Column
    private Long passengerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RideType type;

    @Column(nullable = false, length = 255)
    private String source;

    @Column(nullable = false, length = 255)
    private String destination;

    @Column(nullable = false)
    private LocalDateTime departureTime;

    @Column(nullable = false)
    private Integer availableSeats;

    @Column
    private Integer totalSeats;

    @Column
    private Integer reservedSeats;

    @Column
    @Builder.Default
    private Boolean capacityReviewRequired = false;

    @Column(precision = 10, scale = 2)
    private BigDecimal pricePerSeat;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private RideStatus status = RideStatus.PENDING;

    @Column(length = 500)
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void acceptRide(Long passengerId) {
        this.passengerId = passengerId;
        this.status = RideStatus.ACCEPTED;
    }

    public void initializeCapacity(Integer totalSeats) {
        if (totalSeats == null || totalSeats < 0) {
            throw new IllegalArgumentException("Total seats must be zero or greater");
        }
        this.totalSeats = totalSeats;
        this.reservedSeats = 0;
        this.availableSeats = totalSeats;
    }

    public void applyVerifiedReservation(Integer seats) {
        if (totalSeats == null || seats == null || seats < 1 || seats > totalSeats) {
            throw new IllegalArgumentException("Verified reservation seats must fit within total seats");
        }
        this.reservedSeats = seats;
        this.availableSeats = totalSeats - reservedSeats;
    }

    public void reserveSeats(Integer seats) {
        if (requiresCapacityReview()) {
            throw new IllegalStateException("Ride capacity requires manual review before new reservations");
        }
        if (!hasCompleteAndConsistentCapacity() || seats == null || seats < 1 || seats > availableSeats) {
            throw new IllegalArgumentException("Not enough seats available");
        }
        reservedSeats += seats;
        availableSeats -= seats;
    }

    public void releaseSeats(Integer seats) {
        if (!hasCompleteAndConsistentCapacity() || seats == null || seats < 1 || seats > reservedSeats) {
            throw new IllegalArgumentException("Cannot release the requested seats");
        }
        reservedSeats -= seats;
        availableSeats += seats;
    }

    public boolean hasCompleteAndConsistentCapacity() {
        return totalSeats != null && reservedSeats != null && availableSeats != null
                && totalSeats >= 0 && reservedSeats >= 0 && reservedSeats <= totalSeats
                && availableSeats.equals(totalSeats - reservedSeats);
    }

    public boolean requiresCapacityReview() {
        return Boolean.TRUE.equals(capacityReviewRequired);
    }

    public void markCapacityReviewRequired() {
        this.capacityReviewRequired = true;
    }

    public void startRide() {
        this.status = RideStatus.STARTED;
    }

    public void completeRide() {
        this.status = RideStatus.COMPLETED;
    }

    public void cancelRide() {
        this.status = RideStatus.CANCELLED;
    }
}
