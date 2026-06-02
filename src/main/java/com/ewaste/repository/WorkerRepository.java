package com.ewaste.repository;

import com.ewaste.entity.Worker;
import com.ewaste.enums.WorkerStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkerRepository extends JpaRepository<Worker, Long> {
    List<Worker> findByStatus(WorkerStatus status);
    Optional<Worker> findByEmail(String email);
    boolean existsByEmail(String email);
}
