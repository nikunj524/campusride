package com.campusride.userservice.service.impl;

import com.campusride.userservice.dto.AdminSettingsDTO;
import com.campusride.userservice.entity.AdminSettings;
import com.campusride.userservice.repository.AdminSettingsRepository;
import com.campusride.userservice.service.AdminSettingsService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminSettingsServiceImpl implements AdminSettingsService {
    
    private final AdminSettingsRepository adminSettingsRepository;
    
    @PostConstruct
    public void init() {
        initializeDefaultSettings();
    }
    
    @Override
    @Transactional(readOnly = true)
    public AdminSettingsDTO getSettings() {
        AdminSettings settings = adminSettingsRepository.findFirstByOrderByIdDesc()
                .orElseGet(this::createDefaultSettings);
        
        return mapToDTO(settings);
    }
    
    @Override
    @Transactional
    public AdminSettingsDTO updateSettings(AdminSettingsDTO settingsDTO, String adminEmail) {
        AdminSettings settings = adminSettingsRepository.findFirstByOrderByIdDesc()
                .orElseGet(this::createDefaultSettings);
        
        updateSettingsFromDTO(settings, settingsDTO);
        settings.setUpdatedBy(adminEmail);
        
        AdminSettings savedSettings = adminSettingsRepository.save(settings);
        return mapToDTO(savedSettings);
    }
    
    @Override
    @Transactional
    public void initializeDefaultSettings() {
        if (adminSettingsRepository.count() == 0) {
            createDefaultSettings();
        }
    }
    
    private AdminSettings createDefaultSettings() {
        AdminSettings settings = AdminSettings.builder()
                .siteName("CampusRide")
                .supportEmail("support@campusride.com")
                .supportPhone("+91 9999999999")
                .requireDriverApproval(true)
                .allowRegistration(true)
                .maxRidesPerDay(10)
                .minDriverRating(4.0)
                .maxPassengersPerRide(4)
                .enableEmailNotifications(true)
                .enableSmsNotifications(false)
                .autoApproveDrivers(false)
                .updatedBy("system")
                .build();
        
        return adminSettingsRepository.save(settings);
    }
    
    private void updateSettingsFromDTO(AdminSettings settings, AdminSettingsDTO dto) {
        if (dto.getSiteName() != null) settings.setSiteName(dto.getSiteName());
        if (dto.getSupportEmail() != null) settings.setSupportEmail(dto.getSupportEmail());
        if (dto.getSupportPhone() != null) settings.setSupportPhone(dto.getSupportPhone());
        if (dto.getRequireDriverApproval() != null) settings.setRequireDriverApproval(dto.getRequireDriverApproval());
        if (dto.getAllowRegistration() != null) settings.setAllowRegistration(dto.getAllowRegistration());
        if (dto.getMaxRidesPerDay() != null) settings.setMaxRidesPerDay(dto.getMaxRidesPerDay());
        if (dto.getMinDriverRating() != null) settings.setMinDriverRating(dto.getMinDriverRating());
        if (dto.getMaxPassengersPerRide() != null) settings.setMaxPassengersPerRide(dto.getMaxPassengersPerRide());
        if (dto.getEnableEmailNotifications() != null) settings.setEnableEmailNotifications(dto.getEnableEmailNotifications());
        if (dto.getEnableSmsNotifications() != null) settings.setEnableSmsNotifications(dto.getEnableSmsNotifications());
        if (dto.getAutoApproveDrivers() != null) settings.setAutoApproveDrivers(dto.getAutoApproveDrivers());
    }
    
    private AdminSettingsDTO mapToDTO(AdminSettings settings) {
        return AdminSettingsDTO.builder()
                .siteName(settings.getSiteName())
                .supportEmail(settings.getSupportEmail())
                .supportPhone(settings.getSupportPhone())
                .requireDriverApproval(settings.getRequireDriverApproval())
                .allowRegistration(settings.getAllowRegistration())
                .maxRidesPerDay(settings.getMaxRidesPerDay())
                .minDriverRating(settings.getMinDriverRating())
                .maxPassengersPerRide(settings.getMaxPassengersPerRide())
                .enableEmailNotifications(settings.getEnableEmailNotifications())
                .enableSmsNotifications(settings.getEnableSmsNotifications())
                .autoApproveDrivers(settings.getAutoApproveDrivers())
                .build();
    }
}
