package com.card_management_system.controller;

import com.card_management_system.dto.LoginRequest;
import com.card_management_system.dto.RegisterRequest;
import com.card_management_system.entity.User;
import com.card_management_system.repository.UserRepository;
import com.card_management_system.service.JwtTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOpt = userRepository.findByUsername(request.getUsername());

        if (userOpt.isEmpty())
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Wrong login or password");

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Wrong login or password");

        try {
            String token = jwtTokenService.generateToken(user.getUsername(), user.getRole());
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error of creating token");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent())
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("User already exists");

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(request.getUsername(), hashedPassword, "ROLE_USER");

        userRepository.save(user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body("User has been successfully registered");
    }
}
