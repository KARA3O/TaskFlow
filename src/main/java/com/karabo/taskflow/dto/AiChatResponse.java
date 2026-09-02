package com.karabo.taskflow.dto;

import java.util.ArrayList;
import java.util.List;

public class AiChatResponse {

    private String reply;
    private List<String> suggestions = new ArrayList<>();
    private List<String> breakdown = new ArrayList<>();
    private List<AiScheduleItem> schedule = new ArrayList<>();

    public AiChatResponse() {
    }

    public AiChatResponse(String reply) {
        this.reply = reply;
    }

    public String getReply() {
        return reply;
    }

    public void setReply(String reply) {
        this.reply = reply;
    }

    public List<String> getSuggestions() {
        return suggestions;
    }

    public void setSuggestions(List<String> suggestions) {
        this.suggestions = suggestions;
    }

    public List<String> getBreakdown() {
        return breakdown;
    }

    public void setBreakdown(List<String> breakdown) {
        this.breakdown = breakdown;
    }

    public List<AiScheduleItem> getSchedule() {
        return schedule;
    }

    public void setSchedule(List<AiScheduleItem> schedule) {
        this.schedule = schedule;
    }
}
