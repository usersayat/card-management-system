package com.card_management_system.service;

import com.card_management_system.dto.user.UserRequest;
import com.card_management_system.dto.user.UserResponse;
import com.card_management_system.entity.Card;
import com.card_management_system.entity.User;
import com.card_management_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test") // Использует application-test.yaml с H2
@Transactional // Откатывает изменения в БД после каждого теста
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private User savedUser;

    @BeforeEach
    void setUp() {
        // Очищаем базу перед каждым тестом (на всякий случай)
        userRepository.deleteAll();

        // Создаем тестового пользователя
        User user = new User();
        user.setUsername("Sayat");
        user.setPassword("password123");
        user.setRole("ROLE_USER");
        user.setCards(new ArrayList<>()); // Пока без карт

        savedUser = userRepository.save(user);
    }

    @Test
    @DisplayName("Успешное получение существующего пользователя")
    void getUser_WhenUserExists_ReturnsCorrectResponse() {
        // When
        UserResponse response = userService.getUser(savedUser.getId());

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(savedUser.getId());
        assertThat(response.getUsername()).isEqualTo("Sayat");
        assertThat(response.getRole()).isEqualTo("ROLE_USER");
    }

    @Test
    @DisplayName("Выброс исключения, если пользователя с таким ID нет")
    void getUser_WhenUserDoesNotExist_ThrowsNoSuchElementException() {
        // Given
        Long nonExistentId = savedUser.getId() + 999L;

        // When & Then
        assertThatThrownBy(() -> userService.getUser(nonExistentId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage("User doesn't exist");
    }

    @Test
    @DisplayName("Успешное редактирование полей пользователя")
    void editUser_UpdatesFieldsInDatabase() {
        // Given
        UserRequest request = new UserRequest();
        request.setUsername("NewSayat");
        request.setPassword("newPassword");
        request.setRole("ROLE_ADMIN");

        // When
        UserResponse response = userService.editUser(savedUser.getId(), request);

        // Then
        assertThat(response.getUsername()).isEqualTo("NewSayat");
        assertThat(response.getRole()).isEqualTo("ROLE_ADMIN");

        // Дополнительно проверяем, что в реальной БД данные тоже обновились
        User updatedUserInDb = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUserInDb.getUsername()).isEqualTo("NewSayat");
        assertThat(updatedUserInDb.getPassword()).isEqualTo("newPassword");
    }

    @Test
    @DisplayName("Удаление пользователя полностью стирает его из БД")
    void deleteUser_RemovesUserFromDatabase() {
        // When
        userService.deleteUser(savedUser.getId());

        // Then
        boolean exists = userRepository.existsById(savedUser.getId());
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Смена роли: автоматически добавляет префикс ROLE_ и убирает пробелы")
    void changeRole_FormatsAndSavesNewRole() {
        // When
        UserResponse response = userService.changeRole(savedUser.getId(), "  manager  ");

        // Then
        assertThat(response.getRole()).isEqualTo("ROLE_MANAGER");

        // Проверяем, что в БД роль тоже изменилась
        User userInDb = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(userInDb.getRole()).isEqualTo("ROLE_MANAGER");
    }

    @Test
    @DisplayName("Смена роли: не дублирует префикс, если он уже есть")
    void changeRole_DoesNotDuplicatePrefix() {
        // When
        UserResponse response = userService.changeRole(savedUser.getId(), "ROLE_ADMIN");

        // Then
        assertThat(response.getRole()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Маппинг пользователя вместе с его картами")
    void toUserResponse_IncludesCardNumbers() {
        // Given
        // Чтобы проверить маппинг карт, добавим их пользователю прямо в базу
        User userWithCards = new User();
        userWithCards.setUsername("CardHolder");
        userWithCards.setPassword("pass");
        userWithCards.setRole("ROLE_USER");

        Card card1 = new Card();
        card1.setNumber("1111-2222");
        card1.setOwner(userWithCards); // Связываем карту с пользователем

        Card card2 = new Card();
        card2.setNumber("3333-4444");
        card2.setOwner(userWithCards);

        userWithCards.setCards(List.of(card1, card2));

        // Сохраняем в БД (если у тебя настроено CascadeType.ALL, карты сохранятся вместе с юзером)
        User savedUserWithCards = userRepository.save(userWithCards);

        // When
        UserResponse response = userService.getUser(savedUserWithCards.getId());

        // Then
        assertThat(response.getCards())
                .hasSize(2)
                .containsExactlyInAnyOrder("1111-2222", "3333-4444");
    }
}