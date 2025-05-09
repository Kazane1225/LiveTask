package com.example.livetask.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.persistence.*;

@Entity
@Table(name = "task", schema = "public")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    @Column(name = "title", nullable = true, length = 50)
    private String title;
    @Column(name = "due_date", nullable = true)
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    @Column(name = "priority", nullable = false)
    private Integer priority;
    @Column(name = "completed", nullable = false)
    private boolean completed;

    // No-args constructor
    public Task() {}

    // All-args constructor
    public Task(Long id, String title, LocalDate due_date, Integer priority, boolean completed) {
        this.id = id;
        this.title = title;
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

    public LocalDate getDueDate() { 
        return dueDate; 
    }
    public void setDueDate(LocalDate due_date) { 
        this.dueDate = due_date; 
    }

    public int getPriority() {
        return this.priority;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isCompleted() { 
        return completed; 
    }
    public void setCompleted(boolean completed) { 
        this.completed = completed; 
    }
}