package com.karabo.taskflow.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks")
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

    protected Task() {
        // Default constructor for JPA
    }


public Task(String title, String description, LocalDate dueDate) {
    this.title = title;
    this.description = description;
    this.status = TaskStatus.PENDING;
    this.dueDate = dueDate;
    this.createdAt = LocalDateTime.now();
}

public void updateTask(String title, String description, LocalDate dueDate) {

    this.title = title;
    this.description = description;
    this.dueDate = dueDate;

}


public void startTask() {

    this.status = TaskStatus.IN_PROGRESS;

}


public void completeTask() {

    this.status = TaskStatus.COMPLETED;

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

  public void setStatus(TaskStatus status) {
    this.status = status;
}

}