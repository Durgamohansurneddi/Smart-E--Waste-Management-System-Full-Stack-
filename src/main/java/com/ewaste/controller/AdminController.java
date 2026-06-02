package com.ewaste.controller;

import com.ewaste.dto.ApiResponse;
import com.ewaste.dto.CreateWorkerAccountRequest;
import com.ewaste.entity.User;
import com.ewaste.entity.Worker;
import com.ewaste.enums.WorkerStatus;
import com.ewaste.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ── User Management ──────────────────────────

    @GetMapping("/users/pending")
    public ResponseEntity<ApiResponse<List<User>>> getPendingUsers() {
        return ResponseEntity.ok(ApiResponse.ok("Pending users", adminService.getPendingUsers()));
    }

    @GetMapping("/users/approved")
    public ResponseEntity<ApiResponse<List<User>>> getApprovedUsers() {
        return ResponseEntity.ok(ApiResponse.ok("Approved users", adminService.getApprovedUsers()));
    }

    @GetMapping("/users/rejected")
    public ResponseEntity<ApiResponse<List<User>>> getRejectedUsers() {
        return ResponseEntity.ok(ApiResponse.ok("Rejected users", adminService.getRejectedUsers()));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.ok("All users", adminService.getAllUsers()));
    }

    @PutMapping("/users/{id}/approve")
    public ResponseEntity<ApiResponse<User>> approveUser(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok("User approved", adminService.approveUser(id)));
    }

    @PutMapping("/users/{id}/reject")
    public ResponseEntity<ApiResponse<User>> rejectUser(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String reason = body.getOrDefault("reason", "No reason provided");
        return ResponseEntity.ok(ApiResponse.ok("User rejected", adminService.rejectUser(id, reason)));
    }

    // ── Worker Management ─────────────────────────

    @GetMapping("/workers")
    public ResponseEntity<ApiResponse<List<Worker>>> getAllWorkers() {
        return ResponseEntity.ok(ApiResponse.ok("All workers", adminService.getAllWorkers()));
    }

    @GetMapping("/workers/available")
    public ResponseEntity<ApiResponse<List<Worker>>> getAvailableWorkers() {
        return ResponseEntity.ok(ApiResponse.ok("Available workers", adminService.getAvailableWorkers()));
    }

    /**
     * Creates worker profile + login account in one call.
     * Worker can then log in at /api/auth/login with the supplied credentials.
     */
    @PostMapping("/workers")
    public ResponseEntity<ApiResponse<Worker>> createWorker(
            @Valid @RequestBody CreateWorkerAccountRequest req) {
        Worker worker = adminService.createWorker(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Worker created with login access", worker));
    }

    @PutMapping("/workers/{id}/status")
    public ResponseEntity<ApiResponse<Worker>> updateWorkerStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        WorkerStatus status = WorkerStatus.valueOf(body.get("status").toUpperCase());
        return ResponseEntity.ok(ApiResponse.ok("Status updated", adminService.updateWorkerStatus(id, status)));
    }

    @DeleteMapping("/workers/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteWorker(@PathVariable Long id) {
        adminService.deleteWorker(id);
        return ResponseEntity.ok(ApiResponse.ok("Worker removed successfully"));
    }
}
