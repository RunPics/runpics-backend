package com.runpics.runpics_backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "RunPics API 명세서",
                description = "러닝 기록 이미지 생성 서비스 RunPics의 API 명세서입니다.",
                version = "v1"))
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI(){
        String jwtSchemeName = "Bearer Authentication"; // SecurityRequirement 이름과 일치해야 함

        // API 요청 헤더에 인증 정보 포함
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwtSchemeName);

        // SecuritySchemes 등록 (JWT Bearer 방식 사용)
        Components components = new Components()
                .addSecuritySchemes(jwtSchemeName, new SecurityScheme()
                        .name(jwtSchemeName)
                        .type(SecurityScheme.Type.HTTP) // HTTP 방식
                        .scheme("bearer")
                        .bearerFormat("JWT")); // 토큰 형식 지정

        return new OpenAPI()
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}