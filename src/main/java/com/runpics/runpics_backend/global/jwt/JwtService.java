package com.runpics.runpics_backend.global.jwt;

import io.jsonwebtoken.*; // Jwts, Claims 등 추가
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException; // SignatureException 추가
import jakarta.servlet.http.HttpServletRequest; // HttpServletRequest 추가
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter; // Getter 추가
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // Slf4j 추가 (로그용)
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey; // Key 대신 SecretKey 사용 (더 명확)
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Getter // 외부에서 설정값 읽기 위해 추가
@Slf4j // 로그 출력을 위해 추가
public class JwtService {

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpirationPeriod;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpirationPeriod;

    // application.yml 에 정의할 헤더 이름
    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;


    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String EMAIL_CLAIM = "email";
    private static final String BEARER_PREFIX = "Bearer "; // 토큰 식별자

    private SecretKey key; // 서명 키

    // 객체 생성 시 application.yml 값으로 키 초기화
    @jakarta.annotation.PostConstruct // 의존성 주입 후 초기화 수행
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }


    /**
     * AccessToken 생성
     */
    public String createAccessToken(String email) {
        Date now = new Date();
        return Jwts.builder()
                .subject(ACCESS_TOKEN_SUBJECT) // 토큰 제목
                .claim(EMAIL_CLAIM, email) // 사용자 이메일 클레임
                .expiration(new Date(now.getTime() + accessTokenExpirationPeriod)) // 만료 시간
                .signWith(key, Jwts.SIG.HS256) // HS256 알고리즘, 키로 서명
                .compact();
    }

    /**
     * RefreshToken 생성 (사용자 정보 미포함)
     */
    public String createRefreshToken() {
        Date now = new Date();
        return Jwts.builder()
                .subject(REFRESH_TOKEN_SUBJECT)
                .expiration(new Date(now.getTime() + refreshTokenExpirationPeriod))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    /**
     * AccessToken 헤더에 전송
     */
    public void sendAccessToken(HttpServletResponse response, String accessToken) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(accessHeader, BEARER_PREFIX + accessToken); // Bearer 추가
        log.info("Access Token 헤더 설정 완료: {}", accessToken);
    }

    /**
     * AccessToken + RefreshToken 헤더에 전송 (로그인 시)
     */
    public void sendAccessAndRefreshToken(HttpServletResponse response, String accessToken, String refreshToken) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader(accessHeader, BEARER_PREFIX + accessToken);
        response.setHeader(refreshHeader, BEARER_PREFIX + refreshToken); // Bearer 추가
        log.info("Access Token, Refresh Token 헤더 설정 완료");
    }

    /**
     * 헤더에서 AccessToken 추출
     * Bearer 접두사 제거
     */
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(accessToken -> accessToken.startsWith(BEARER_PREFIX)) // Bearer 로 시작하는지 확인
                .map(accessToken -> accessToken.replace(BEARER_PREFIX, "")); // Bearer 제거
    }

    /**
     * 헤더에서 RefreshToken 추출
     * Bearer 접두사 제거
     */
    public Optional<String> extractRefreshToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER_PREFIX))
                .map(refreshToken -> refreshToken.replace(BEARER_PREFIX, ""));
    }

    /**
     * AccessToken에서 Email 추출
     */
    public Optional<String> extractEmail(String accessToken) {
        try {
            // 토큰 복호화 및 클레임 추출
            Claims claims = Jwts.parser()
                    .verifyWith(key) // 키로 검증
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
            return Optional.ofNullable(claims.get(EMAIL_CLAIM, String.class));
        } catch (JwtException e) { // 모든 JWT 관련 예외 처리
            log.error("액세스 토큰이 유효하지 않습니다. {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * 토큰 유효성 검사 (서명 및 만료 시간)
     */
    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException e) {
            log.error("유효하지 않은 JWT 서명입니다.");
        } catch (MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }
}