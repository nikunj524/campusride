package com.campusride.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSettings {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "site_name")
    private String siteName;
    
    @Column(name = "support_email")
    private String supportEmail;
    
    @Column(name = "support_phone")
    private String supportPhone;
    
    @Column(name = "require_driver_approval")
    private Boolean requireDriverApproval;
    
    @Column(name = "allow_registration")
    private Boolean allowRegistration;
    
    @Column(name = "max_rides_per_day")
    private Integer maxRidesPerDay;
    
    @Column(name = "min_driver_rating")
    private Double minDriverRating;
    
    @Column(name = "max_passengers_per_ride")
    private Integer maxPassengersPerRide;
    
    @Column(name = "enable_email_notifications")
    private Boolean enableEmailNotifications;
    
    @Column(name = "enable_sms_notifications")
    private Boolean enableSmsNotifications;
    
    @Column(name = "auto_approve_drivers")
    private Boolean autoApproveDrivers;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "updated_by")
    private String updatedBy;
    
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
