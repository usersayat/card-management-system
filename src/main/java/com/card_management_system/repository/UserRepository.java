package com.card_management_system.repository;

import org.springframework.stereotype.Repository;
import com.card_management_system.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
