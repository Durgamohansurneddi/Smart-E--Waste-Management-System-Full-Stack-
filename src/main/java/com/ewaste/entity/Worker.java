package com.ewaste.entity;

import com.ewaste.enums.WorkerStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "workers")
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String email;
    private String phone;
    private String vehicleNumber;
    private String licenceNumber;

    /** Aadhaar number — stored but never serialised to JSON */
    @JsonIgnore
    private String aadhaarNumber;

    private boolean aadhaarVerified = false;

    @Enumerated(EnumType.STRING)
    private WorkerStatus status = WorkerStatus.AVAILABLE;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { this.createdAt = LocalDateTime.now(); }

    public Long getId()                        { return id; }
    public void setId(Long id)                 { this.id = id; }
    public String getName()                    { return name; }
    public void setName(String name)           { this.name = name; }
    public String getEmail()                   { return email; }
    public void setEmail(String email)         { this.email = email; }
    public String getPhone()                   { return phone; }
    public void setPhone(String phone)         { this.phone = phone; }
    public String getVehicleNumber()           { return vehicleNumber; }
    public void setVehicleNumber(String v)     { this.vehicleNumber = v; }
    public String getLicenceNumber()           { return licenceNumber; }
    public void setLicenceNumber(String l)     { this.licenceNumber = l; }
    public String getAadhaarNumber()           { return aadhaarNumber; }
    public void setAadhaarNumber(String a)     { this.aadhaarNumber = a; }
    public boolean isAadhaarVerified()         { return aadhaarVerified; }
    public void setAadhaarVerified(boolean v)  { this.aadhaarVerified = v; }
    public WorkerStatus getStatus()            { return status; }
    public void setStatus(WorkerStatus status) { this.status = status; }
    public LocalDateTime getCreatedAt()        { return createdAt; }
    public void setCreatedAt(LocalDateTime c)  { this.createdAt = c; }
}
