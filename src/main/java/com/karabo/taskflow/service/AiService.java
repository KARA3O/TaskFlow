package com.karabo.taskflow.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karabo.taskflow.dto.AiPlanResponse;
import com.karabo.taskflow.model.Task;
import com.karabo.taskflow.model.TaskStatus;
import com.karabo.taskflow.model.User;
import com.karabo.taskflow.repository.TaskRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class AiService {

    private final TaskRepository taskRepository;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public AiService(
            TaskRepository taskRepository,
            ObjectMapper objectMapper,
            @Value("${gemini.api.key:}") String apiKey) {
        this.taskRepository = taskRepository;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.restClient = RestClient.builder().build();
    }

    public AiPlanResponse generatePlanForUser(User user) {
        List<Task> activeTasks = taskRepository
                .findByOwnerAndStatusNotOrderByDueDateAsc(user, TaskStatus.COMPLETED);

        if (activeTasks.isEmpty()) {
            return new AiPlanResponse(
                    "You have no active tasks left. Enjoy your clear schedule.",
                    new ArrayList<>());
        }

        if (apiKey == null || apiKey.isBlank()) {
            return fallbackResponse();
        }

        StringBuilder taskListText = new StringBuilder();
        for (Task task : activeTasks) {
            taskListText.append(String.format(
                    "- Title: %s | Status: %s | Due: %s | Description: %s%n",
                    task.getTitle(), task.getStatus(), task.getDueDate(), task.getDescription()));
        }

        String prompt = "You are a productivity expert assistant for the TaskFlow app.\n"
                + "Analyze these active tasks and prioritize them:\n\n"
                + taskListText
                + "\nRespond only with a valid JSON object matching this structure, without markdown:\n"
                + "{\n"
                + "  \"summary\": \"Short overview strategy\",\n"
                + "  \"recommendations\": [\n"
                + "    {\"title\": \"Task Title\", \"reason\": \"Why to focus on this now\"}\n"
                + "  ]\n"
                + "}";

        Map<String, Object> requestBody = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));

        try {
            String rawResponse = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("generativelanguage.googleapis.com")
                            .path("/v1beta/models/gemini-2.5-flash:generateContent")
                            .queryParam("key", apiKey)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(String.class);

            JsonNode candidates = objectMapper.readTree(rawResponse).path("candidates");
            if (!candidates.isArray() || candidates.isEmpty()) {
                return fallbackResponse();
            }

            JsonNode parts = candidates.get(0).path("content").path("parts");
            if (!parts.isArray() || parts.isEmpty()) {
                return fallbackResponse();
            }

            String textOutput = parts.get(0).path("text").asText();
            String cleanJson = textOutput.replace("```json", "").replace("```", "").trim();
            return objectMapper.readValue(cleanJson, AiPlanResponse.class);
        } catch (Exception exception) {
            return fallbackResponse();
        }
    }

    private AiPlanResponse fallbackResponse() {
        return new AiPlanResponse(
                "Prioritize tasks due soonest first, focusing on items already in progress.",
                new ArrayList<>());
    }
}
