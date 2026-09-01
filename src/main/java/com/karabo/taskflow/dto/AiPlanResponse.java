package com.karabo.taskflow.dto;

import java.util.List;

public class AiPlanResponse {

    private String summary;
    private List<RecommendationDto> recommendations;

    public AiPlanResponse() {
    }

    public AiPlanResponse(String summary, List<RecommendationDto> recommendations) {
        this.summary = summary;
        this.recommendations = recommendations;
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
}
