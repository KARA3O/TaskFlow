package com.karabo.taskflow.dto;

public class RecommendationDto {
    private String title;
    private String reason;

    public RecommendationDto() {}

    public RecommendationDto(String title, String reason) {
        this.title = title;
        this.reason = reason;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}