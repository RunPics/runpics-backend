package com.runpics.runpics_backend.domain.user.service;

import com.runpics.runpics_backend.domain.user.dto.UserResponseDto;
import com.runpics.runpics_backend.domain.user.entity.User;
import com.runpics.runpics_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true) // 읽기 전용 트랜잭션 (성능 향상)
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 현재 로그인한 사용자의 정보를 조회
     */
    public UserResponseDto getMyInfo() {
        // SecurityContext에서 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName(); // JWT 토큰 생성 시 넣었던 이메일

        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userEmail));

        // User 엔티티를 UserResponseDto로 변환하여 반환
        return UserResponseDto.from(user);
    }
}