package com.karabo.taskflow.dto;

public class RecommendationDto {

    private String title;
    private String reason;
    private String priority;
    private String action;
    private Integer estimatedMinutes;

    public RecommendationDto() {
    }

    public RecommendationDto(String title, String reason) {
        this.title = title;
        this.reason = reason;
    }

    public RecommendationDto(
            String title,
            String reason,
            String priority,
            String action,
            Integer estimatedMinutes) {
        this.title = title;
        this.reason = reason;
        this.priority = priority;
        this.action = action;
        this.estimatedMinutes = estimatedMinutes;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Integer getEstimatedMinutes() {
        return estimatedMinutes;
    }

    public void setEstimatedMinutes(Integer estimatedMinutes) {
        this.estimatedMinutes = estimatedMinutes;
    }
}
