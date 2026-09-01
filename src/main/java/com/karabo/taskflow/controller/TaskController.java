package com.karabo.taskflow.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.karabo.taskflow.dto.TaskRequest;
import com.karabo.taskflow.dto.TaskResponse;
import com.karabo.taskflow.model.User;
import com.karabo.taskflow.service.TaskService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponse> getAllTasks(Authentication authentication) {
        return taskService.getAllTasks(currentUser(authentication));
    }

    @GetMapping("/{id}")
    public TaskResponse getTaskById(@PathVariable Long id, Authentication authentication) {
        return taskService.getTaskById(id, currentUser(authentication));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskRequest request,
            Authentication authentication) {

        TaskResponse created = taskService.createTask(request, currentUser(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public TaskResponse updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request,
            Authentication authentication) {

        return taskService.updateTask(id, request, currentUser(authentication));
    }

    @PatchMapping("/{id}/start")
    public TaskResponse startTask(@PathVariable Long id, Authentication authentication) {
        return taskService.startTask(id, currentUser(authentication));
    }

    @PatchMapping("/{id}/complete")
    public TaskResponse completeTask(@PathVariable Long id, Authentication authentication) {
        return taskService.completeTask(id, currentUser(authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id, Authentication authentication) {
        taskService.deleteTask(id, currentUser(authentication));
        return ResponseEntity.noContent().build();
    }

    // The JWT filter sets the authenticated User entity itself as the
    // principal, so every task operation can be scoped to it.
    private User currentUser(Authentication authentication) {
        return (User) authentication.getPrincipal();
    }
}
