package com.karabo.taskflow.controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import com.karabo.taskflow.dto.LoginRequest;
import com.karabo.taskflow.dto.LoginResponse;
import com.karabo.taskflow.dto.UserResponse;
import com.karabo.taskflow.model.User;
import com.karabo.taskflow.security.JwtService;
import com.karabo.taskflow.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserService userService,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()));

        User user = userService.findByEmail(request.getEmail());
        String token = jwtService.generateToken(user);
        UserResponse response = userService.mapToResponse(user);

        return new LoginResponse(token, response);
    }
}
