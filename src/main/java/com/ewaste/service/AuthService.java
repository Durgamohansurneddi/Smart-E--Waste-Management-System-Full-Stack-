package com.ewaste.service;

import com.ewaste.dto.LoginRequest;
import com.ewaste.dto.LoginResponse;
import com.ewaste.dto.RegisterRequest;
import com.ewaste.dto.WorkerRegisterRequest;
import com.ewaste.entity.User;
import com.ewaste.entity.Worker;
import com.ewaste.enums.Role;
import com.ewaste.enums.Status;
import com.ewaste.enums.WorkerStatus;
import com.ewaste.exception.CustomException;
import com.ewaste.repository.UserRepository;
import com.ewaste.repository.WorkerRepository;
import com.ewaste.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;
    private final BCryptPasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       WorkerRepository workerRepository,
                       BCryptPasswordEncoder encoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.workerRepository = workerRepository;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    /** Standard user registration */
    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new CustomException("Email already registered: " + request.getEmail());
        if (userRepository.existsByUsername(request.getUsername()))
            throw new CustomException("Username already taken: " + request.getUsername());

        User user = new User();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        user.setStatus(Status.PENDING);
        userRepository.save(user);
        log.info("User registered: {} — pending admin approval", request.getEmail());
        return "Registration successful. Your account is pending admin approval.";
    }

    /** Worker self-registration with vehicle + licence */
    @Transactional
    public String registerWorker(WorkerRegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail()))
            throw new CustomException("Email already registered: " + req.getEmail());
        if (userRepository.existsByUsername(req.getUsername()))
            throw new CustomException("Username already taken: " + req.getUsername());
        if (workerRepository.existsByEmail(req.getEmail()))
            throw new CustomException("Worker profile with this email already exists");

        // Create login account — PENDING until admin approves
        User user = new User();
        user.setName(req.getName());
        user.setUsername(req.getUsername());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setRole(Role.WORKER);
        user.setStatus(Status.PENDING);
        userRepository.save(user);

        // Create worker profile
        Worker worker = new Worker();
        worker.setName(req.getName());
        worker.setEmail(req.getEmail());
        worker.setPhone(req.getPhone());
        worker.setVehicleNumber(req.getVehicleNumber());
        worker.setLicenceNumber(req.getLicenceNumber());
        worker.setStatus(WorkerStatus.AVAILABLE);
        worker.setAadhaarVerified(false);
        workerRepository.save(worker);

        log.info("Worker registered: {} — pending admin approval", req.getEmail());
        return "Worker registration submitted. Awaiting admin approval.";
    }

    /** Login for all roles */
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);

        if (user == null)
            return new LoginResponse(null, "No account found with this email address", null);
        if (!encoder.matches(request.getPassword(), user.getPassword()))
            return new LoginResponse(null, "Incorrect password", null);
        if (user.getStatus() == Status.PENDING)
            return new LoginResponse(null, "Your account is pending admin approval. Please wait.", null);
        if (user.getStatus() == Status.REJECTED)
            return new LoginResponse(null, "Your registration was rejected. Please contact support.", null);

        // For workers: require Aadhaar verification before allowing login
        if (user.getRole() == Role.WORKER) {
            Worker worker = workerRepository.findByEmail(user.getEmail()).orElse(null);
            if (worker != null && !worker.isAadhaarVerified()) {
                return new LoginResponse(null, "AADHAAR_REQUIRED", user);
            }
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        log.info("Login: {} ({})", user.getEmail(), user.getRole());
        return new LoginResponse(token, "Login successful", user);
    }
}
