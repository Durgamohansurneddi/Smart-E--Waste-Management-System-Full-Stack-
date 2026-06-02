package com.ewaste.controller.request;

import com.ewaste.dto.ApiResponse;
import com.ewaste.dto.request.AssignWorkerRequest;
import com.ewaste.dto.request.CreateRequestDTO;
import com.ewaste.dto.request.CreateRequestResponse;
import com.ewaste.dto.request.RejectRequestDTO;
import com.ewaste.entity.EwasteItem;
import com.ewaste.entity.EwasteRequest;
import com.ewaste.entity.TimeSlot;
import com.ewaste.enums.RequestStatus;
import com.ewaste.service.request.EwasteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ewaste")
public class EwasteController {

    private final EwasteService service;

    public EwasteController(EwasteService service) {
        this.service = service;
    }

    // ───────────── MASTER DATA (shared) ─────────────

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/timeslots")
    public ResponseEntity<ApiResponse<List<TimeSlot>>> getTimeSlots() {
        return ResponseEntity.ok(ApiResponse.ok("Time slots fetched", service.getAllTimeSlots()));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<EwasteItem>>> getItems() {
        return ResponseEntity.ok(ApiResponse.ok("Items fetched", service.getAllItems()));
    }

    // ───────────── USER ENDPOINTS ─────────────

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/user/request")
    public ResponseEntity<ApiResponse<CreateRequestResponse>> createRequest(
            @Valid @RequestBody CreateRequestDTO dto) {
        CreateRequestResponse response = service.createRequest(dto);
        return ResponseEntity.ok(ApiResponse.ok(response.getMessage(), response));
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/requests")
    public ResponseEntity<ApiResponse<List<EwasteRequest>>> myRequests() {
        return ResponseEntity.ok(ApiResponse.ok("Your requests fetched", service.getMyRequests()));
    }

    // ───────────── ADMIN ENDPOINTS ─────────────

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/requests")
    public ResponseEntity<ApiResponse<List<EwasteRequest>>> allRequests() {
        return ResponseEntity.ok(ApiResponse.ok("All requests fetched", service.getAllRequests()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/requests/status/{status}")
    public ResponseEntity<ApiResponse<List<EwasteRequest>>> requestsByStatus(
            @PathVariable String status) {
        RequestStatus rs = RequestStatus.valueOf(status.toUpperCase());
        return ResponseEntity.ok(ApiResponse.ok("Requests fetched", service.getRequestsByStatus(rs)));
    }

    /**
     * Approve a request and assign a worker in one call.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/requests/{id}/approve")
    public ResponseEntity<ApiResponse<Void>> approveRequest(
            @PathVariable Long id,
            @Valid @RequestBody AssignWorkerRequest body) {
        service.approveAndAssignWorker(id, body.getWorkerId());
        return ResponseEntity.ok(ApiResponse.ok("Request approved and worker assigned"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/requests/{id}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectRequest(
            @PathVariable Long id,
            @Valid @RequestBody RejectRequestDTO body) {
        service.rejectRequest(id, body.getReason());
        return ResponseEntity.ok(ApiResponse.ok("Request rejected"));
    }

    // ───────────── WORKER ENDPOINTS ─────────────

    @PreAuthorize("hasRole('WORKER')")
    @GetMapping("/worker/requests")
    public ResponseEntity<ApiResponse<List<EwasteRequest>>> myAssignedRequests() {
        return ResponseEntity.ok(ApiResponse.ok("Assigned requests fetched", service.getMyAssignedRequests()));
    }

    @PreAuthorize("hasRole('WORKER')")
    @PutMapping("/worker/requests/{id}/complete")
    public ResponseEntity<ApiResponse<Void>> markCompleted(@PathVariable Long id) {
        service.markCompleted(id);
        return ResponseEntity.ok(ApiResponse.ok("Pickup marked as completed"));
    }
}
