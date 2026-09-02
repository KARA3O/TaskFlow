package com.karabo.taskflow.controller;

import com.karabo.taskflow.dto.AiChatRequest;
import com.karabo.taskflow.dto.AiChatResponse;
import com.karabo.taskflow.dto.AiPlanResponse;
import com.karabo.taskflow.model.User;
import com.karabo.taskflow.service.AiService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiRecommendationController {

    private final AiService aiService;

    public AiRecommendationController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/recommendations")
    public ResponseEntity<AiPlanResponse> getRecommendations(
            @AuthenticationPrincipal User user) {

        return ResponseEntity.ok(
                aiService.generatePlanForUser(user)
        );
    }

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(
            @AuthenticationPrincipal User user,
            @RequestBody AiChatRequest request) {

        return ResponseEntity.ok(
                aiService.chat(user, request)
        );
    }
}
