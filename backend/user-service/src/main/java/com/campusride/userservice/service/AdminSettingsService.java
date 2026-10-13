package com.campusride.userservice.service;

import com.campusride.userservice.dto.AdminSettingsDTO;

public interface AdminSettingsService {
    AdminSettingsDTO getSettings();
    AdminSettingsDTO updateSettings(AdminSettingsDTO settingsDTO, String adminEmail);
    void initializeDefaultSettings();
}
