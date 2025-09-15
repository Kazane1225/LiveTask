package com.example.livetask.model;

import java.time.Instant;
import java.time.OffsetDateTime;

import org.hibernate.annotations.CreationTimestamp;
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
    @Column(name = "due_date_from", nullable = true)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private OffsetDateTime dueDateFrom;
    @Column(name = "due_date_to", nullable = true)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private OffsetDateTime dueDateTo;
    @Column(name = "priority", nullable = true)
    private Integer priority;
    @Column(name = "completed", nullable = false)
    private boolean completed;
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    // No-args constructor
    public Task() {}

    // All-args constructor
    public Task(Long id, String title, User user, OffsetDateTime due_date_from, OffsetDateTime due_date_to, Integer priority, boolean completed, Instant createdAt) {
        this.id = id;
        this.title = title;
        this.user = user;
        this.dueDateFrom = due_date_from;
        this.dueDateTo = due_date_to;
        this.priority = priority;
        this.completed = completed;
        this.createdAt = createdAt;
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

    public OffsetDateTime getDueDateFrom() { 
        return dueDateFrom; 
    }
    public void setDueDateFrom(OffsetDateTime due_date_from) { 
        this.dueDateFrom = due_date_from; 
    }

    public OffsetDateTime getDueDateTo() { 
        return dueDateTo; 
    }
    public void setDueDateTo(OffsetDateTime due_date_to) { 
        this.dueDateTo = due_date_to; 
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

    public Instant getCreatedAt() { 
        return createdAt; 
    }
    public void setCreatedAt(Instant createdAt) { 
        this.createdAt = createdAt; 
    }
}