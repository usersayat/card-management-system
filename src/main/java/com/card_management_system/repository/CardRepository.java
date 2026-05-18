package com.card_management_system.repository;

import com.card_management_system.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    Card findByNumber(String number);

    List<Card> findByOwnerUsername(String username);

    Card findByIdAndOwnerUsername(Long id, String username);
}
