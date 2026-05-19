package com.card_management_system.service;

import com.card_management_system.common.CardStatus;
import com.card_management_system.dto.card.CardResponse;
import com.card_management_system.entity.Card;
import com.card_management_system.entity.CardSpecifications;
import com.card_management_system.entity.User;
import com.card_management_system.repository.CardRepository;
import com.card_management_system.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.NoSuchElementException;
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

    public CardResponse issueCard(String ownerUsername) {
        User owner = userRepository.findByUsername(ownerUsername)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Card card = new Card();
        card.setNumber(generateUniqueCardNumber());
        card.setBalance(0L);
        card.setStatus(CardStatus.ACTIVE);
        card.setExpiryDate(ZonedDateTime.now(ZoneOffset.UTC).plusYears(10));
        card.setOwner(owner);

        return toCardResponse(cardRepository.save(card));
    }

    public CardResponse getCard(Long id) {
            Card card = cardRepository.findById(id)
                    .orElseThrow(() -> new NoSuchElementException("Card doesn't exist"));
            return toCardResponse(card);
    }

    public void deleteCard(Long id) {

            cardRepository.deleteById(id);
    }

    public Page<CardResponse> getCardsByUsername(String username, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User doesn't exist"));

        return cardRepository.findByOwnerUsername(username, pageable)
                .map(this::toCardResponse);
    }

    public Long getBalance(Long id, String username) {
        Card card = cardRepository.findByIdAndOwnerUsername(id, username)
                .orElseThrow(() -> new NoSuchElementException("Card doesn't exist"));

        return card.getBalance();
    }

    public Page<CardResponse> searchCard(String username,
                                 CardStatus status,
                                 Long minBalance,
                                 int page,
                                 int size) {
        Specification<Card> spec = Specification.where(CardSpecifications.hasUsername(username));

        if (status != null)
            spec = spec.and(CardSpecifications.hasStatus(status));

        if (minBalance != null)
            spec = spec.and(CardSpecifications.minBalance(minBalance));

        Pageable pageable = PageRequest.of(page, size);


        return cardRepository.findAll(spec, pageable).map(this::toCardResponse);
    }

    @Transactional
    public CardResponse deposit(Long id, Long amount, String username) {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount should be greater than zero");

        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Card doesn't exist"));

        if (card.getStatus() != CardStatus.ACTIVE)
            throw new IllegalStateException("Card should be active");

        card.setBalance(card.getBalance() + amount);

        return toCardResponse(cardRepository.save(card));
    }

    @Transactional
    public void transfer(Long idOfCardFrom, String numberOfCardTo, Long amount, String username) {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount should be greater than zero");

        Card cardFrom = cardRepository.findByIdAndOwnerUsername(idOfCardFrom, username)
                .orElseThrow(() -> new NoSuchElementException("Sender's card doesn't exist"));

        Card cardTo = cardRepository.findByNumber(numberOfCardTo)
                .orElseThrow(() -> new NoSuchElementException("Recipient's card doesn't exist"));

        if ((cardFrom.getStatus() != CardStatus.ACTIVE) || (cardTo.getStatus() != CardStatus.ACTIVE))
            throw new IllegalStateException("Card should be active");

        if (cardFrom.getBalance() < amount)
            throw new IllegalArgumentException("Not enough money to transfer");

        cardFrom.setBalance(cardFrom.getBalance() - amount);
        cardTo.setBalance(cardTo.getBalance() + amount);

        cardRepository.save(cardFrom);
        cardRepository.save(cardTo);

    }

    @Transactional
    public CardResponse changeStatusAccessForAdmin(Long id, CardStatus status) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Card doesn't exist"));

        card.setStatus(status);
        return toCardResponse(card);
    }

    @Transactional
    public CardResponse blockCardAccessForUser(Long id, String username) {
        Card card = cardRepository.findByIdAndOwnerUsername(id, username)
                .orElseThrow(() -> new NoSuchElementException("Card doesn't exists"));


        card.setStatus(CardStatus.BLOCKED);
        return toCardResponse(card);
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
            isUnique = cardRepository.findByNumber(cardNumber).isEmpty();
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
