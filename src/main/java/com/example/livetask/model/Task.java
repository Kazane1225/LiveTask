package com.example.livetask.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "tasks", schema = "public")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "title", nullable = false, length = 50)
    @NotBlank(message = "{task.title.required}")
    private String title;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;
    @Column(name = "due_date", nullable = true)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private OffsetDateTime dueDate;
    @Column(name = "priority", nullable = true)
    private Integer priority;
    @Column(name = "completed", nullable = false)
    private boolean completed;

    // No-args constructor
    public Task() {}

    // All-args constructor
    public Task(Long id, String title, User user, OffsetDateTime due_date, Integer priority, boolean completed) {
        this.id = id;
        this.title = title;
        this.user = user;
        this.dueDate = due_date;
        this.priority = priority;
        this.completed = completed;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) { 
        this.id = id; 
    }

    public String getTitle() { 
        return title; 
    }
    public void setTitle(String title) { 
        this.title = title; 
    }

    public User getUser() {
    return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public OffsetDateTime getDueDate() { 
        return dueDate; 
    }
    public void setDueDate(OffsetDateTime due_date) { 
        this.dueDate = due_date; 
    }

    public Integer getPriority() {
        return this.priority;
    }
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public boolean isCompleted() { 
        return completed; 
    }
    public void setCompleted(boolean completed) { 
        this.completed = completed; 
    }
}