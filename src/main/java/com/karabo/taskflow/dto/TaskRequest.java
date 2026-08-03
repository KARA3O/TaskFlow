package com.karabo.taskflow.dto;

import java.time.LocalDate;

public class TaskRequest {

    private String title;

    private String description;

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