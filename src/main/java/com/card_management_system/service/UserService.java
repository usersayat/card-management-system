package com.card_management_system.service;

import com.card_management_system.dto.user.UserRequest;
import com.card_management_system.dto.user.UserResponse;
import com.card_management_system.entity.Card;
import com.card_management_system.entity.User;
import com.card_management_system.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse getUser(Long id) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException("User doesn't exist"));
        return toUserResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository
                .findAll()
                .stream()
                .map(this::toUserResponse)
                .toList();
    }

    @Transactional
    public UserResponse editUser(Long id, UserRequest request) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException("User doesn't exist"));

        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());

        return toUserResponse(user);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public UserResponse changeRole(Long id, String newRole) {
        User user = userRepository
                .findById(id)
                .orElseThrow(() -> new NoSuchElementException("User doesn't exist"));

        String formattedRole = newRole.toUpperCase().trim();
        if (!formattedRole.startsWith("ROLE_")) {
            formattedRole = "ROLE_" + formattedRole;
        }

        user.setRole(formattedRole);
        return toUserResponse(userRepository.save(user));
    }

    public UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setPassword(user.getPassword());
        response.setRole(user.getRole());
        List<String> cards = user
                .getCards()
                .stream()
                .map(Card::getNumber)
                .toList();
        response.setCards(cards);
        return response;
    }
}
