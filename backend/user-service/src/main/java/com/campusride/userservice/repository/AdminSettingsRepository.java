package com.campusride.userservice.repository;

import com.campusride.userservice.entity.AdminSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminSettingsRepository extends JpaRepository<AdminSettings, Long> {
    Optional<AdminSettings> findFirstByOrderByIdDesc();
}
