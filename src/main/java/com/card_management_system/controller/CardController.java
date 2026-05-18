package com.card_management_system.controller;

import com.card_management_system.common.CardStatus;
import com.card_management_system.service.CardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/card")
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping("/issue")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> issueCard(@AuthenticationPrincipal Jwt jwt) {
        String ownerUsername = jwt.getSubject();
        return ResponseEntity.ok(cardService.issueCard(ownerUsername));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getCard(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.getCard(id));
    }

    @GetMapping("/{id}/cards")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getCardsByUsername(@AuthenticationPrincipal Jwt jwt) {
        String ownerUsername = jwt.getSubject();
        return ResponseEntity.ok(cardService.getCardsByUsername(ownerUsername));
    }

    @GetMapping("/user-cards")
    public ResponseEntity<?> getUserCardsForAdmin(@RequestParam String username) {
        return ResponseEntity.ok(cardService.getCardsByUsername(username));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PatchMapping("/{id}/block")
    public ResponseEntity<?> blockCardForUser(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getSubject();

        return ResponseEntity.ok(cardService.blockCardAccessForUser(id, username));
    }

    @PatchMapping("/{id}/activate-for-admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> activateCardForAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.changeStatusAccessForAdmin(id, CardStatus.ACTIVE));
    }

    @PatchMapping("/{id}/block-for-admin")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> blockCardForAdmin(@PathVariable Long id) {
        return ResponseEntity.ok(cardService.changeStatusAccessForAdmin(id, CardStatus.BLOCKED));
    }

}
