package com.karabo.taskflow.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Future;
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


    public TaskRequest() {
    }



    public TaskRequest(String title, String description, LocalDate dueDate) {

        this.title = title;
        this.description = description;
        this.dueDate = dueDate;

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

}