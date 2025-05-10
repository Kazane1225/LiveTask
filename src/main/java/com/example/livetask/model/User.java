package com.example.livetask.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Entity
@Table(name = "users") // "user" は予約語なので "users" にするのが一般的
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{user.username.required}")
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank(message = "{user.email.required}")
    @Email
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "{user.password.required}")
    @Column(nullable = false)
    private String password;

    // タスク一覧との関連
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks;

    // --- Getter / Setter ---
    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public List<Task> getTasks() { return tasks; }

    public void setTasks(List<Task> tasks) { this.tasks = tasks; }
}

