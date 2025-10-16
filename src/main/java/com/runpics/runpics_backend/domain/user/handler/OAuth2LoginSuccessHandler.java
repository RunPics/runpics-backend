package com.runpics.runpics_backend.domain.user.handler;

import com.runpics.runpics_backend.global.jwt.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler; // 1. SimpleUrlAuthenticationSuccessHandler로 변경
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder; // 2. UriComponentsBuilder import

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler { // 3. 상속 클래스 변경

    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        // 1. 인증된 사용자 정보 가져오기
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        // 2. JWT 토큰 생성
        String accessToken = jwtService.createAccessToken(email);
        String refreshToken = jwtService.createRefreshToken();

        System.out.println("로그인 성공! 이메일: " + email); // 서버 로그 확인용
        System.out.println("발급된 액세스 토큰: " + accessToken);

        // 3. 토큰을 담아 리디렉션할 URL 생성
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/login-success") // 프론트엔드 주소
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build()
                .encode(StandardCharsets.UTF_8)
                .toUriString();

        // 4. 생성된 URL로 리디렉션
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}