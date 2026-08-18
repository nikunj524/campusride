package com.campusride.authservice.entity;

import com.campusride.authservice.enums.Role;
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

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(length = 100)
    private String passwordResetOtpHash;

    private LocalDateTime passwordResetOtpExpiresAt;

    private LocalDateTime passwordResetVerifiedUntil;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void beginPasswordReset(String otpHash, LocalDateTime otpExpiresAt) {
        this.passwordResetOtpHash = otpHash;
        this.passwordResetOtpExpiresAt = otpExpiresAt;
        this.passwordResetVerifiedUntil = null;
    }

    public void verifyPasswordReset(LocalDateTime verifiedUntil) {
        this.passwordResetOtpHash = null;
        this.passwordResetOtpExpiresAt = null;
        this.passwordResetVerifiedUntil = verifiedUntil;
    }

    public void updatePasswordAfterReset(String encodedPassword) {
        this.password = encodedPassword;
        this.passwordResetOtpHash = null;
        this.passwordResetOtpExpiresAt = null;
        this.passwordResetVerifiedUntil = null;
    }
}
