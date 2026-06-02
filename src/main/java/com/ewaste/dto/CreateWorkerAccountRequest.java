package com.ewaste.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Admin creates a worker account so the worker can log in with JWT.
 * The Worker entity stores display info; the User entity stores login credentials.
 */
public class CreateWorkerAccountRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30)
    private String username;

    @Email(message = "Valid email required")
    @NotBlank
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    public String getName()     { return name; }
    public void setName(String name)         { this.name = name; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail()    { return email; }
    public void setEmail(String email)       { this.email = email; }
    public String getPhone()    { return phone; }
    public void setPhone(String phone)       { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
