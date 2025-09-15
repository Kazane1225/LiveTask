package com.example.livetask.service;

import java.util.ArrayList;
import java.util.Comparator;
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

     // TaskService.java（概略）
public List<Task> sortTasks(User user, List<String> tags, boolean desc) {
    List<Task> tasks = searchTasksByEmail(user.getEmail()); // 既存の取得

    Comparator<Task> cmp = null;
    for (String tag : (tags == null ? List.<String>of() : tags)) {
        Comparator<Task> part = switch (tag) {
            case "priority" -> Comparator.comparing(
                Task::getPriority,
                Comparator.nullsLast(Comparator.naturalOrder())
            );
            case "dueDate" -> Comparator.comparing(
                t -> firstNonNull(t.getDueDateFrom(), t.getDueDateTo()),
                Comparator.nullsLast(Comparator.naturalOrder())
            );
            case "created" -> Comparator.comparing(
                Task::getCreatedAt, // ← フィールドが無いなら追加（@CreationTimestamp推奨）
                Comparator.nullsLast(Comparator.naturalOrder())
            );
            default -> null;
        };
        if (part != null) {
            cmp = (cmp == null) ? part : cmp.thenComparing(part);
        }
    }
    if (cmp == null) return tasks; // タグ指定なしはそのまま

    if (desc) cmp = cmp.reversed();
    return tasks.stream().sorted(cmp).toList();
}

private static <T> T firstNonNull(T a, T b) { return a != null ? a : b; }

    public void deleteAllByUser(User user) {
        taskRepository.deleteAllByUser(user);
    }
}