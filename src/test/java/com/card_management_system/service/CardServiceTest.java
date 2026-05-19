package com.card_management_system.service;

import com.card_management_system.common.CardStatus;
import com.card_management_system.dto.card.CardResponse;
import com.card_management_system.entity.Card;
import com.card_management_system.entity.User;
import com.card_management_system.repository.CardRepository;
import com.card_management_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest // Запускает полноценный контекст Spring Boot
@ActiveProfiles("test") // Говорит использовать настройки из application-test.yaml
@Transactional // После каждого теста автоматически откатывает любые изменения в базе H2
class CardServiceTest {

    @Autowired
    private CardService cardService;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    private User savedUser;
    private Card savedActiveCard;
    private Card savedRecipientCard;

    @BeforeEach
    void setUp() {
        // Очищаем базу перед каждым тестом, чтобы они были изолированы
        cardRepository.deleteAll();
        userRepository.deleteAll();

        // 1. Создаем и сохраняем реального пользователя в базу H2
        User user = new User();
        ReflectionTestUtils.setField(user, "username", "sayat_developer");
        savedUser = userRepository.save(user);

        // 2. Создаем и сохраняем реальную активную карту
        Card card1 = new Card();
        card1.setNumber("4123456789012345");
        card1.setBalance(1000L);
        card1.setStatus(CardStatus.ACTIVE);
        card1.setOwner(savedUser);
        card1.setExpiryDate(ZonedDateTime.now().plusYears(1));
        savedActiveCard = cardRepository.save(card1);

        // 3. Создаем и сохраняем карту получателя для переводов
        Card card2 = new Card();
        card2.setNumber("5987654321098765");
        card2.setBalance(500L);
        card2.setStatus(CardStatus.ACTIVE);
        card2.setOwner(savedUser);
        card2.setExpiryDate(ZonedDateTime.now().plusYears(1));
        savedRecipientCard = cardRepository.save(card2);
    }

    @Test
    @DisplayName("Интеграция: Успешное пополнение карты в базе данных")
    void deposit_Success() {
        // Act - вызываем реальный сервис, который сделает реальный SQL-запрос
        CardResponse response = cardService.deposit(savedActiveCard.getId(), 500L, "sayat_developer");

        // Assert - проверяем DTO ответ
        assertNotNull(response);
        assertEquals(1500L, response.getBalance());

        // Дополнительно проверяем, что в самой БД баланс тоже обновился!
        Card cardFromDb = cardRepository.findById(savedActiveCard.getId()).orElseThrow();
        assertEquals(1500L, cardFromDb.getBalance());
    }

    @Test
    @DisplayName("Интеграция: Ошибка пополнения, если карта заблокирована в БД")
    void deposit_ThrowsException_WhenCardIsBlocked() {
        // Arrange - блокируем карту прямо в базе данных H2
        savedActiveCard.setStatus(CardStatus.BLOCKED);
        cardRepository.save(savedActiveCard);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            cardService.deposit(savedActiveCard.getId(), 100L, "sayat_developer");
        });

        // Проверяем, что баланс в базе не изменился
        Card cardFromDb = cardRepository.findById(savedActiveCard.getId()).orElseThrow();
        assertEquals(1000L, cardFromDb.getBalance());
    }

    @Test
    @DisplayName("Интеграция: Успешный перевод между картами в H2")
    void transfer_Success() {
        // Act
        cardService.transfer(savedActiveCard.getId(), "5987654321098765", 300L, "sayat_developer");

        // Assert - вытаскиваем обе карты из базы и смотрим их новые балансы
        Card senderFromDb = cardRepository.findById(savedActiveCard.getId()).orElseThrow();
        Card recipientFromDb = cardRepository.findById(savedRecipientCard.getId()).orElseThrow();

        assertEquals(700L, senderFromDb.getBalance(), "С отправителя должно списаться 300");
        assertEquals(800L, recipientFromDb.getBalance(), "Получателю должно прийти 300");
    }

    @Test
    @DisplayName("Интеграция: Ошибка перевода при нехватке денег (Проверка транзакционности)")
    void transfer_ThrowsException_WhenNotEnoughMoney() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            cardService.transfer(savedActiveCard.getId(), "5987654321098765", 9999L, "sayat_developer");
        });

        // Проверяем, что из-за транзакции балансы обеих карт в базе вообще не изменились
        Card senderFromDb = cardRepository.findById(savedActiveCard.getId()).orElseThrow();
        Card recipientFromDb = cardRepository.findById(savedRecipientCard.getId()).orElseThrow();

        assertEquals(1000L, senderFromDb.getBalance());
        assertEquals(500L, recipientFromDb.getBalance());
    }

    @Test
    @DisplayName("Интеграция: Успешный выпуск новой карты в БД")
    void issueCard_Success() {
        // Act
        CardResponse response = cardService.issueCard("sayat_developer");

        // Assert
        assertNotNull(response);
        assertNotNull(response.getId(), "База данных H2 должна была сама сгенерировать ID для новой карты");
        assertEquals(0L, response.getBalance());
        assertEquals(CardStatus.ACTIVE, response.getStatus());

        // Проверяем, что в репозитории теперь физически стало на 1 карту больше
        assertTrue(cardRepository.findByNumber(savedActiveCard.getNumber()).isPresent());
    }
}