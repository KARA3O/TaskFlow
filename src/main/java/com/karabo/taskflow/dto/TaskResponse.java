package com.karabo.taskflow.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.karabo.taskflow.model.TaskStatus;

public class TaskResponse {

    private Long id;

    private String title;

    private String description;

    private TaskStatus status;

    private LocalDate dueDate;

    private LocalDateTime createdAt;


    public TaskResponse(
            Long id,
            String title,
            String description,
            TaskStatus status,
            LocalDate dueDate,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
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

}