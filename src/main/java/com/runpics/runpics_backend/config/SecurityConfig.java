package com.runpics.runpics_backend.config;

import com.runpics.runpics_backend.domain.user.handler.OAuth2LoginSuccessHandler;
import com.runpics.runpics_backend.domain.user.repository.UserRepository;
import com.runpics.runpics_backend.domain.user.service.CustomOAuth2UserService;
import com.runpics.runpics_backend.global.jwt.JwtService;
import com.runpics.runpics_backend.global.jwt.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration // 이 클래스가 설정 파일임을 스프링에게 알려줌
@EnableWebSecurity // 스프링 시큐리티를 활성화
@RequiredArgsConstructor // final 필드 주입을 위해 추가
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final JwtService jwtService; // JwtService 주입
    private final UserRepository userRepository; // UserRepository 주입 (필터 생성 시 필요)

    @Bean // 이 메소드가 반환하는 객체를 스프링이 관리하도록 등록
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // CSRF, Form Login, HTTP Basic 인증 비활성화
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(formLogin -> formLogin.disable())
                .httpBasic(httpBasic -> httpBasic.disable());

        // 세션을 사용하지 않도록 설정 (JWT 사용을 위함)
        http
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // OAuth2 소셜 로그인을 기본 설정으로 사용
        http
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(customOAuth2UserService)) // 사용자 정보 처리 담당자 등록
                        .successHandler(oAuth2LoginSuccessHandler) // 로그인 성공 후 처리 담당자 등록
                );


        // 요청 인가 규칙 설정
        http
                .authorizeHttpRequests(auth -> auth
                        // ✅ '/test/login' 경로도 인증이 필요하도록 추가!
                        .requestMatchers("/api/**", "/test/login").authenticated()
                        // 그 외 모든 요청은 일단 허용
                        .anyRequest().permitAll());

        // ✅ JWT 인증 필터를 UsernamePasswordAuthenticationFilter 앞에 추가
        http.addFilterBefore(new JwtAuthenticationFilter(jwtService, userRepository), UsernamePasswordAuthenticationFilter.class); // ✅ 오타 수정


        return http.build();
    }
}

