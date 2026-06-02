package com.ewaste.entity;

import com.ewaste.enums.Role;
import com.ewaste.enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @Column(unique = true)
    @NotBlank
    private String username;

    private String phone;

    @Email
    @Column(unique = true)
    @NotBlank
    private String email;

    @JsonIgnore
    @NotBlank
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private Status status;

    @JsonIgnore
    private String resetOtp;

    @JsonIgnore
    private LocalDateTime otpExpiry;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public User() {}

    public User(Long id, String name, String username, String phone, String email,
                String password, Role role, Status status) {
        this.id = id; this.name = name; this.username = username;
        this.phone = phone; this.email = email; this.password = password;
        this.role = role; this.status = status;
    }

    public Long getId()                      { return id; }
    public void setId(Long id)               { this.id = id; }
    public String getName()                  { return name; }
    public void setName(String name)         { this.name = name; }
    public String getUsername()              { return username; }
    public void setUsername(String u)        { this.username = u; }
    public String getPhone()                 { return phone; }
    public void setPhone(String phone)       { this.phone = phone; }
    public String getEmail()                 { return email; }
    public void setEmail(String email)       { this.email = email; }
    public String getPassword()              { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole()                    { return role; }
    public void setRole(Role role)           { this.role = role; }
    public Status getStatus()               { return status; }
    public void setStatus(Status status)    { this.status = status; }
    public String getResetOtp()             { return resetOtp; }
    public void setResetOtp(String otp)     { this.resetOtp = otp; }
    public LocalDateTime getOtpExpiry()     { return otpExpiry; }
    public void setOtpExpiry(LocalDateTime e){ this.otpExpiry = e; }
    public LocalDateTime getCreatedAt()     { return createdAt; }
    public void setCreatedAt(LocalDateTime c){ this.createdAt = c; }
}
