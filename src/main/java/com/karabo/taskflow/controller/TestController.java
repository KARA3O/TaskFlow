package com.karabo.taskflow.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    // Deliberately NOT mapped to "/" - Spring Boot already serves
    // static/index.html at the root. A REST mapping on "/" would shadow
    // the static resource and hide the frontend behind a text response.
    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of("status", "TaskFlow API is running!");
    }
}
