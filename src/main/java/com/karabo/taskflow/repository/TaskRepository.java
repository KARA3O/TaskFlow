package com.karabo.taskflow.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.karabo.taskflow.model.Task;

public interface TaskRepository extends JpaRepository<Task, Long> {


    // JpaRepository already provides:
    // save()
    // findAll()
    // findById()
    // deleteById()


    // Custom methods for TaskRepository


    // Find tasks by status
    // Example: Get all completed tasks
    // List<Task> findByStatus(TaskStatus status);


    // Search tasks by title
    // Example: Search "Java"
    // List<Task> findByTitleContainingIgnoreCase(String title);


    // Find tasks with a specific due date
    // List<Task> findByDueDate(LocalDate dueDate);

}