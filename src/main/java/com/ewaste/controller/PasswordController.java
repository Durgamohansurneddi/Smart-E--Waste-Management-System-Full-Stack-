package com.ewaste.controller;

import com.ewaste.dto.ApiResponse;
import com.ewaste.dto.pswd.ForgotPasswordRequest;
import com.ewaste.dto.pswd.ResetPasswordRequest;
import com.ewaste.dto.pswd.VerifyOtpRequest;
import com.ewaste.service.PasswordService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/password")
public class PasswordController {

    private final PasswordService passwordService;

    public PasswordController(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @PostMapping("/forgot")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        String message = passwordService.generateOtp(request.getEmail());
        return ResponseEntity.ok(ApiResponse.ok(message));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<String>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        String message = passwordService.verifyOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(ApiResponse.ok(message));
    }

    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        String message = passwordService.resetPassword(
                request.getEmail(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.ok(message));
    }
}
