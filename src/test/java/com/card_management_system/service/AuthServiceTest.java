package com.card_management_system.service;

import com.card_management_system.dto.auth.RegisterRequest;
import com.card_management_system.entity.User;
import com.card_management_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = "admin.registration-key=secret_admin_123")
@ActiveProfiles("test") // Использует твой application-test.yaml с H2
@Transactional // Откатывает изменения в БД после каждого теста
class AuthServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Успешная регистрация обычного пользователя (роль по умолчанию)")
    void register_WhenDefaultRole_SavesUserWithRoleUser() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("alex_jones");
        request.setPassword("my_password");
        request.setRole(""); // Пустая роль, должна сработать дефолтная

        // When
        authService.register(request);

        // Then
        Optional<User> savedUserOpt = userRepository.findByUsername("alex_jones");
        assertThat(savedUserOpt).isPresent();

        User savedUser = savedUserOpt.get();
        assertThat(savedUser.getRole()).isEqualTo("ROLE_USER");

        // Проверяем, что пароль в БД зашифрован, а не лежит в открытом виде
        assertThat(savedUser.getPassword()).isNotEqualTo("my_password");
        assertThat(passwordEncoder.matches("my_password", savedUser.getPassword())).isTrue();
    }

    @Test
    @DisplayName("Ошибка при регистрации, если имя пользователя уже занято")
    void register_WhenUserAlreadyExists_ThrowsIllegalArgumentException() {
        // Given
        User existingUser = new User();
        existingUser.setUsername("duplicate_user");
        existingUser.setPassword("any_pass");
        existingUser.setRole("ROLE_USER");
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("duplicate_user");
        request.setPassword("new_pass");

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User already exists");
    }

    @Test
    @DisplayName("Успешная регистрация администратора с правильным секретным ключом")
    void register_WhenAdminWithCorrectKey_SavesUserWithRoleAdmin() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("boss_man");
        request.setPassword("admin_pass");
        request.setRole("admin"); // Без префикса ROLE_ и в нижнем регистре
        request.setAdminSecretKey("secret_admin_123"); // Ключ совпадает с проперти вверху класса

        // When
        authService.register(request);

        // Then
        Optional<User> savedUserOpt = userRepository.findByUsername("boss_man");
        assertThat(savedUserOpt).isPresent();
        assertThat(savedUserOpt.get().getRole()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Ошибка регистрации администратора, если секретный ключ неверный")
    void register_WhenAdminWithWrongKey_ThrowsIllegalArgumentException() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("fake_admin");
        request.setPassword("pass");
        request.setRole("ROLE_ADMIN");
        request.setAdminSecretKey("wrong_key_111");

        // When & Then
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Access denied");

        // Проверяем, что в базу никто не записался
        assertThat(userRepository.findByUsername("fake_admin")).isEmpty();
    }

    @Test
    @DisplayName("Успешная аутентификация при правильном логине и пароле")
    void authenticate_WhenCredentialsAreCorrect_ReturnsUser() {
        // Given
        User user = new User();
        user.setUsername("valid_user");
        // Пароль в базу нужно сохранить уже зашифрованным, как это делает реальное приложение
        user.setPassword(passwordEncoder.encode("correct_password"));
        user.setRole("ROLE_USER");
        userRepository.save(user);

        // When
        User authenticatedUser = authService.authenticate("valid_user", "correct_password");

        // Then
        assertThat(authenticatedUser).isNotNull();
        assertThat(authenticatedUser.getUsername()).isEqualTo("valid_user");
    }

    @Test
    @DisplayName("Ошибка аутентификации, если пользователя нет в базе")
    void authenticate_WhenUserNotFound_ThrowsIllegalArgumentException() {
        // When & Then
        assertThatThrownBy(() -> authService.authenticate("ghost_user", "any_password"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Wrong login");
    }

    @Test
    @DisplayName("Ошибка аутентификации, если пароль не совпадает")
    void authenticate_WhenPasswordIsWrong_ThrowsIllegalArgumentException() {
        // Given
        User user = new User();
        user.setUsername("test_user");
        user.setPassword(passwordEncoder.encode("real_password"));
        user.setRole("ROLE_USER");
        userRepository.save(user);

        // When & Then
        assertThatThrownBy(() -> authService.authenticate("test_user", "wrong_password"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Wrong login or password");
    }
}