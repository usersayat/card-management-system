package com.card_management_system.service;

import com.card_management_system.common.CardStatus;
import com.card_management_system.dto.card.CardResponse;
import com.card_management_system.entity.Card;
import com.card_management_system.entity.User;
import com.card_management_system.repository.CardRepository;
import com.card_management_system.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Random;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;
    private final Random random = new Random();

    public CardService(CardRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    public CardResponse getCard(Long id) {

            Card card = cardRepository
                    .findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("card doesn't exist"));
            return toCardResponse(card);
    }

    public void deleteCard(Long id) {

            cardRepository.deleteById(id);
    }

    public CardResponse issueCard(String ownerUsername) {
        User owner = userRepository
                .findByUsername(ownerUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Card card = new Card();
        card.setNumber(generateUniqueCardNumber());
        card.setBalance(0L);
        card.setStatus(CardStatus.ACTIVE);
        card.setExpiryDate(ZonedDateTime.now(ZoneOffset.UTC).plusYears(10));
        card.setOwner(owner);

        return toCardResponse(cardRepository.save(card));
    }

    @Transactional
    public CardResponse changeStatusAccessForAdmin(Long id, CardStatus status) {
        Card card = cardRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Card doesn't exist"));

        card.setStatus(status);
        return toCardResponse(card);
    }

    @Transactional
    public CardResponse blockCardAccessForUser(Long id, String username) {
        Card card = cardRepository
                .findByIdAndOwnerUsername(id, username);
        if (card == null)
            throw new IllegalArgumentException("Card doesn't exists");

        card.setStatus(CardStatus.BLOCKED);
        return toCardResponse(card);
    }

    public List<CardResponse> getCardsByUsername(String username) {
        userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User doesn't exist"));

        return cardRepository
                .findByOwnerUsername(username)
                .stream()
                .map(this::toCardResponse)
                .toList();
    }

    /**
     * Вспомогательный метод для генерации случайного и уникального 16-значного номера карты
     */
    private String generateUniqueCardNumber() {
        String cardNumber;
        boolean isUnique = false;

        do {
            StringBuilder sb = new StringBuilder();
            // Первую цифру ставим, например, 4 (Visa) или 5 (Mastercard)
            sb.append(random.nextInt(2) == 0 ? "4" : "5");

            // Генерируем остальные 15 цифр
            for (int i = 0; i < 15; i++) {
                sb.append(random.nextInt(10));
            }
            cardNumber = sb.toString();

            // Проверяем в MySQL, свободен ли этот номер
            isUnique = cardRepository.findByNumber(cardNumber) == null;
        } while (!isUnique); // Если номер уже занят, цикл запустится заново

        return cardNumber;
    }

    private CardResponse toCardResponse(Card cardEntity) {
        return new CardResponse(cardEntity.getId(),
                cardEntity.getNumber(),
                cardEntity.getOwner().getUsername(),
                cardEntity.getExpiryDate(),
                cardEntity.getStatus(),
                cardEntity.getBalance());
    }

}
