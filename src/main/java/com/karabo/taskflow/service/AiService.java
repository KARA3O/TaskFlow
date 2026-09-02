package com.karabo.taskflow.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.karabo.taskflow.dto.AiChatMessage;
import com.karabo.taskflow.dto.AiChatRequest;
import com.karabo.taskflow.dto.AiChatResponse;
import com.karabo.taskflow.dto.AiPlanResponse;
import com.karabo.taskflow.dto.RecommendationDto;
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

        List<Task> tasks = getActiveTasks(user);

        if (tasks.isEmpty()) {
            return new AiPlanResponse(
                    "Your task list is clear. This is a good time to review upcoming goals or add your next priorities.",
                    new ArrayList<>(),
                    List.of(
                            "No active work is currently blocking your schedule.",
                            "Consider planning tomorrow before ending the day."
                    ),
                    0,
                    0,
                    0,
                    0
            );
        }

        WorkloadContext context = buildContext(tasks);

        if (apiKey == null || apiKey.isBlank()) {
            return fallbackPlan(tasks, context);
        }

        String prompt = buildPlanPrompt(tasks, context);

        try {
            String rawResponse = callGemini(prompt);

            JsonNode json = extractJson(rawResponse);

            AiPlanResponse result =
                    objectMapper.treeToValue(json, AiPlanResponse.class);

            if (result.getRecommendations() == null) {
                result.setRecommendations(new ArrayList<>());
            }

            if (result.getInsights() == null) {
                result.setInsights(new ArrayList<>());
            }

            // Always use real application data for workload metrics.
            result.setTotalEstimatedMinutes(context.totalMinutes());
            result.setOverdueCount(context.overdue());
            result.setDueTodayCount(context.todayCount());
            result.setDueTomorrowCount(context.tomorrowCount());

            // Only allow recommendations for tasks that actually exist.
            List<String> validTitles = tasks.stream()
                    .map(Task::getTitle)
                    .filter(title -> title != null && !title.isBlank())
                    .toList();

            List<RecommendationDto> validRecommendations =
                    result.getRecommendations()
                            .stream()
                            .filter(rec -> rec != null)
                            .filter(rec ->
                                    rec.getTitle() != null &&
                                    !rec.getTitle().isBlank())
                            .filter(rec ->
                                    validTitles.stream()
                                            .anyMatch(title ->
                                                    title.equalsIgnoreCase(
                                                            rec.getTitle())))
                            .filter(rec ->
                                    rec.getReason() != null &&
                                    !rec.getReason().isBlank())
                            .filter(rec ->
                                    rec.getAction() != null &&
                                    !rec.getAction().isBlank())
                            .limit(Math.min(5, tasks.size()))
                            .collect(Collectors.toCollection(ArrayList::new));

            // If Gemini produces unusable recommendations,
            // use the deterministic local fallback.
            if (validRecommendations.isEmpty()) {
                return fallbackPlan(tasks, context);
            }

            result.setRecommendations(validRecommendations);

            return result;

        } catch (Exception exception) {
            return fallbackPlan(tasks, context);
        }
    }

    public AiChatResponse chat(
            User user,
            AiChatRequest request) {

        List<Task> tasks = getActiveTasks(user);

        if (request == null ||
                request.getMessage() == null ||
                request.getMessage().isBlank()) {

            return new AiChatResponse(
                    "Tell me what you want help with — for example, ask me what to work on first, ask me to break down a task, or ask me to plan your day."
            );
        }

        if (apiKey == null || apiKey.isBlank()) {
            return fallbackChat(
                    tasks,
                    request.getMessage()
            );
        }

        WorkloadContext context = buildContext(tasks);

        String prompt = buildChatPrompt(
                tasks,
                context,
                request.getMessage(),
                request.getHistory()
        );

        try {
            String rawResponse = callGemini(prompt);

            JsonNode json = extractJson(rawResponse);

            return objectMapper.treeToValue(
                    json,
                    AiChatResponse.class
            );

        } catch (Exception exception) {
            return fallbackChat(
                    tasks,
                    request.getMessage()
            );
        }
    }

    private List<Task> getActiveTasks(User user) {

        return taskRepository
                .findByOwnerAndStatusNotOrderByDueDateAsc(
                        user,
                        TaskStatus.COMPLETED
                );
    }

    private WorkloadContext buildContext(List<Task> tasks) {

        LocalDate today = LocalDate.now();

        int overdue = 0;
        int todayCount = 0;
        int tomorrowCount = 0;
        int totalMinutes = 0;
        int inProgress = 0;

        for (Task task : tasks) {

            LocalDate due = task.getDueDate();

            if (due.isBefore(today)) {
                overdue++;
            }

            if (due.equals(today)) {
                todayCount++;
            }

            if (due.equals(today.plusDays(1))) {
                tomorrowCount++;
            }

            if (task.getEstimatedMinutes() != null) {
                totalMinutes += Math.max(
                        1,
                        task.getEstimatedMinutes()
                );
            } else {
                totalMinutes += 30;
            }

            if (task.getStatus() == TaskStatus.IN_PROGRESS) {
                inProgress++;
            }
        }

        return new WorkloadContext(
                today,
                overdue,
                todayCount,
                tomorrowCount,
                totalMinutes,
                inProgress
        );
    }

    private String buildPlanPrompt(
            List<Task> tasks,
            WorkloadContext context) {

        StringBuilder taskData = new StringBuilder();

        for (Task task : tasks) {

            long daysUntil =
                    ChronoUnit.DAYS.between(
                            context.today(),
                            task.getDueDate()
                    );

            String taskBlock = """
                    TASK
                    id: %s
                    title: %s
                    description: %s
                    status: %s
                    dueDate: %s
                    daysUntilDue: %s
                    estimatedMinutes: %s
                    createdAt: %s

                    """.formatted(
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    task.getStatus(),
                    task.getDueDate(),
                    daysUntil,
                    task.getEstimatedMinutes(),
                    task.getCreatedAt()
            );

            taskData.append(taskBlock);
        }

        return """
                You are the intelligent productivity assistant inside TaskFlow.

                Your job is NOT to give generic productivity advice.

                Analyze the user's ACTUAL tasks and produce a useful, specific plan.

                Today's date: %s

                WORKLOAD:
                Active tasks: %s
                Overdue: %s
                Due today: %s
                Due tomorrow: %s
                Already in progress: %s
                Estimated total workload: %s minutes

                TASK DATA:
                %s

                RULES:

                1. Prioritize based on urgency, importance, dependencies implied by descriptions,
                   current progress, and estimated effort.

                2. Overdue tasks should receive strong attention.

                3. A task due today should normally rank above a task due next week.

                4. Avoid recommending every task equally.

                5. Give 3 to 5 genuinely useful recommendations when enough tasks exist.
                   If fewer active tasks exist, only recommend the tasks that actually exist.

                6. Each recommendation must explain WHY that specific task matters NOW.

                7. Give a concrete next action, not vague advice.

                8. Use the estimated minutes when deciding between tasks.

                9. Do not invent tasks.

                10. Do not invent deadlines.

                11. If the workload is too large for one day, explicitly say so
                    and identify what can realistically move later.

                12. Identify useful insights such as:
                    - overload
                    - overdue work
                    - too many tasks in progress
                    - quick wins
                    - large tasks that should be started early
                    - scheduling conflicts

                Return ONLY valid JSON.

                Required JSON:

                {
                  "summary": "specific overview of what the user should do",
                  "recommendations": [
                    {
                      "title": "exact task title",
                      "reason": "specific explanation",
                      "priority": "HIGH|MEDIUM|LOW",
                      "action": "concrete next action",
                      "estimatedMinutes": 30
                    }
                  ],
                  "insights": [
                    "specific insight about this user's workload"
                  ],
                  "totalEstimatedMinutes": 120,
                  "overdueCount": 1,
                  "dueTodayCount": 2,
                  "dueTomorrowCount": 1
                }
                """.formatted(
                context.today(),
                tasks.size(),
                context.overdue(),
                context.todayCount(),
                context.tomorrowCount(),
                context.inProgress(),
                context.totalMinutes(),
                taskData.toString()
        );
    }

    private String buildChatPrompt(
            List<Task> tasks,
            WorkloadContext context,
            String message,
            List<AiChatMessage> history) {

        String taskData = tasks.stream()
                .map(task -> """
                        - %s | status=%s | due=%s | minutes=%s | description=%s
                        """.formatted(
                        task.getTitle(),
                        task.getStatus(),
                        task.getDueDate(),
                        task.getEstimatedMinutes(),
                        task.getDescription()
                ))
                .collect(Collectors.joining("\n"));

        String conversation = "";

        if (history != null && !history.isEmpty()) {

            conversation = history.stream()
                    .limit(12)
                    .map(item ->
                            "%s: %s".formatted(
                                    item.getRole(),
                                    item.getContent()
                            ))
                    .collect(Collectors.joining("\n"));
        }

        return """
                You are TaskFlow AI, a highly capable personal productivity assistant.

                Be conversational, practical and specific.

                You have access to the user's current TaskFlow tasks.

                TODAY: %s

                CURRENT WORKLOAD:
                Active tasks: %s
                Overdue: %s
                Due today: %s
                Due tomorrow: %s
                Estimated workload: %s minutes
                In progress: %s

                TASKS:
                %s

                PREVIOUS CONVERSATION:
                %s

                USER MESSAGE:
                %s

                Your capabilities include:

                - deciding what the user should work on first
                - explaining why a task should be prioritized
                - creating a realistic daily plan
                - breaking a task into smaller steps
                - identifying overdue or risky work
                - estimating workload
                - finding quick wins
                - suggesting what can be postponed
                - helping the user recover from an overloaded schedule
                - answering follow-up questions naturally

                IMPORTANT:

                Do not repeat generic productivity advice.

                Refer to the user's actual tasks whenever relevant.

                Never invent a task.

                Never invent a deadline.

                Never claim you changed a task unless the application actually performs that action.

                If the user asks to break down a task, return useful concrete steps.

                If the user asks for a schedule, create schedule blocks using the known tasks.

                Return ONLY valid JSON:

                {
                  "reply": "natural conversational answer",
                  "suggestions": [
                    "useful follow-up question or action"
                  ],
                  "breakdown": [
                    "step 1",
                    "step 2"
                  ],
                  "schedule": [
                    {
                      "time": "09:00",
                      "taskTitle": "exact task title",
                      "durationMinutes": 45,
                      "reason": "why this belongs here"
                    }
                  ]
                }
                """.formatted(
                context.today(),
                tasks.size(),
                context.overdue(),
                context.todayCount(),
                context.tomorrowCount(),
                context.totalMinutes(),
                context.inProgress(),
                taskData,
                conversation,
                message
        );
    }

    private String callGemini(String prompt) {

        Map<String, Object> requestBody = Map.of(
                "systemInstruction",
                Map.of(
                        "parts",
                        List.of(
                                Map.of(
                                        "text",
                                        "You are TaskFlow AI. Return accurate JSON only."
                                )
                        )
                ),
                "contents",
                List.of(
                        Map.of(
                                "role",
                                "user",
                                "parts",
                                List.of(
                                        Map.of(
                                                "text",
                                                prompt
                                        )
                                )
                        )
                ),
                "generationConfig",
                Map.of(
                        "temperature",
                        0.85,
                        "responseMimeType",
                        "application/json"
                )
        );

        return restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .host("generativelanguage.googleapis.com")
                        .path(
                                "/v1beta/models/gemini-2.5-flash:generateContent"
                        )
                        .queryParam("key", apiKey)
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(String.class);
    }

    private JsonNode extractJson(String rawResponse)
            throws Exception {

        if (rawResponse == null || rawResponse.isBlank()) {
            throw new IllegalStateException(
                    "Gemini returned an empty response"
            );
        }

        JsonNode candidates =
                objectMapper
                        .readTree(rawResponse)
                        .path("candidates");

        if (!candidates.isArray() ||
                candidates.isEmpty()) {

            throw new IllegalStateException(
                    "Gemini returned no candidates"
            );
        }

        JsonNode parts =
                candidates
                        .get(0)
                        .path("content")
                        .path("parts");

        if (!parts.isArray() ||
                parts.isEmpty()) {

            throw new IllegalStateException(
                    "Gemini returned no content"
            );
        }

        String text =
                parts.get(0)
                        .path("text")
                        .asText();

        if (text == null || text.isBlank()) {
            throw new IllegalStateException(
                    "Gemini returned empty text"
            );
        }

        String clean = text
                .replace("```json", "")
                .replace("```", "")
                .trim();

        return objectMapper.readTree(clean);
    }

    private AiPlanResponse fallbackPlan(
            List<Task> tasks,
            WorkloadContext context) {

        List<RecommendationDto> recommendations =
                new ArrayList<>();

        List<Task> sorted = tasks.stream()
                .sorted((a, b) -> {

                    int aScore =
                            priorityScore(
                                    a,
                                    context.today()
                            );

                    int bScore =
                            priorityScore(
                                    b,
                                    context.today()
                            );

                    return Integer.compare(
                            bScore,
                            aScore
                    );
                })
                .limit(5)
                .toList();

        for (Task task : sorted) {

            int score =
                    priorityScore(
                            task,
                            context.today()
                    );

            String priority =
                    score >= 100
                            ? "HIGH"
                            : score >= 50
                            ? "MEDIUM"
                            : "LOW";

            String reason;

            if (task.getDueDate()
                    .isBefore(context.today())) {

                reason =
                        "This task is overdue, so it should be addressed before lower-urgency work.";

            } else if (task.getDueDate()
                    .equals(context.today())) {

                reason =
                        "This task is due today and should be protected from being pushed into tomorrow.";

            } else if (task.getStatus() ==
                    TaskStatus.IN_PROGRESS) {

                reason =
                        "It is already in progress, so finishing it can reduce context switching.";

            } else if (task.getDueDate()
                    .equals(context.today().plusDays(1))) {

                reason =
                        "This task is due tomorrow, so starting it now reduces the risk of a last-minute rush.";

            } else {

                reason =
                        "This is currently one of the strongest candidates based on its deadline and workload.";
            }

            recommendations.add(
                    new RecommendationDto(
                            task.getTitle(),
                            reason,
                            priority,
                            buildTaskAction(task),
                            task.getEstimatedMinutes()
                    )
            );
        }

        String summary;

        if (context.overdue() > 0) {

            summary =
                    "You have " +
                    context.overdue() +
                    " overdue task(s). Clear the most urgent overdue item first, then protect today's deadlines.";

        } else if (context.todayCount() > 0) {

            summary =
                    "You have " +
                    context.todayCount() +
                    " task(s) due today. Focus on those before taking on lower-priority work.";

        } else if (context.tomorrowCount() > 0) {

            summary =
                    "Your immediate deadlines are tomorrow. Use the available time today to make meaningful progress before the deadline arrives.";

        } else {

            summary =
                    "Your deadlines are relatively controlled. Use your current in-progress work and nearest deadlines to decide what to tackle next.";
        }

        List<String> insights = new ArrayList<>();

        insights.add(
                "Estimated active workload: " +
                context.totalMinutes() +
                " minutes."
        );

        insights.add(
                "Currently in progress: " +
                context.inProgress() +
                " task(s)."
        );

        if (context.overdue() > 0) {

            insights.add(
                    "There are " +
                    context.overdue() +
                    " overdue task(s) requiring attention."
            );
        }

        if (context.todayCount() > 0) {

            insights.add(
                    context.todayCount() +
                    " task(s) are due today."
            );
        }

        if (context.tomorrowCount() > 0) {

            insights.add(
                    context.tomorrowCount() +
                    " task(s) are due tomorrow."
            );
        }

        return new AiPlanResponse(
                summary,
                recommendations,
                insights,
                context.totalMinutes(),
                context.overdue(),
                context.todayCount(),
                context.tomorrowCount()
        );
    }

    private String buildTaskAction(Task task) {

        String description = task.getDescription();

        if (description != null &&
                !description.isBlank()) {

            String cleaned =
                    description.trim();

            if (cleaned.length() > 120) {
                cleaned =
                        cleaned.substring(0, 120) +
                        "...";
            }

            return "Start by working on this concrete part of the task: " +
                    cleaned;
        }

        return "Start with the smallest concrete step for this task.";
    }

    private int priorityScore(
            Task task,
            LocalDate today) {

        int score = 0;

        if (task.getDueDate().isBefore(today)) {

            score += 150;

        } else {

            long days =
                    ChronoUnit.DAYS.between(
                            today,
                            task.getDueDate()
                    );

            if (days == 0) {

                score += 120;

            } else if (days == 1) {

                score += 90;

            } else if (days <= 3) {

                score += 60;

            } else if (days <= 7) {

                score += 30;
            }
        }

        if (task.getStatus() ==
                TaskStatus.IN_PROGRESS) {

            score += 25;
        }

        int minutes =
                task.getEstimatedMinutes() == null
                        ? 30
                        : task.getEstimatedMinutes();

        if (minutes <= 30) {
            score += 10;
        }

        return score;
    }

    private AiChatResponse fallbackChat(
            List<Task> tasks,
            String message) {

        AiChatResponse response =
                new AiChatResponse();

        if (tasks.isEmpty()) {

            response.setReply(
                    "You currently have no active tasks. Add a task and I can help you prioritize it, break it down, or plan when to work on it."
            );

            return response;
        }

        String lower =
                message.toLowerCase();

        if (lower.contains("first") ||
                lower.contains("priorit") ||
                lower.contains("today")) {

            Task first =
                    tasks.stream()
                            .min((a, b) ->
                                    Integer.compare(
                                            priorityScore(
                                                    b,
                                                    LocalDate.now()
                                            ),
                                            priorityScore(
                                                    a,
                                                    LocalDate.now()
                                            )
                                    ))
                            .orElse(tasks.get(0));

            response.setReply(
                    "Based on your deadlines, I would start with \"" +
                    first.getTitle() +
                    "\". It currently has the strongest priority signal in your active workload."
            );

        } else if (lower.contains("break") ||
                lower.contains("steps")) {

            Task task =
                    tasks.get(0);

            response.setReply(
                    "A good way to break down \"" +
                    task.getTitle() +
                    "\" is to first define the desired outcome, then identify the first concrete action, complete that action, review the result, and finish the remaining work."
            );

            response.setBreakdown(
                    List.of(
                            "Define the exact outcome.",
                            "Identify the smallest first action.",
                            "Complete the first action without switching tasks.",
                            "Review what remains.",
                            "Finish or schedule the remaining work."
                    )
            );

        } else {

            response.setReply(
                    "I can help you decide what to work on, explain priorities, build a schedule, break down a task, or identify workload risks. Ask me something specific about your current tasks."
            );
        }

        return response;
    }

    private record WorkloadContext(
            LocalDate today,
            int overdue,
            int todayCount,
            int tomorrowCount,
            int totalMinutes,
            int inProgress
    ) {
    }
}