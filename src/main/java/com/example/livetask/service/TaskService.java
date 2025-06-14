package com.example.livetask.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.example.livetask.model.Task;
import com.example.livetask.model.User;
import com.example.livetask.repository.TaskRepository;
import com.example.livetask.repository.UserRepository;

@Service
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private ChatGPTService chatGPTService;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository, ChatGPTService chatGPTService) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.chatGPTService = chatGPTService;
    }

    public List<Task> searchTasksByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        List<Task> tasks = taskRepository.findByUser(user);
        return tasks;
    }

    public Task createTask(Task task, User user) {
        task.setUser(user);
        task.setCompleted(false);

        // priority初期化
        if (task.getPriority() == null) {
            task.setPriority(3);
        }

        // task重複チェック
        if (taskRepository.existsByTitleAndUser(task.getTitle(), user)) {
            throw new IllegalArgumentException("Duplicate task title.");
        }

        return taskRepository.save(task);
    }

    public boolean deleteTaskById(Long id, User user) {
        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isPresent()) {
            Task task = taskOpt.get();
            if (task.getUser().getId().equals(user.getId())) {
                taskRepository.delete(task);
                return true;
            }
        }
        return false;
    }

    public Map<String, Object> toggleTask(Long id) {
        Task task = taskRepository.findById(id).orElseThrow();
        boolean wasCompleted = task.isCompleted();
        task.setCompleted(!wasCompleted);
        taskRepository.save(task);

        Map<String, Object> result = new HashMap<>();
        result.put("id", task.getId());
        result.put("completed", task.isCompleted());
        result.put("title", task.getTitle());
        result.put("priority", task.getPriority());

        if (!wasCompleted && task.isCompleted()) {
            String praise = chatGPTService.generatePraise(task.getTitle());
            result.put("message", praise);
        }
        return result;
    }

    public List<Task> sortTasksByTags(User user, List<String> tags) {
        Sort sort = Sort.unsorted();
        if (tags != null && !tags.isEmpty()) {
            List<Sort.Order> orders = new ArrayList<>();
            if (tags.contains("priority")) orders.add(Sort.Order.asc("priority"));
            if (tags.contains("dueDate")) orders.add(Sort.Order.asc("dueDate"));
            sort = Sort.by(orders);
        }
        return taskRepository.findByUser(user, sort);
    }

    public void deleteAllByUser(User user) {
        taskRepository.deleteAllByUser(user);
    }
}