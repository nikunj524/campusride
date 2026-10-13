package com.campusride.userservice.service;

import com.campusride.userservice.dto.ProfileResponse;
import com.campusride.userservice.dto.ProfileUpdateRequest;

import java.util.List;

public interface ProfileService {

    List<ProfileResponse> getAllUsers();

    ProfileResponse getProfile(Long userId);

    ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request);
}
