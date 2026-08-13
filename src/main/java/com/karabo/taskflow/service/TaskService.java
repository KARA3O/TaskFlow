package com.karabo.taskflow.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.karabo.taskflow.dto.TaskRequest;
import com.karabo.taskflow.dto.TaskResponse;
import com.karabo.taskflow.exception.TaskNotFoundException;
import com.karabo.taskflow.model.Task;
import com.karabo.taskflow.repository.TaskRepository;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TaskResponse getTaskById(Long id) {
        return mapToResponse(findTask(id));
    }

    public TaskResponse createTask(TaskRequest request) {
        Task task = new Task(
                request.getTitle(),
                request.getDescription(),
                request.getDueDate());

        return mapToResponse(taskRepository.save(task));
    }

    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task existingTask = findTask(id);

        existingTask.updateTask(
                request.getTitle(),
                request.getDescription(),
                request.getDueDate());

        return mapToResponse(taskRepository.save(existingTask));
    }

    public void deleteTask(Long id) {
        Task task = findTask(id);
        taskRepository.delete(task);
    }

    private Task findTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() ->
                        new TaskNotFoundException("Task with id " + id + " not found"));
    }

    private TaskResponse mapToResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDueDate(),
                task.getCreatedAt());
    }
}
