package com.campusride.userservice.repository;

import com.campusride.userservice.entity.DriverRequest;
import com.campusride.userservice.enums.DriverRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriverRequestRepository extends JpaRepository<DriverRequest, Long> {

    Optional<DriverRequest> findFirstByUserIdAndStatusOrderByRequestedAtDesc(
            Long userId,
            DriverRequestStatus status
    );

    List<DriverRequest> findAllByOrderByRequestedAtDesc();
}
