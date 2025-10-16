package com.runpics.runpics_backend.domain.user.service;

import com.runpics.runpics_backend.domain.user.entity.User;
import com.runpics.runpics_backend.domain.user.enums.Provider;
import com.runpics.runpics_backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor // final 필드에 대한 생성자를 자동으로 만들어줌
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. 기본 OAuth2UserService를 통해 사용자 정보를 가져옴
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 2. 소셜 플랫폼 정보와 사용자 속성 추출
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google"
        String providerId = oAuth2User.getName(); // 구글의 고유 식별자 (sub)

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        // 3. DB에 사용자가 있는지 확인하고, 없으면 새로 저장 (회원가입)
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createUser(email, name, Provider.valueOf(registrationId.toUpperCase()), providerId));

        // 4. Spring Security가 인식할 수 있도록 OAuth2User 객체 반환
        return oAuth2User;
    }

    private User createUser(String email, String name, Provider provider, String providerId) {
        User newUser = User.builder()
                .email(email)
                .name(name)
                .provider(provider)
                .providerId(providerId)
                .build();
        return userRepository.save(newUser);
    }
}