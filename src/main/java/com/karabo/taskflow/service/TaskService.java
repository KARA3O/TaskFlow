package com.karabo.taskflow.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.karabo.taskflow.model.Task;
import com.karabo.taskflow.repository.TaskRepository;

@Service
public class TaskService {

    private final TaskRepository taskRepository;


    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }


    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }


    public Task getTaskById(Long id) {
        return taskRepository.findById(id)
                .orElse(null);
    }


    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

public Task updateTask(Long id, Task updatedTask) {

    Task existingTask = taskRepository.findById(id)
            .orElse(null);

    if (existingTask != null) {

        existingTask.updateTask(
                updatedTask.getTitle(),
                updatedTask.getDescription(),
                updatedTask.getDueDate()
        );

       existingTask.changeStatus(updatedTask.getStatus());

        return taskRepository.save(existingTask);
    }

    return null;
}

public void deleteTask(Long id) {

    taskRepository.deleteById(id);

}

}