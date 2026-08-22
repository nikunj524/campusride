package com.campusride.userservice.service;

import com.campusride.userservice.dto.ProfileResponse;
import com.campusride.userservice.dto.ProfileUpdateRequest;

public interface ProfileService {

    ProfileResponse getProfile(Long userId);

    ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request);
}
