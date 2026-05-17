package com.card_management_system.controller;

import com.card_management_system.dto.CardRequest;
import com.card_management_system.service.CardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping()
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> createCard(@RequestBody CardRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(cardService.createCard(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCard(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> editCard(@PathVariable Long id,
                                     @RequestBody CardRequest request) {
        return ResponseEntity.ok(cardService.editCard(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }
}
