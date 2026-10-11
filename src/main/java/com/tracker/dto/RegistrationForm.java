package com.tracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegistrationForm {

    @NotBlank(message = "Full name is required.")
    @Size(max = 120, message = "Full name must not exceed 120 characters.")
    private String fullName;

    @NotBlank(message = "Email address is required.")
    @Email(message = "Please enter a valid email address.")
    @Size(max = 150, message = "Email must not exceed 150 characters.")
    private String email;

    @Size(max = 40, message = "Roll number must not exceed 40 characters.")
    private String rollNumber;

    @Size(max = 40, message = "ID card number must not exceed 40 characters.")
    private String idCardNumber;

    @Size(max = 100, message = "Department must not exceed 100 characters.")
    private String department;

    @NotBlank(message = "Password is required.")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 characters.")
    private String password;

    @NotBlank(message = "Please confirm your password.")
    private String confirmPassword;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRollNumber() { return rollNumber; }
    public void setRollNumber(String rollNumber) { this.rollNumber = rollNumber; }
    public String getIdCardNumber() { return idCardNumber; }
    public void setIdCardNumber(String idCardNumber) { this.idCardNumber = idCardNumber; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
}