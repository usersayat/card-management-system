package com.card_management_system.service;

import com.card_management_system.dto.auth.RegisterRequest;
import com.card_management_system.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.card_management_system.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.registration-key}")
    private String adminRegistrationKey;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent())
            throw new IllegalArgumentException("User already exists");

        User user = new User();
        user.setUsername(request.getUsername());

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(hashedPassword);

        if (request.getRole() == null || request.getRole().trim().isEmpty()) {
            user.setRole("ROLE_USER");
        }
        else {
            String finalRole = request.getRole().toUpperCase().trim();
            if (!finalRole.startsWith("ROLE_"))
                finalRole = "ROLE_" + finalRole;

            if ("ROLE_ADMIN".equals(finalRole))
                if (!adminRegistrationKey.equals(request.getAdminSecretKey()))
                    throw new IllegalArgumentException("Access denied");

            user.setRole(finalRole);
        }

        userRepository.save(user);
    }

    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Wrong login"));

        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new IllegalArgumentException("Wrong login or password");

        return user;
    }
}
