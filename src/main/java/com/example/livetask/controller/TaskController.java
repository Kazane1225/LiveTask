package com.example.livetask.controller;

import com.example.livetask.model.Task;
import com.example.livetask.model.User;
import com.example.livetask.repository.TaskRepository;
import com.example.livetask.repository.UserRepository;
import com.example.livetask.service.ChatGPTService;
import com.example.livetask.service.TaskService;

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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class TaskController {
    
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private ChatGPTService chatGPTService;

    @Autowired
    private TaskService taskService;

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
        List<Task> tasks = taskService.searchTasksByEmail(email);
        model.addAttribute("tasks", tasks);
        return "index";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addTask(@ModelAttribute @Valid Task task, BindingResult result, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        try {
            Task saved = taskService.createTask(task, user);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            Map<String, String> errors = new HashMap<>();
            String msg = messageSource.getMessage("task.title.duplicate", null, Locale.getDefault());
            errors.put("title", msg);
            return ResponseEntity.badRequest().body(errors);
        }
    }

    @PostMapping("/delete/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteTask(@PathVariable Long id, Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        boolean deleted = taskService.deleteTaskById(id, user);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.status(403).build();
    }

    @PostMapping("/toggle/{id}")
    @ResponseBody
    public Map<String, Object> toggleTask(@PathVariable Long id) {
        return taskService.toggleTask(id);
    }

    @PostMapping("/tasks/sort")
    @ResponseBody
    public List<Task> sortTasks(@RequestBody Map<String, List<String>> body, Principal principal) {
        if(principal == null) return List.of();
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return taskService.sortTasksByTags(user, body.get("tags"));
    }

    @GetMapping("/delete-account")
    public String showDeleteAccountPage(Principal principal, Model model) {
        if (principal == null) return "redirect:/login";
        return "delete-account"; // delete-account.html を表示
    }

    @PostMapping("/delete-account")
    @Transactional
    public String deleteAccount(Principal principal) {
        if (principal == null) return "redirect:/login";
        String email = principal.getName();
        userRepository.findByEmail(email).ifPresent(user -> {
            taskService.deleteAllByUser(user);
            userRepository.delete(user);
        });
        return "redirect:/login";
    }
}