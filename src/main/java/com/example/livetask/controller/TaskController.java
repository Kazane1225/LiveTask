package com.example.livetask.controller;

import com.example.livetask.model.Task;
import com.example.livetask.model.User;
import com.example.livetask.repository.TaskRepository;
import com.example.livetask.repository.UserRepository;

import jakarta.validation.Valid;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class TaskController {
    
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Autowired
    private MessageSource messageSource;

    public TaskController(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/tasks";
    }

    @GetMapping("/tasks")
    public String taskList(Model model, Principal principal) {
        if (principal == null) return "redirect:/login";

        String email = principal.getName();
        Optional<User> user = userRepository.findByEmail(email);
        List<Task> tasks = taskRepository.findByUser(user);
        model.addAttribute("tasks", tasks);
        return "index";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addTask(@ModelAttribute @Valid Task task, BindingResult result, Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).build(); // or redirect or error JSON
        }
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        task.setUser(user);  // ★ ユーザーとタスクを紐づけ

        task.setCompleted(false);
        if (task.getPriority() == null) {
            task.setPriority(3);
        }

        // 重複チェック（同ユーザー内で）
        if (taskRepository.existsByTitleAndUser(task.getTitle(), user)) {
            Map<String, String> errors = new HashMap<>();
            String message = messageSource.getMessage("task.title.duplicate", null, Locale.getDefault());
            errors.put("title", message);
            return ResponseEntity.badRequest().body(errors);
        }

        Task saved = taskRepository.save(task);
        return ResponseEntity.ok(saved);
    }

    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteTask(@PathVariable Long id, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();

        Optional<Task> taskOpt = taskRepository.findById(id);
        if (taskOpt.isEmpty()) return ResponseEntity.notFound().build();

        Task task = taskOpt.get();
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        // 所有者確認
        if (!task.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }

        taskRepository.delete(task);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/toggle/{id}")
    @ResponseBody
    public Map<String, Object> toggleTask(@PathVariable Long id) {
        Task task = taskRepository.findById(id).orElseThrow();
        task.setCompleted(!task.isCompleted());
        taskRepository.save(task);

        Map<String, Object> result = new HashMap<>();
        result.put("id", task.getId());
        result.put("completed", task.isCompleted());
        result.put("title", task.getTitle());
        result.put("priority", task.getPriority());
        return result;
    }

    @PostMapping("/tasks/sort")
    @ResponseBody
    public List<Task> sortTasks(@RequestBody Map<String, List<String>> body, Principal principal) {
        if(principal == null) return List.of();
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        List<String> tags = body.get("tags");
        Sort sort = Sort.unsorted();
        if(tags != null && !tags.isEmpty()) {
            List<Sort.Order> orders = new ArrayList<>();
            if(tags.contains("priority")) orders.add(Sort.Order.asc("priority"));
            if(tags.contains("dueDate")) orders.add(Sort.Order.asc("dueDate"));
            sort = Sort.by(orders);
        }
        return taskRepository.findByUser(user, sort);
    }
}