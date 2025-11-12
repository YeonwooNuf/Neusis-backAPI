package com.neusis.backapi.repository;

import com.neusis.backapi.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);    // 이메일 중복 확인
    Optional<User> findByEmail(String email);
}