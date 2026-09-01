package com.karabo.taskflow.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.karabo.taskflow.dto.TaskRequest;
import com.karabo.taskflow.dto.TaskResponse;
import com.karabo.taskflow.exception.TaskNotFoundException;
import com.karabo.taskflow.model.Task;
import com.karabo.taskflow.model.User;
import com.karabo.taskflow.repository.TaskRepository;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public List<TaskResponse> getAllTasks(User owner) {
        return taskRepository.findByOwnerOrderByCreatedAtDesc(owner)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TaskResponse getTaskById(Long id, User owner) {
        return mapToResponse(findTask(id, owner));
    }

    public TaskResponse createTask(TaskRequest request, User owner) {
        Task task = new Task(
                request.getTitle(),
                request.getDescription(),
                request.getDueDate(),
                request.getEstimatedMinutes(),
                owner);

        return mapToResponse(taskRepository.save(task));
    }

    public TaskResponse updateTask(Long id, TaskRequest request, User owner) {
        Task existingTask = findTask(id, owner);

        existingTask.updateTask(
                request.getTitle(),
                request.getDescription(),
                request.getDueDate(),
                request.getEstimatedMinutes());

        return mapToResponse(taskRepository.save(existingTask));
    }

    public TaskResponse startTask(Long id, User owner) {
        Task task = findTask(id, owner);
        task.startTask();
        return mapToResponse(taskRepository.save(task));
    }

    public TaskResponse completeTask(Long id, User owner) {
        Task task = findTask(id, owner);
        task.completeTask();
        return mapToResponse(taskRepository.save(task));
    }

    public void deleteTask(Long id, User owner) {
        taskRepository.delete(findTask(id, owner));
    }

    private Task findTask(Long id, User owner) {
        return taskRepository.findByIdAndOwner(id, owner)
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
                task.getCreatedAt(),
                task.getEstimatedMinutes());
    }
}
