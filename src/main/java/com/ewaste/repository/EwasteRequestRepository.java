package com.ewaste.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ewaste.entity.EwasteRequest;
import com.ewaste.enums.RequestStatus;

import java.util.*;

public interface EwasteRequestRepository extends JpaRepository<EwasteRequest, Long> {

    List<EwasteRequest> findByUserId(Long userId);

    List<EwasteRequest> findByStatus(RequestStatus status);
    List<EwasteRequest> findByAssignedWorkerId(Long workerId);
}