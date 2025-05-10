package com.example.livetask.controller;

import com.example.livetask.model.Task;
import com.example.livetask.model.User;
import com.example.livetask.repository.TaskRepository;
import com.example.livetask.repository.UserRepository;

import jakarta.validation.Valid;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
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
    public String index(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        String username = principal.getName();
        Optional<User> user = userRepository.findByUsername(username);
        List<Task> tasks = taskRepository.findByUser(user);
        model.addAttribute("tasks", tasks);
        return "index";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addTask(@ModelAttribute @Valid Task task, BindingResult result, Principal principal) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));
            return ResponseEntity.badRequest().body(errors);
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElseThrow();
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
    public String deleteTask(@PathVariable Long id) {
        taskRepository.deleteById(id);
        return "redirect:/";
    }

    @PostMapping("/toggle/{id}")
    @ResponseBody
    public Map<String, Object> toggleTask(@PathVariable Long id) {
        Task task = taskRepository.findById(id).orElseThrow();
        task.setCompleted(!task.isCompleted());
        taskRepository.save(task);

        Map<String, Object> result = new HashMap<>();
        result.put("completed", task.isCompleted());
        return result;
    }
}