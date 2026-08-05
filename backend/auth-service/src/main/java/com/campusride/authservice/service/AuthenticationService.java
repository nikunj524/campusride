package com.campusride.authservice.service;

import com.campusride.authservice.dto.AuthenticationRequest;
import com.campusride.authservice.dto.AuthenticationResponse;
import com.campusride.authservice.dto.RegisterRequest;
import com.campusride.authservice.dto.UserProfileResponse;
import com.campusride.authservice.entity.User;
import com.campusride.authservice.enums.Role;
import com.campusride.authservice.exception.DuplicateEmailException;
import com.campusride.authservice.exception.ForbiddenRegistrationRoleException;
import com.campusride.authservice.exception.InvalidCredentialsException;
import com.campusride.authservice.exception.UserNotFoundException;
import com.campusride.authservice.repository.UserRepository;
import com.campusride.authservice.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class AuthenticationService implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthenticationService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("An account with this email already exists");
        }
        if (request.role() == Role.ADMIN) {
            throw new ForbiddenRegistrationRoleException();
        }

        User user = User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.password()))
                .phoneNumber(request.phoneNumber().trim())
                .role(request.role())
                .build();

        User savedUser = userRepository.save(user);
        return authenticationResponse(savedUser);
    }

    @Override
    public AuthenticationResponse login(AuthenticationRequest request) {
        User user = userRepository.findByEmail(normalizeEmail(request.email()))
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        return authenticationResponse(user);
    }

    @Override
    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(UserNotFoundException::new);
        return toProfile(user);
    }

    private AuthenticationResponse authenticationResponse(User user) {
        return new AuthenticationResponse(jwtUtil.generateToken(user.getEmail()), "Bearer", toProfile(user));
    }

    private UserProfileResponse toProfile(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getCreatedAt()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
