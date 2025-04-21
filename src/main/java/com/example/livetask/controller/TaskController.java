package com.example.livetask.controller;

import com.example.livetask.model.Task;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Controller
public class TaskController {

    private final List<Task> tasks = new ArrayList<>();
    private final AtomicLong counter = new AtomicLong(1);

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("tasks", tasks);
        model.addAttribute("newTask", new Task());
        return "index";
    }

    @PostMapping("/add")
    public String addTask(@RequestParam String title, 
                         @RequestParam LocalDate dueDate) {
        Task task = new Task();
        task.setId(counter.getAndIncrement());
        task.setTitle(title);
        task.setDueDate(dueDate);
        tasks.add(task);
        return "redirect:/";
    }

    @PostMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id) {
        tasks.removeIf(task -> task.getId().equals(id));
        return "redirect:/";
    }

    @PostMapping("/toggle/{id}")
    public String toggleTask(@PathVariable Long id) {
        tasks.stream()
            .filter(task -> task.getId().equals(id))
            .findFirst()
            .ifPresent(task -> task.setCompleted(!task.isCompleted()));
        return "redirect:/";
    }
}