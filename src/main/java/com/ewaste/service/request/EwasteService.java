package com.ewaste.service.request;

import com.ewaste.dto.request.CreateRequestDTO;
import com.ewaste.dto.request.CreateRequestResponse;
import com.ewaste.dto.request.ItemDTO;
import com.ewaste.entity.*;
import com.ewaste.enums.RequestStatus;
import com.ewaste.enums.WorkerStatus;
import com.ewaste.exception.CustomException;
import com.ewaste.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class EwasteService {

    private static final Logger log = LoggerFactory.getLogger(EwasteService.class);

    private final EwasteRequestRepository requestRepo;
    private final EwasteRequestItemRepository itemRepo;
    private final TimeSlotRepository timeSlotRepository;
    private final EwasteItemRepository itemRepository;
    private final UserRepository userRepository;
    private final WorkerRepository workerRepository;

    public EwasteService(EwasteRequestRepository requestRepo,
                         EwasteRequestItemRepository itemRepo,
                         TimeSlotRepository timeSlotRepository,
                         EwasteItemRepository itemRepository,
                         UserRepository userRepository,
                         WorkerRepository workerRepository) {
        this.requestRepo = requestRepo;
        this.itemRepo = itemRepo;
        this.timeSlotRepository = timeSlotRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.workerRepository = workerRepository;
    }

    // ── Resolves item IDs → names in one DB call ────────────
    private void enrichItems(List<EwasteRequest> requests) {
        // Collect all item IDs needed
        Set<Long> ids = new HashSet<>();
        for (EwasteRequest r : requests) {
            List<EwasteRequestItem> items = itemRepo.findByRequestId(r.getId());
            r.setItems(items);
            items.forEach(i -> ids.add(i.getItemId()));
        }
        // Build lookup map
        Map<Long, String> nameMap = new HashMap<>();
        if (!ids.isEmpty()) {
            itemRepository.findAllById(ids).forEach(e -> nameMap.put(e.getId(), e.getName()));
        }
        // Enrich
        requests.forEach(r -> r.getItems().forEach(i -> i.setItemName(
                nameMap.getOrDefault(i.getItemId(), "Item #" + i.getItemId()))));
    }

    // ───── USER ──────────────────────────────────────────────

    @Transactional
    public CreateRequestResponse createRequest(CreateRequestDTO dto) {
        String email = currentEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found"));

        TimeSlot slot = timeSlotRepository.findById(dto.getTimeSlotId())
                .orElseThrow(() -> new CustomException("Invalid time slot"));

        for (ItemDTO item : dto.getItems()) {
            itemRepository.findById(item.getItemId())
                    .orElseThrow(() -> new CustomException("Invalid item ID: " + item.getItemId()));
        }

        EwasteRequest request = new EwasteRequest();
        request.setUserId(user.getId());
        request.setAddress(dto.getAddress());
        request.setPreferredDate(dto.getPreferredDate());
        request.setTimeSlotId(slot.getId());
        request.setStatus(RequestStatus.PENDING);
        request = requestRepo.save(request);

        List<EwasteRequestItem> items = new ArrayList<>();
        for (ItemDTO item : dto.getItems()) {
            EwasteRequestItem ri = new EwasteRequestItem();
            ri.setRequestId(request.getId());
            ri.setItemId(item.getItemId());
            ri.setQuantity(item.getQuantity());
            items.add(ri);
        }
        itemRepo.saveAll(items);
        log.info("E-waste request #{} created by {}", request.getId(), email);

        CreateRequestResponse resp = new CreateRequestResponse();
        resp.setRequestId(request.getId());
        resp.setMessage("Pickup request submitted. Awaiting admin approval.");
        return resp;
    }

    public List<EwasteRequest> getMyRequests() {
        String email = currentEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("User not found"));
        List<EwasteRequest> requests = requestRepo.findByUserId(user.getId());
        enrichItems(requests);
        return requests;
    }

    // ───── ADMIN ─────────────────────────────────────────────

    public List<EwasteRequest> getAllRequests() {
        List<EwasteRequest> requests = requestRepo.findAll();
        enrichItems(requests);
        return requests;
    }

    public List<EwasteRequest> getRequestsByStatus(RequestStatus status) {
        List<EwasteRequest> requests = requestRepo.findByStatus(status);
        enrichItems(requests);
        return requests;
    }

    @Transactional
    public void approveAndAssignWorker(Long requestId, Long workerId) {
        EwasteRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new CustomException("Request not found: " + requestId));
        if (req.getStatus() != RequestStatus.PENDING)
            throw new CustomException("Only PENDING requests can be approved");

        Worker worker = workerRepository.findById(workerId)
                .orElseThrow(() -> new CustomException("Worker not found: " + workerId));
        if (worker.getStatus() != WorkerStatus.AVAILABLE)
            throw new CustomException("Selected worker is not available");

        req.setStatus(RequestStatus.APPROVED);
        req.setAssignedWorker(worker);
        req.setApprovedAt(LocalDateTime.now());
        worker.setStatus(WorkerStatus.ASSIGNED);
        workerRepository.save(worker);
        requestRepo.save(req);
        log.info("Request #{} approved → worker '{}' assigned", requestId, worker.getName());
    }

    @Transactional
    public void rejectRequest(Long requestId, String reason) {
        if (reason == null || reason.isBlank())
            throw new CustomException("Rejection reason is required");
        EwasteRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new CustomException("Request not found: " + requestId));
        if (req.getStatus() != RequestStatus.PENDING)
            throw new CustomException("Only PENDING requests can be rejected");
        req.setStatus(RequestStatus.REJECTED);
        req.setAdminRemarks(reason);
        requestRepo.save(req);
        log.info("Request #{} rejected — reason: {}", requestId, reason);
    }

    // ───── WORKER ────────────────────────────────────────────

    public List<EwasteRequest> getMyAssignedRequests() {
        String email = currentEmail();
        Worker worker = workerRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("No worker profile for: " + email));
        List<EwasteRequest> requests = requestRepo.findByAssignedWorkerId(worker.getId());
        enrichItems(requests);
        return requests;
    }

    @Transactional
    public void markCompleted(Long requestId) {
        String email = currentEmail();
        EwasteRequest req = requestRepo.findById(requestId)
                .orElseThrow(() -> new CustomException("Request not found"));
        if (req.getAssignedWorker() == null || !req.getAssignedWorker().getEmail().equals(email))
            throw new CustomException("You are not assigned to this request");
        if (req.getStatus() != RequestStatus.APPROVED)
            throw new CustomException("Only APPROVED requests can be marked completed");
        req.setStatus(RequestStatus.COMPLETED);
        Worker worker = req.getAssignedWorker();
        worker.setStatus(WorkerStatus.AVAILABLE);
        workerRepository.save(worker);
        requestRepo.save(req);
        log.info("Request #{} completed by worker {}", requestId, email);
    }

    // ───── MASTER DATA ────────────────────────────────────────
    public List<TimeSlot> getAllTimeSlots() { return timeSlotRepository.findAll(); }
    public List<EwasteItem> getAllItems()   { return itemRepository.findAll(); }

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
