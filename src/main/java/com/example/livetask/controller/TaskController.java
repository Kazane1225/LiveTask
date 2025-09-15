package com.example.livetask.controller;

import com.example.livetask.dto.SortRequest;
import com.example.livetask.model.Task;
import com.example.livetask.model.User;
import com.example.livetask.repository.UserRepository;
import com.example.livetask.service.TaskService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class TaskController {
    
    private final UserRepository userRepository;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private TaskService taskService;

    public TaskController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/tasks";
    }

    @GetMapping("/tasks")
    public String taskList(Model model, Principal principal) throws JsonProcessingException {
        if (principal == null) return "redirect:/login";
        String email = principal.getName();
        List<Task> tasks = taskService.searchTasksByEmail(email);

        ObjectMapper mapper = new ObjectMapper();
        List<String> jsonTasks = tasks.stream()
                                    .map(t -> {
                                        try { return mapper.writeValueAsString(t); }
                                        catch (JsonProcessingException e) { return "{}"; }
                                    })
                                    .toList();
        model.addAttribute("jsonTasks", jsonTasks);
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
    public List<Task> sortTasks(@RequestBody SortRequest req, Principal principal) {
        if (principal == null) return List.of();
        String email = principal.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return taskService.sortTasks(user, req.getTags(), Boolean.TRUE.equals(req.getDesc()));
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