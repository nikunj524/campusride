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
