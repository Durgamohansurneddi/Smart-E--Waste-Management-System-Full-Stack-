package com.ewaste.dto.request;

import jakarta.validation.constraints.NotNull;

public class AssignWorkerRequest {

    @NotNull(message = "Worker ID is required")
    private Long workerId;

    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }
}
