package com.card_management_system.controller;

import com.card_management_system.common.CardStatus;
import com.card_management_system.service.CardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cards")
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

    @GetMapping("/search")
    public ResponseEntity<?> search(@AuthenticationPrincipal Jwt jwt,
                                    @RequestParam(required = false) CardStatus status,
                                    @RequestParam(required = false) Long minBalance,
                                    @RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size) {
        String username = jwt.getSubject();

        return ResponseEntity.ok(cardService.searchCard(username, status, minBalance, page, size));
    }

    @GetMapping("/{id}/cards")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getCardsByUsername(@AuthenticationPrincipal Jwt jwt,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        String ownerUsername = jwt.getSubject();
        return ResponseEntity.ok(cardService.getCardsByUsername(ownerUsername, page, size));
    }

    @GetMapping("/user-cards")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> getUserCardsForAdmin(@RequestParam String username,
                                                  @RequestParam(defaultValue = "0") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(cardService.getCardsByUsername(username, page, size));
    }

    @GetMapping("/{id}/balance")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> getBalance (@PathVariable Long id,
                                         @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getSubject();

        Long balance = cardService.getBalance(id, username);
        return ResponseEntity.ok(Map.of("balance", balance));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
                .build();
    }

    @PatchMapping("/{id}/deposit")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> deposit(@PathVariable Long id,
                                     @AuthenticationPrincipal Jwt jwt,
                                     @RequestParam Long amount) {
        String username = jwt.getSubject();
        return ResponseEntity.ok(cardService.deposit(id, amount, username));
    }

    //Переводы между любыми существующими картами
    @PatchMapping("/{idOfCardFrom}/transfer")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<Void> transfer(@PathVariable Long idOfCardFrom,
                                         @RequestParam String numberOfCardTo,
                                         @RequestParam Long amount,
                                         @AuthenticationPrincipal Jwt jwt) {
        String username = jwt.getSubject();

        cardService.transfer(idOfCardFrom, numberOfCardTo, amount, username);
        return ResponseEntity
                .ok()
                .build();
    }

    @PatchMapping("/{id}/block")
    @PreAuthorize("hasAnyRole('ROLE_USER', 'ROLE_ADMIN')")
    public ResponseEntity<?> blockCardForUser(@PathVariable Long id,
                                              @AuthenticationPrincipal Jwt jwt) {
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
