package com.campusride.rideservice.entity;

import com.campusride.rideservice.enums.ReservationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ride_reservations",
        uniqueConstraints = @UniqueConstraint(name = "uk_ride_reservations_booking_id", columnNames = "booking_id"),
        indexes = @Index(name = "idx_ride_reservations_ride_id", columnList = "ride_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RideReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ride_id", nullable = false)
    private Long rideId;

    @Column(name = "booking_id", nullable = false, unique = true)
    private Long bookingId;

    @Column(name = "passenger_id", nullable = false)
    private Long passengerId;

    @Column(nullable = false)
    private Integer seats;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private RideReservation(Long rideId, Long bookingId, Long passengerId, Integer seats) {
        this.rideId = rideId;
        this.bookingId = bookingId;
        this.passengerId = passengerId;
        this.seats = seats;
        this.status = ReservationStatus.ACTIVE;
    }

    public static RideReservation historicalActive(
            Long rideId, Long bookingId, Long passengerId, Integer seats) {
        return new RideReservation(rideId, bookingId, passengerId, seats);
    }
}
