package com.tracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ContactForm {

    @NotBlank(message = "Your name is required.")
    @Size(max = 120, message = "Name must not exceed 120 characters.")
    private String name;

    @NotBlank(message = "Email address is required.")
    @Email(message = "Please enter a valid email address.")
    @Size(max = 150, message = "Email must not exceed 150 characters.")
    private String email;

    @NotBlank(message = "Subject is required.")
    @Size(max = 150, message = "Subject must not exceed 150 characters.")
    private String subject;

    @NotBlank(message = "Message is required.")
    @Size(max = 3000, message = "Message must not exceed 3000 characters.")
    private String message;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}