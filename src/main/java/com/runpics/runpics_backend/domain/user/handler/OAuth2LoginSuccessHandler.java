package com.runpics.runpics_backend.domain.user.handler;

import com.runpics.runpics_backend.global.jwt.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 1. 소셜 플랫폼에 따라 이메일을 다르게 추출
        String email = getEmailFromOAuth2User(oAuth2User, authentication);

        // 2. JWT 토큰 생성
        String accessToken = jwtService.createAccessToken(email);
        String refreshToken = jwtService.createRefreshToken();

        System.out.println("로그인 성공! 이메일: " + email);
        System.out.println("발급된 액세스 토큰: " + accessToken);
        System.out.println("발급된 리프레시 토큰: " + refreshToken);

        // 3. 토큰을 담아 리디렉션할 URL 생성
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/login-success")
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        // 4. 생성된 URL로 리디렉션
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private String getEmailFromOAuth2User(OAuth2User oAuth2User, Authentication authentication) {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();

        if (registrationId.equals("google")) {
            return oAuth2User.getAttribute("email");
        } else if (registrationId.equals("kakao")) {
            Map<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");
            return (String) kakaoAccount.get("email");
        }

        // 다른 소셜 로그인을 추가할 경우 여기에 로직 추가
        return null; // 또는 예외 처리
    }
}
