package com.campusride.userservice.service;

import com.campusride.userservice.dto.DriverRequestResponse;

import java.util.List;

public interface DriverRequestService {

    DriverRequestResponse createRequest(String studentEmail);

    DriverRequestResponse getMyRequest(String userEmail);

    List<DriverRequestResponse> getAllRequests();

    DriverRequestResponse approveRequest(Long requestId, String adminEmail);

    DriverRequestResponse rejectRequest(Long requestId, String adminEmail);

    DriverRequestResponse revokeRequest(Long requestId, String adminEmail);
}
