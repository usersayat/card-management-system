package com.card_management_system.repository;

import com.card_management_system.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long>, JpaSpecificationExecutor<Card> {
    Optional<Card> findByNumber(String number);

    Page<Card> findByOwnerUsername(String username, Pageable pageable);

    Optional<Card> findByIdAndOwnerUsername(Long id, String username);
}
