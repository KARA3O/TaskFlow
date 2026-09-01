package com.karabo.taskflow.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.karabo.taskflow.model.TaskStatus;

public class TaskResponse {

    private final Long id;
    private final String title;
    private final String description;
    private final TaskStatus status;
    private final LocalDate dueDate;
    private final LocalDateTime createdAt;
    private final Integer estimatedMinutes;

    public TaskResponse(
            Long id,
            String title,
            String description,
            TaskStatus status,
            LocalDate dueDate,
            LocalDateTime createdAt,
            Integer estimatedMinutes
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
        this.estimatedMinutes = estimatedMinutes;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }
}
