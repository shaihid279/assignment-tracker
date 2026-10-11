package com.tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class AssignmentForm {

    @NotBlank(message = "Assignment name is required.")
    @Size(max = 150, message = "Assignment name must not exceed 150 characters.")
    private String title;

    @NotBlank(message = "Subject is required.")
    @Size(max = 100, message = "Subject must not exceed 100 characters.")
    private String subject;

    @Size(max = 120, message = "Teacher name must not exceed 120 characters.")
    private String teacherName;

    @Size(max = 2000, message = "Description must not exceed 2000 characters.")
    private String description;

    @NotNull(message = "Due date is required.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dueDate;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}