package com.runpics.runpics_backend.global.jwt;

import com.runpics.runpics_backend.domain.user.entity.User;
import com.runpics.runpics_backend.domain.user.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JwtService {

    @Value("${jwt.secretKey}") // application.yml에서 비밀키 값을 가져옴
    private String secretKey;

    @Value("${jwt.access.expiration}") // application.yml에서 액세스 토큰 만료 시간을 가져옴
    private Long accessTokenExpirationPeriod;

    @Value("${jwt.refresh.expiration}") // application.yml에서 리프레시 토큰 만료 시간을 가져옴
    private Long refreshTokenExpirationPeriod;

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String EMAIL_CLAIM = "email";

    /**
     * AccessToken 생성 메소드
     */
    public String createAccessToken(String email) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(ACCESS_TOKEN_SUBJECT)
                .claim(EMAIL_CLAIM, email) // Payload에 들어갈 정보 (클레임)
                .setExpiration(new Date(now.getTime() + accessTokenExpirationPeriod)) // 만료 시간
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // 서명
                .compact();
    }

    /**
     * RefreshToken 생성 메소드
     */
    public String createRefreshToken() {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(REFRESH_TOKEN_SUBJECT)
                .setExpiration(new Date(now.getTime() + refreshTokenExpirationPeriod))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 서명에 사용할 키 생성
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes());
    }
}