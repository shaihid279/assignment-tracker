package com.tracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TeacherMessageForm {

    @NotNull(message = "Please select a teacher.")
    private Long teacherId;

    @NotBlank(message = "Subject is required.")
    @Size(max = 150, message = "Subject must not exceed 150 characters.")
    private String subject;

    @NotBlank(message = "Message is required.")
    @Size(max = 2000, message = "Message must not exceed 2000 characters.")
    private String body;

    public Long getTeacherId() { return teacherId; }
    public void setTeacherId(Long teacherId) { this.teacherId = teacherId; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}