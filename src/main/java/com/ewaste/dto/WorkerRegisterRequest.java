package com.ewaste.dto;

import jakarta.validation.constraints.*;

public class WorkerRegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 30)
    private String username;

    @Email(message = "Valid email required")
    @NotBlank
    private String email;

    @NotBlank
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 6)
    private String password;

    @NotBlank(message = "Vehicle number is required")
    private String vehicleNumber;

    @NotBlank(message = "Licence number is required")
    private String licenceNumber;

    // Getters and setters
    public String getName()           { return name; }
    public void setName(String n)     { this.name = n; }
    public String getUsername()       { return username; }
    public void setUsername(String u) { this.username = u; }
    public String getEmail()          { return email; }
    public void setEmail(String e)    { this.email = e; }
    public String getPhone()          { return phone; }
    public void setPhone(String p)    { this.phone = p; }
    public String getPassword()       { return password; }
    public void setPassword(String p) { this.password = p; }
    public String getVehicleNumber()         { return vehicleNumber; }
    public void setVehicleNumber(String v)   { this.vehicleNumber = v; }
    public String getLicenceNumber()         { return licenceNumber; }
    public void setLicenceNumber(String l)   { this.licenceNumber = l; }
}
