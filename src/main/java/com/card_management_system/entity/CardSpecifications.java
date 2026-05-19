package com.card_management_system.entity;

import com.card_management_system.common.CardStatus;
import org.springframework.data.jpa.domain.Specification;

public class CardSpecifications {

    public static Specification<Card> hasUsername(String username) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("owner").get("username"), username);
    }

    public static Specification<Card> hasStatus(CardStatus status) {
        return (root, query, criteriaBuilder) -> status == null ? null :
                    criteriaBuilder.equal(root.get("status"), status);
    }

    public static Specification<Card> minBalance(Long minBalance) {
        return (root, query, criteriaBuilder) -> minBalance == null ? null :
                criteriaBuilder.greaterThanOrEqualTo(root.get("balance"), minBalance);
    }
}
