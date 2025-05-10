package com.example.livetask.controller;

import com.example.livetask.model.Task;
import com.example.livetask.repository.TaskRepository;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

    @Autowired
    private MessageSource messageSource;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("tasks", taskRepository.findAll());
        model.addAttribute("newTask", new Task());
        return "index";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addTask(@ModelAttribute @Valid Task task, BindingResult result) {
        // バリデーションエラー
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            result.getFieldErrors().forEach(err -> {
                errors.put(err.getField(), err.getDefaultMessage());
            });
            return ResponseEntity.badRequest().body(errors); // HTTP 400 + エラーマップを返す
        }

        // デフォルト値の設定
        task.setCompleted(false);
        if (task.getPriority() == null) {
            task.setPriority(3);
        }

        // 重複チェック
        if (taskRepository.existsByTitle(task.getTitle())) {
            Map<String, String> errors = new HashMap<>();
            String message = messageSource.getMessage("task.title.duplicate", null, Locale.getDefault());
            errors.put("title", message);
            return ResponseEntity.badRequest().body(errors);
        }

        // 正常登録
        Task saved = taskRepository.save(task);
        return ResponseEntity.ok(saved); // HTTP 200 + 登録されたタスクJSON
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