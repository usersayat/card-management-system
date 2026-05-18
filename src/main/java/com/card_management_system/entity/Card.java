package com.card_management_system.entity;

import com.card_management_system.common.CardStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.ZonedDateTime;

@Entity
@Table(name = "cards")
public class Card {
    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String number;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User owner;

    private ZonedDateTime expiryDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private CardStatus status;
    private Long balance;

    @CreationTimestamp
    private ZonedDateTime createdAt;
    @UpdateTimestamp
    private ZonedDateTime updatedAt;


    public Long getId() {
        return id;
    }

    public String getNumber() {
        return number;
    }

    public User getOwner() {
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

    public ZonedDateTime getCreatedAt() {
        return createdAt;
    }

    public ZonedDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setOwner(User owner) {
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

    public void setUpdatedAt(ZonedDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Card card))
            return false;
        return id != null && id.equals(card.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
