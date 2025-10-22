package com.runpics.runpics_backend.domain.user.controller;

import com.runpics.runpics_backend.domain.user.dto.UserResponseDto;
import com.runpics.runpics_backend.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation; // Swagger 어노테이션
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement; // 보안 요구사항 어노테이션
import io.swagger.v3.oas.annotations.tags.Tag; // 태그 어노테이션
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User API", description = "사용자 관련 API") // API 그룹 이름 설정
@RestController
@RequestMapping("/api/v1/users") // 이 컨트롤러의 모든 API는 '/api/v1/users' 로 시작
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 정보 조회", description = "현재 로그인된 사용자의 정보를 조회합니다.") // API 설명
    @ApiResponse(responseCode = "200", description = "조회 성공",
            content = @Content(schema = @Schema(implementation = UserResponseDto.class))) // 성공 응답 명시
    @SecurityRequirement(name = "Bearer Authentication") // 이 API는 JWT 인증이 필요함을 명시
    @GetMapping("/me") // GET /api/v1/users/me
    public ResponseEntity<UserResponseDto> getMyInfo() {
        UserResponseDto myInfo = userService.getMyInfo();
        return ResponseEntity.ok(myInfo); // 200 OK 응답과 함께 사용자 정보 반환
    }
}