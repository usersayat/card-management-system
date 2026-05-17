package com.card_management_system.controller;

import com.card_management_system.dto.LoginRequest;
import com.card_management_system.dto.RegisterRequest;
import com.card_management_system.entity.User;
import com.card_management_system.service.JwtTokenService;
import com.card_management_system.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final JwtTokenService jwtTokenService;

    public AuthController(UserService userService, JwtTokenService jwtTokenService) {
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.authenticate(request.getUsername(), request.getPassword());
            String token = jwtTokenService.generateToken(user.getUsername(), user.getRole());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error of creating token");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            userService.register(request);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("User has been successfully registered");

        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }
}
