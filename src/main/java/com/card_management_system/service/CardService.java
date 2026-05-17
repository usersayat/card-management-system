package com.card_management_system.service;

import com.card_management_system.common.ServiceException;
import com.card_management_system.dto.CardRequest;
import com.card_management_system.dto.CardResponse;
import com.card_management_system.entity.Card;
import com.card_management_system.repository.CardRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Service
public class CardService {

    private final CardRepository cardRepository;

    public CardService(CardRepository cardRepository) {
        this.cardRepository = cardRepository;
    }

    public CardResponse createCard(CardRequest request) {
        if (request.getId() != null)
            throw new IllegalArgumentException("id should be null!");
        if (cardRepository.findByNumber(request.getNumber()) != null)
            throw new IllegalArgumentException("Card with the same id already exists");


        Card cardEntity = new Card(request.getNumber(),
                request.getOwner(),
                request.getExpiryDate(),
                request.getStatus(),
                request.getBalance(),
                ZonedDateTime.now(ZoneOffset.UTC),
                ZonedDateTime.now(ZoneOffset.UTC));

        try {
            return toCardResponse(cardRepository.save(cardEntity));
        } catch (Exception e) {
            throw new ServiceException("repository error", e.getCause(), 1, null);
        }
    }

    public CardResponse getCard(Long id) {
        try {
            Card card = cardRepository
                    .findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("card doesn't exist"));
            return toCardResponse(card);
        } catch (Exception e) {
            System.out.println("not found");
            throw new ServiceException("repository error", e.getCause(), 2, null);
        }
    }

    @Transactional
    public CardResponse editCard(Long id, CardRequest request) {
        try {
            Card card = cardRepository
                    .findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("card doesn't exist"));

            card.setBalance(request.getBalance());
            card.setStatus(request.getStatus());
            card.setUpdatedAt(ZonedDateTime.now(ZoneOffset.UTC));

            return toCardResponse(card);
        } catch (Exception e) {
            throw new ServiceException("repository error", e.getCause(), 3, null);
        }
    }

    public void deleteCard(Long id) {
        try {
            cardRepository.deleteById(id);
        } catch (Exception e) {
            throw new ServiceException("repository error", e.getCause(), 4, null);
        }
    }

    public CardResponse toCardResponse(Card cardEntity) {
        return new CardResponse(cardEntity.getId(),
                cardEntity.getNumber(),
                cardEntity.getOwner(),
                cardEntity.getExpiryDate(),
                cardEntity.getStatus(),
                cardEntity.getBalance());
    }
}
