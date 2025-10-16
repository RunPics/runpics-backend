package com.runpics.runpics_backend.domain.user.repository;

import com.runpics.runpics_backend.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 이메일로 사용자를 찾는 커스텀 메소드
    Optional<User> findByEmail(String email);

}