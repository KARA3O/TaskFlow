package com.karabo.taskflow.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.karabo.taskflow.dto.TaskRequest;
import com.karabo.taskflow.dto.TaskResponse;
import com.karabo.taskflow.service.TaskService;


@RestController
@RequestMapping("/api/tasks")
public class TaskController {


    private final TaskService taskService;


    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }


    @GetMapping
    public List<TaskResponse> getAllTasks() {

        return taskService.getAllTasks();

    }


    @GetMapping("/{id}")
    public TaskResponse getTaskById(@PathVariable Long id) {

        return taskService.getTaskById(id);

    }


    @PostMapping
    public TaskResponse createTask(@RequestBody TaskRequest request) {

        return taskService.createTask(request);

    }


    @PutMapping("/{id}")
    public TaskResponse updateTask(
            @PathVariable Long id,
            @RequestBody TaskRequest request) {

        return taskService.updateTask(id, request);

    }


    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Long id) {

        taskService.deleteTask(id);

    }

}