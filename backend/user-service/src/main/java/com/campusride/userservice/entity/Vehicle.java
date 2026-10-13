package com.campusride.userservice.entity;

import com.campusride.userservice.enums.OwnershipType;
import com.campusride.userservice.enums.VehicleStatus;
import com.campusride.userservice.enums.VehicleType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "vehicles",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_vehicles_user_id", columnNames = "user_id"),
                @UniqueConstraint(name = "uk_vehicles_vehicle_number", columnNames = "vehicle_number")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "vehicle_number", nullable = false, unique = true, length = 30)
    private String vehicleNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false, length = 20)
    private VehicleType vehicleType;

    @Column(name = "vehicle_model", nullable = false, length = 80)
    private String vehicleModel;

    @Column(name = "vehicle_color", nullable = false, length = 40)
    private String vehicleColor;

    @Column(name = "total_seats", nullable = false)
    private Integer totalSeats;

    @Enumerated(EnumType.STRING)
    @Column(name = "ownership_type", nullable = false, length = 20)
    private OwnershipType ownershipType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static Vehicle register(
            Long userId,
            String vehicleNumber,
            VehicleType vehicleType,
            String vehicleModel,
            String vehicleColor,
            Integer totalSeats,
            OwnershipType ownershipType
    ) {
        Vehicle vehicle = new Vehicle();
        vehicle.userId = userId;
        vehicle.vehicleNumber = vehicleNumber;
        vehicle.vehicleType = vehicleType;
        vehicle.vehicleModel = vehicleModel;
        vehicle.vehicleColor = vehicleColor;
        vehicle.totalSeats = totalSeats;
        vehicle.ownershipType = ownershipType;
        vehicle.status = VehicleStatus.ACTIVE;
        return vehicle;
    }

    public void update(String vehicleModel, String vehicleColor, Integer totalSeats, OwnershipType ownershipType) {
        this.vehicleModel = vehicleModel;
        this.vehicleColor = vehicleColor;
        this.totalSeats = totalSeats;
        this.ownershipType = ownershipType;
        this.updatedAt = LocalDateTime.now();
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.status == null) {
            this.status = VehicleStatus.ACTIVE;
        }
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
