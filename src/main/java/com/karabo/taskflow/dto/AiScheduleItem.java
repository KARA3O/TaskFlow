package com.karabo.taskflow.dto;

public class AiScheduleItem {

    private String time;
    private String taskTitle;
    private Integer durationMinutes;
    private String reason;

    public AiScheduleItem() {
    }

    public AiScheduleItem(
            String time,
            String taskTitle,
            Integer durationMinutes,
            String reason) {
        this.time = time;
        this.taskTitle = taskTitle;
        this.durationMinutes = durationMinutes;
        this.reason = reason;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
