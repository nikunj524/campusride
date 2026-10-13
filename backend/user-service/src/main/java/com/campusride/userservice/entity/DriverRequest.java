package com.campusride.userservice.entity;

import com.campusride.userservice.enums.DriverRequestStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "driver_requests")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DriverRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DriverRequestStatus status;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    public static DriverRequest submit(Long userId) {
        DriverRequest request = new DriverRequest();
        request.userId = userId;
        request.status = DriverRequestStatus.PENDING;
        request.requestedAt = LocalDateTime.now();
        return request;
    }

    public void approve(Long adminId) {
        review(DriverRequestStatus.APPROVED, adminId);
    }

    public void reject(Long adminId) {
        review(DriverRequestStatus.REJECTED, adminId);
    }

    public void revoke(Long adminId) {
        review(DriverRequestStatus.REVOKED, adminId);
    }

    private void review(DriverRequestStatus nextStatus, Long adminId) {
        this.status = nextStatus;
        this.reviewedAt = LocalDateTime.now();
        this.reviewedBy = adminId;
    }

    @PrePersist
    void onCreate() {
        if (this.requestedAt == null) {
            this.requestedAt = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = DriverRequestStatus.PENDING;
        }
    }
}
