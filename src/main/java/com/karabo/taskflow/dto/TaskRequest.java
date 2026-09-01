package com.karabo.taskflow.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TaskRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be in the future")
    private LocalDate dueDate;

    @Min(value = 1, message = "Estimated minutes must be at least 1")
    private Integer estimatedMinutes;

    public TaskRequest() {
    }

    public TaskRequest(String title, String description, LocalDate dueDate) {
        this(title, description, dueDate, null);
    }

    public TaskRequest(String title, String description, LocalDate dueDate, Integer estimatedMinutes) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.estimatedMinutes = estimatedMinutes;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }
}
