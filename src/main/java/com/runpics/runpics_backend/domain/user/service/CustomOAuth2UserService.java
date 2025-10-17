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
        String registrationId = userRequest.getClientRegistration().getRegistrationId(); // "google" 또는 "kakao"

        // 2. 소셜 플랫폼에 따라 사용자 정보를 다르게 파싱
        String email;
        String name;
        String providerId;

        if (registrationId.equals("google")) {
            // 구글의 경우
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
            providerId = oAuth2User.getName(); // 구글의 고유 식별자 (sub)

        } else if (registrationId.equals("kakao")) {
            // 카카오의 경우
            Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

            email = (String) kakaoAccount.get("email");
            name = (String) profile.get("nickname");
            providerId = oAuth2User.getAttribute("id").toString();

        } else {
            // 지원하지 않는 소셜 플랫폼의 경우 예외 발생
            throw new OAuth2AuthenticationException("Unsupported provider: " + registrationId);
        }

        // 3. DB에 사용자가 있는지 확인하고, 없으면 새로 저장 (자동 회원가입)
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> createUser(email, name, Provider.valueOf(registrationId.toUpperCase()), providerId));

        // 4. Spring Security가 인식할 수 있도록 OAuth2User 객체 반환
        // (지금은 기본 객체를 반환하지만, 나중에 우리만의 PrincipalDetails 객체로 바꿀 예정)
        return oAuth2User;
    }

    private User createUser(String email, String name, Provider provider, String providerId) {
        // 새로운 User 객체를 생성
        User newUser = User.builder()
                .email(email)
                .name(name)
                .provider(provider)
                .providerId(providerId)
                .build();
        // DB에 저장
        return userRepository.save(newUser);
    }
}
