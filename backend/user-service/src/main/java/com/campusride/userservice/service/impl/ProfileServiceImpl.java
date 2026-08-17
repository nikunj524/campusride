package com.campusride.userservice.service.impl;

import com.campusride.userservice.dto.ProfileResponse;
import com.campusride.userservice.dto.ProfileUpdateRequest;
import com.campusride.userservice.entity.User;
import com.campusride.userservice.exception.UserNotFoundException;
import com.campusride.userservice.repository.UserRepository;
import com.campusride.userservice.service.ProfileService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;

    public ProfileServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public ProfileResponse getProfile(Long userId) {
        return toProfileResponse(findUser(userId));
    }

    @Override
    @Transactional
    public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = findUser(userId);
        user.updateProfile(request.firstName(), request.lastName(), request.phoneNumber());
        return toProfileResponse(user);
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private ProfileResponse toProfileResponse(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole()
        );
    }
}
