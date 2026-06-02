package com.ewaste.controller;

import com.ewaste.dto.*;
import com.ewaste.entity.User;
import com.ewaste.entity.Worker;
import com.ewaste.exception.CustomException;
import com.ewaste.repository.UserRepository;
import com.ewaste.repository.WorkerRepository;
import com.ewaste.security.JwtService;
import com.ewaste.service.AuthService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final WorkerRepository workerRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    private final ConcurrentHashMap<String, String> aadhaarOtpStore = new ConcurrentHashMap<>();

    public AuthController(AuthService authService, WorkerRepository workerRepository,
                          UserRepository userRepository, JwtService jwtService) {
        this.authService = authService;
        this.workerRepository = workerRepository;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(authService.register(request)));
    }

    @PostMapping("/register-worker")
    public ResponseEntity<ApiResponse<String>> registerWorker(@Valid @RequestBody WorkerRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(authService.registerWorker(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);

        if ("AADHAAR_REQUIRED".equals(response.getMessage())) {
            return ResponseEntity.status(HttpStatus.ACCEPTED)
                    .body(new ApiResponse<>(false, "AADHAAR_REQUIRED", response));
        }
        if (response.getToken() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(response.getMessage()));
        }
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    /** Step 1: Worker enters Aadhaar → OTP printed to console */
    @PostMapping("/worker/aadhaar/send-otp")
    public ResponseEntity<ApiResponse<String>> sendAadhaarOtp(@RequestBody Map<String, String> body) {
        String email   = body.get("email");
        String aadhaar = body.get("aadhaarNumber");

        if (email == null || aadhaar == null || !aadhaar.matches("\\d{12}"))
            throw new CustomException("Valid 12-digit Aadhaar number is required");

        Worker worker = workerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Worker profile not found"));

        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);
        aadhaarOtpStore.put(email, otp);
        worker.setAadhaarNumber(aadhaar);
        workerRepository.save(worker);

        // OTP printed to console as requested
        System.out.println("\n>>> AADHAAR OTP for [" + email + "]: " + otp + " <<<\n");
        log.info("Aadhaar OTP generated for worker: {}", email);

        return ResponseEntity.ok(ApiResponse.ok("OTP generated. Check server console."));
    }

    /** Step 2: Worker submits OTP → gets JWT token */
    @PostMapping("/worker/aadhaar/verify-otp")
    public ResponseEntity<ApiResponse<LoginResponse>> verifyAadhaarOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp   = body.get("otp");

        String stored = aadhaarOtpStore.get(email);
        if (stored == null || !stored.equals(otp))
            throw new CustomException("Invalid or expired OTP");

        Worker worker = workerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("Worker not found"));
        worker.setAadhaarVerified(true);
        workerRepository.save(worker);
        aadhaarOtpStore.remove(email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User account not found"));
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        log.info("Worker {} Aadhaar verified — JWT issued", email);
        LoginResponse lr = new LoginResponse(token, "Aadhaar verified. Login granted.", user);
        return ResponseEntity.ok(ApiResponse.ok("Aadhaar verified", lr));
    }
}
