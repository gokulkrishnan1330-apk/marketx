package com.marketx.marketx;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<user, Long> {

    Optional<user> findByEmail(String email);

    Optional<user> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);
}