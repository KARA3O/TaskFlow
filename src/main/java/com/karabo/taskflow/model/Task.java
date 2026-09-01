package com.karabo.taskflow.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks", indexes = @Index(name = "idx_tasks_owner", columnList = "user_id"))
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private TaskStatus status;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // Optional field: helps AI calculate schedule duration (in minutes)
    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    protected Task() {
        // Default constructor for JPA
    }

    public Task(String title, String description, LocalDate dueDate, User owner) {
        this(title, description, dueDate, 30, owner);
    }

    public Task(String title, String description, LocalDate dueDate, Integer estimatedMinutes, User owner) {
        this.title = title;
        this.description = description;
        this.status = TaskStatus.PENDING;
        this.dueDate = dueDate;
        this.estimatedMinutes = estimatedMinutes != null ? estimatedMinutes : 30;
        this.createdAt = LocalDateTime.now();
        this.owner = owner;
    }

    public void updateTask(String title, String description, LocalDate dueDate, Integer estimatedMinutes) {
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        if (estimatedMinutes != null) {
            this.estimatedMinutes = estimatedMinutes;
        }
    }

    public void startTask() {
        this.status = TaskStatus.IN_PROGRESS;
    }

    public void completeTask() {
        this.status = TaskStatus.COMPLETED;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public TaskStatus getStatus() { return status; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Integer getEstimatedMinutes() { return estimatedMinutes; }
    public User getOwner() { return owner; }

    public void setStatus(TaskStatus status) { this.status = status; }
    public void setEstimatedMinutes(Integer estimatedMinutes) { this.estimatedMinutes = estimatedMinutes; }
}