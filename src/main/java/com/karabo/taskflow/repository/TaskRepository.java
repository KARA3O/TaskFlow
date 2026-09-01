package com.karabo.taskflow.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.karabo.taskflow.model.Task;
import com.karabo.taskflow.model.TaskStatus;
import com.karabo.taskflow.model.User;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // Scoped to the authenticated user so nobody can see another user's tasks.
    List<Task> findByOwnerOrderByCreatedAtDesc(User owner);

    // Scoped lookup used for get/update/delete so ownership is enforced at the query level.
    Optional<Task> findByIdAndOwner(Long id, User owner);

    // Fetch active tasks for AI processing (excludes completed items)
    List<Task> findByOwnerAndStatusNotOrderByDueDateAsc(User owner, TaskStatus status);

    // Fetch tasks by specific status (e.g., PENDING or IN_PROGRESS)
    List<Task> findByOwnerAndStatusOrderByDueDateAsc(User owner, TaskStatus status);
}