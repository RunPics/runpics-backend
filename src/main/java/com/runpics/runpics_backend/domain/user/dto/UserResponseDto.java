package com.runpics.runpics_backend.domain.user.dto;

import com.runpics.runpics_backend.domain.user.entity.User;
import com.runpics.runpics_backend.domain.user.enums.Provider;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder // 빌더 패턴 사용
public class UserResponseDto {

    private Long userId;
    private String email;
    private String name;
    private Provider provider;

    // User 엔티티를 UserResponseDto로 변환하는 정적 메소드
    public static UserResponseDto from(User user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .name(user.getName())
                .provider(user.getProvider())
                .build();
    }
}