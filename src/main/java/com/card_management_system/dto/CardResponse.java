package com.card_management_system.dto;

import com.card_management_system.common.CardStatus;

import java.time.ZonedDateTime;

public class CardResponse {

    private final Long id;

    private String number;
    private String owner;

    private ZonedDateTime expiryDate;

    private CardStatus status;
    private Long balance;

    public CardResponse(Long id, String number, String owner, ZonedDateTime expiryDate, CardStatus status, Long balance) {
        this.id = id;
        this.number = number;
        this.owner = owner;
        this.expiryDate = expiryDate;
        this.status = status;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public String getOwner() {
        return owner;
    }

    public ZonedDateTime getExpiryDate() {
        return expiryDate;
    }

    public CardStatus getStatus() {
        return status;
    }

    public Long getBalance() {
        return balance;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setExpiryDate(ZonedDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    public void setStatus(CardStatus status) {
        this.status = status;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }
}
