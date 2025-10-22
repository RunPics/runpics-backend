package com.runpics.runpics_backend.config;

import com.runpics.runpics_backend.domain.user.handler.OAuth2LoginSuccessHandler;
import com.runpics.runpics_backend.domain.user.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import com.runpics.runpics_backend.global.jwt.JwtService; // JwtService import
import com.runpics.runpics_backend.global.jwt.filter.JwtAuthenticationFilter; // JwtAuthenticationFilter import
import com.runpics.runpics_backend.domain.user.repository.UserRepository; // UserRepository import (필터에서 사용)
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // 필터 순서 지정을 위해 import
import org.springframework.security.config.Customizer; // Customizer import

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final JwtService jwtService; // JwtService 주입
    private final UserRepository userRepository; // UserRepository 주입 (필터 생성 시 필요)

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .formLogin(formLogin -> formLogin.disable())
                .httpBasic(httpBasic -> httpBasic.disable());

        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http
                .oauth2Login(Customizer.withDefaults()); // 기본 설정 사용 명시

        // 요청 인가 규칙 수정 (API 경로는 인증 필요)
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").authenticated() // '/api/'로 시작하는 모든 경로는 인증 필요
                        .anyRequest().permitAll()); // 그 외 나머지 경로는 모두 허용

        // ✅ JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
        http.addFilterBefore(new JwtAuthenticationFilter(jwtService, userRepository), UsernamePasswordAuthenticationFilter.class);


        return http.build();
    }
}