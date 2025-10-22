package com.runpics.runpics_backend.global.jwt.filter;

import com.runpics.runpics_backend.domain.user.entity.User;
import com.runpics.runpics_backend.domain.user.repository.UserRepository;
import com.runpics.runpics_backend.global.jwt.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter { // 각 요청마다 딱 한 번 실행되는 필터

    private final JwtService jwtService;
    private final UserRepository userRepository;

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper(); // 권한 매핑 (지금은 기본 사용)

    private static final String NO_CHECK_URL = "/login"; // '/login' 경로는 필터 검사 안 함 (로그인 자체는 토큰 검사 X)

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // '/login' 요청은 이 필터를 그냥 통과시킴
        if (request.getRequestURI().equals(NO_CHECK_URL)) {
            filterChain.doFilter(request, response);
            return; // 필터 로직 실행 안 함
        }

        // 1. 헤더에서 RefreshToken 추출 (재발급 요청 시 사용)
        String refreshToken = jwtService.extractRefreshToken(request)
                .filter(jwtService::isTokenValid)
                .orElse(null);

        // 2. RefreshToken이 있으면 AccessToken 재발급 처리 (지금은 주석 처리, 나중에 구현)
        if (refreshToken != null) {
            // jwtService.checkRefreshTokenAndReIssueAccessToken(response, refreshToken);
            // return; // 재발급 후 필터 종료
        }

        // 3. RefreshToken이 없거나 유효하지 않으면 AccessToken 검사 및 인증 처리 시도
        checkAccessTokenAndAuthentication(request, response, filterChain);
    }

    /**
     * AccessToken 검증 및 인증 처리 메소드
     */
    public void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        log.debug("checkAccessTokenAndAuthentication() 호출");
        // 헤더에서 AccessToken 추출 -> 유효성 검사 -> 이메일 추출 -> DB에서 사용자 조회 -> 인증 정보 저장
        jwtService.extractAccessToken(request)
                .filter(jwtService::isTokenValid)
                .flatMap(accessToken -> jwtService.extractEmail(accessToken))
                .flatMap(userRepository::findByEmail)
                .ifPresent(this::saveAuthentication); // 사용자가 있으면 인증 정보 저장

        filterChain.doFilter(request, response); // 다음 필터로 요청 전달
    }

    /**
     * 인증 정보를 SecurityContext에 저장하는 메소드
     */
    public void saveAuthentication(User myUser) {
        // UserDetails 구현체를 직접 만들거나, Spring Security의 User 사용 가능
        UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(myUser.getEmail())
                .password("") // 소셜 로그인이므로 비밀번호 없음
                .roles("USER") // 기본 역할 부여 (추후 DB에서 가져오도록 수정 가능)
                .build();

        // 인증 객체 생성
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null,
                        authoritiesMapper.mapAuthorities(userDetails.getAuthorities()));

        // SecurityContext에 인증 객체 저장 -> 이제 이 요청은 인증된 사용자의 요청으로 간주됨
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}