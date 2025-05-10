package com.example.livetask.repository;

import com.example.livetask.model.Task;
import com.example.livetask.model.User;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    boolean existsByTitle(String title);

    List<Task> findByUser(Optional<User> user);

    boolean existsByTitleAndUser(String title, User user);
}