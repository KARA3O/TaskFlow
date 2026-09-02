package com.karabo.taskflow.dto;

import java.util.ArrayList;
import java.util.List;

public class AiPlanResponse {

    private String summary;
    private List<RecommendationDto> recommendations;
    private List<String> insights;
    private Integer totalEstimatedMinutes;
    private Integer overdueCount;
    private Integer dueTodayCount;
    private Integer dueTomorrowCount;

    public AiPlanResponse() {
    }

    public AiPlanResponse(
            String summary,
            List<RecommendationDto> recommendations) {
        this.summary = summary;
        this.recommendations = recommendations;
        this.insights = new ArrayList<>();
    }

    public AiPlanResponse(
            String summary,
            List<RecommendationDto> recommendations,
            List<String> insights,
            Integer totalEstimatedMinutes,
            Integer overdueCount,
            Integer dueTodayCount,
            Integer dueTomorrowCount) {

        this.summary = summary;
        this.recommendations = recommendations;
        this.insights = insights;
        this.totalEstimatedMinutes = totalEstimatedMinutes;
        this.overdueCount = overdueCount;
        this.dueTodayCount = dueTodayCount;
        this.dueTomorrowCount = dueTomorrowCount;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<RecommendationDto> getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(List<RecommendationDto> recommendations) {
        this.recommendations = recommendations;
    }

    public List<String> getInsights() {
        return insights;
    }

    public void setInsights(List<String> insights) {
        this.insights = insights;
    }

    public Integer getTotalEstimatedMinutes() {
        return totalEstimatedMinutes;
    }

    public void setTotalEstimatedMinutes(Integer totalEstimatedMinutes) {
        this.totalEstimatedMinutes = totalEstimatedMinutes;
    }

    public Integer getOverdueCount() {
        return overdueCount;
    }

    public void setOverdueCount(Integer overdueCount) {
        this.overdueCount = overdueCount;
    }

    public Integer getDueTodayCount() {
        return dueTodayCount;
    }

    public void setDueTodayCount(Integer dueTodayCount) {
        this.dueTodayCount = dueTodayCount;
    }

    public Integer getDueTomorrowCount() {
        return dueTomorrowCount;
    }

    public void setDueTomorrowCount(Integer dueTomorrowCount) {
        this.dueTomorrowCount = dueTomorrowCount;
    }
}
