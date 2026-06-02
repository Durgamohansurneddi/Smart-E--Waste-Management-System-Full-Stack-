package com.ewaste.service;

import com.ewaste.dto.CreateWorkerAccountRequest;
import com.ewaste.entity.User;
import com.ewaste.entity.Worker;
import com.ewaste.enums.Role;
import com.ewaste.enums.Status;
import com.ewaste.enums.WorkerStatus;
import com.ewaste.exception.CustomException;
import com.ewaste.repository.UserRepository;
import com.ewaste.repository.WorkerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;
    private final BCryptPasswordEncoder encoder;

    public AdminService(UserRepository userRepository,
                        WorkerRepository workerRepository,
                        BCryptPasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.workerRepository = workerRepository;
        this.encoder = encoder;
    }

    // ───────────── USER MANAGEMENT ─────────────

    public List<User> getPendingUsers()  { return userRepository.findByStatus(Status.PENDING);  }
    public List<User> getApprovedUsers() { return userRepository.findByStatus(Status.APPROVED); }
    public List<User> getRejectedUsers() { return userRepository.findByStatus(Status.REJECTED); }
    public List<User> getAllUsers()       { return userRepository.findAll(); }

    @Transactional
    public User approveUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException("User not found: " + id));
        if (user.getStatus() != Status.PENDING)
            throw new CustomException("User is not in PENDING state");
        user.setStatus(Status.APPROVED);
        log.info("Admin approved user: {}", user.getEmail());
        return userRepository.save(user);
    }

    @Transactional
    public User rejectUser(Long id, String reason) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new CustomException("User not found: " + id));
        if (user.getStatus() != Status.PENDING)
            throw new CustomException("User is not in PENDING state");
        user.setStatus(Status.REJECTED);
        log.info("Admin rejected user {} — reason: {}", user.getEmail(), reason);
        return userRepository.save(user);
    }

    // ───────────── WORKER MANAGEMENT ─────────────

    public List<Worker> getAllWorkers()       { return workerRepository.findAll(); }
    public List<Worker> getAvailableWorkers(){ return workerRepository.findByStatus(WorkerStatus.AVAILABLE); }

    /**
     * Creates both a Worker profile (for assignment) and a User account
     * (for JWT login) in one transaction.
     */
    @Transactional
    public Worker createWorker(CreateWorkerAccountRequest req) {
        if (userRepository.existsByEmail(req.getEmail()))
            throw new CustomException("Email already registered: " + req.getEmail());
        if (userRepository.existsByUsername(req.getUsername()))
            throw new CustomException("Username already taken: " + req.getUsername());
        if (workerRepository.existsByEmail(req.getEmail()))
            throw new CustomException("Worker with this email already exists");

        // 1. Create login account
        User user = new User();
        user.setName(req.getName());
        user.setUsername(req.getUsername());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setRole(Role.WORKER);
        user.setStatus(Status.APPROVED);   // auto-approved — admin created them
        userRepository.save(user);

        // 2. Create worker profile
        Worker worker = new Worker();
        worker.setName(req.getName());
        worker.setEmail(req.getEmail());
        worker.setPhone(req.getPhone());
        worker.setStatus(WorkerStatus.AVAILABLE);
        Worker saved = workerRepository.save(worker);

        log.info("Created worker account: {} ({})", req.getName(), req.getEmail());
        return saved;
    }

    @Transactional
    public Worker updateWorkerStatus(Long workerId, WorkerStatus status) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new CustomException("Worker not found: " + workerId));
        worker.setStatus(status);
        return workerRepository.save(worker);
    }

    @Transactional
    public void deleteWorker(Long workerId) {
        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new CustomException("Worker not found: " + workerId));
        if (worker.getStatus() == WorkerStatus.ASSIGNED)
            throw new CustomException("Cannot delete a worker with an active assignment");
        // Also remove login account
        userRepository.findByEmail(worker.getEmail()).ifPresent(userRepository::delete);
        workerRepository.deleteById(workerId);
        log.info("Deleted worker: {}", worker.getEmail());
    }
}
