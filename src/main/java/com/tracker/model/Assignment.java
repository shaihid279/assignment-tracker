package com.tracker.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "assignments")
public class Assignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(nullable = false)
    private LocalDate dueDate;

    /** The teacher who published the assignment. Null for personal assignments. */
    @ManyToOne
    @JoinColumn(name = "teacher_id")
    private User teacher;

    /** Free-text teacher name used by personal assignments. */
    @Column(length = 120)
    private String teacherName;

    /** The student who owns a personal assignment. Null for teacher assignments. */
    @ManyToOne
    @JoinColumn(name = "owner_id")
    private User owner;

    @Column(nullable = false)
    private boolean personal;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public String getTeacherDisplayName() {
        if (teacher != null) {
            return teacher.getFullName();
        }
        if (teacherName != null && !teacherName.isBlank()) {
            return teacherName;
        }
        return "Not specified";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
    public User getTeacher() { return teacher; }
    public void setTeacher(User teacher) { this.teacher = teacher; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }
    public boolean isPersonal() { return personal; }
    public void setPersonal(boolean personal) { this.personal = personal; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}