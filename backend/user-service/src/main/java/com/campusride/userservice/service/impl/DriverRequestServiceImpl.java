package com.campusride.userservice.service.impl;

import com.campusride.userservice.dto.DriverRequestResponse;
import com.campusride.userservice.entity.DriverRequest;
import com.campusride.userservice.entity.User;
import com.campusride.userservice.enums.DriverRequestStatus;
import com.campusride.userservice.enums.Role;
import com.campusride.userservice.exception.DriverRequestConflictException;
import com.campusride.userservice.exception.DriverRequestNotFoundException;
import com.campusride.userservice.exception.UserNotFoundException;
import com.campusride.userservice.repository.DriverRequestRepository;
import com.campusride.userservice.repository.UserRepository;
import com.campusride.userservice.service.DriverRequestService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class DriverRequestServiceImpl implements DriverRequestService {

    private final DriverRequestRepository driverRequestRepository;
    private final UserRepository userRepository;

    public DriverRequestServiceImpl(
            DriverRequestRepository driverRequestRepository,
            UserRepository userRepository
    ) {
        this.driverRequestRepository = driverRequestRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public DriverRequestResponse createRequest(String studentEmail) {
        User student = findUser(studentEmail);
        if (student.getRole() != Role.STUDENT || student.isDriverEligible()) {
            throw new DriverRequestConflictException("Driver mode is already enabled for this account");
        }
        if (driverRequestRepository.findFirstByUserIdAndStatusOrderByRequestedAtDesc(
                student.getId(), DriverRequestStatus.PENDING).isPresent()) {
            throw new DriverRequestConflictException("A driver request is already pending");
        }

        return toResponse(driverRequestRepository.save(DriverRequest.submit(student.getId())));
    }

    @Override
    public DriverRequestResponse getMyRequest(String userEmail) {
        User user = findUser(userEmail);
        return driverRequestRepository.findFirstByUserIdAndStatusOrderByRequestedAtDesc(
                        user.getId(), DriverRequestStatus.PENDING)
                .or(() -> findLatestReviewedRequest(user.getId()))
                .map(this::toResponse)
                .orElse(null);
    }

    @Override
    public List<DriverRequestResponse> getAllRequests() {
        return driverRequestRepository.findAllByOrderByRequestedAtDesc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public DriverRequestResponse approveRequest(Long requestId, String adminEmail) {
        User admin = findAdmin(adminEmail);
        DriverRequest request = findRequest(requestId);
        ensurePending(request);

        User student = findUser(request.getUserId());
        student.approveDriverEligibility();
        request.approve(admin.getId());
        return toResponse(request);
    }

    @Override
    @Transactional
    public DriverRequestResponse rejectRequest(Long requestId, String adminEmail) {
        User admin = findAdmin(adminEmail);
        DriverRequest request = findRequest(requestId);
        ensurePending(request);

        request.reject(admin.getId());
        return toResponse(request);
    }

    @Override
    @Transactional
    public DriverRequestResponse revokeRequest(Long requestId, String adminEmail) {
        User admin = findAdmin(adminEmail);
        DriverRequest request = findRequest(requestId);
        ensureApproved(request);

        User driver = findUser(request.getUserId());
        driver.revokeDriverEligibility();
        request.revoke(admin.getId());
        return toResponse(request);
    }

    private java.util.Optional<DriverRequest> findLatestReviewedRequest(Long userId) {
        return driverRequestRepository.findAll().stream()
                .filter(request -> request.getUserId().equals(userId)
                        && request.getStatus() != DriverRequestStatus.PENDING)
                .max(java.util.Comparator.comparing(DriverRequest::getRequestedAt));
    }

    private void ensurePending(DriverRequest request) {
        if (request.getStatus() != DriverRequestStatus.PENDING) {
            throw new DriverRequestConflictException("Driver request has already been reviewed");
        }
    }

    private void ensureApproved(DriverRequest request) {
        if (request.getStatus() != DriverRequestStatus.APPROVED) {
            throw new DriverRequestConflictException("Only approved driver requests can be revoked");
        }
    }

    private User findAdmin(String email) {
        User admin = findUser(email);
        if (admin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Only admins can review driver requests");
        }
        return admin;
    }

    private User findUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    private DriverRequest findRequest(Long requestId) {
        return driverRequestRepository.findById(requestId)
                .orElseThrow(() -> new DriverRequestNotFoundException(requestId));
    }

    private DriverRequestResponse toResponse(DriverRequest request) {
        User user = findUser(request.getUserId());
        return new DriverRequestResponse(
                request.getId(),
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                request.getStatus(),
                request.getRequestedAt(),
                request.getReviewedAt(),
                request.getReviewedBy()
        );
    }
}
