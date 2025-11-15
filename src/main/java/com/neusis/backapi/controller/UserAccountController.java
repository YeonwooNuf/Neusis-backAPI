package com.neusis.backapi.controller;

import com.neusis.backapi.dto.AuthDtos;
import com.neusis.backapi.dto.UserDto;
import com.neusis.backapi.service.UserAccountService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserAccountController {

    private final UserAccountService userAccountService;

    private static final String LOGIN_USER_KEY = "LOGIN_USER";  // 세션 저장용 태그 이름

    // 회원가입
    @PostMapping("/auth/signup")
    public ResponseEntity<Long> signup(@Valid @RequestBody AuthDtos.SignupRequest req) {
        Long userId = userAccountService.signup(req);

        URI location = URI.create("/api/users/" + userId);
        // Location 헤더에 생성된 리소스의 URL을 넣어줌
        return ResponseEntity.created(location).body(userId);
    }

    // 로그인
    @PostMapping("/auth/login")
    public ResponseEntity<UserDto> login(
            @Valid @RequestBody AuthDtos.LoginRequest req,   // 요청 바디를 JSON으로 받고, DTO에서 검증 조건을 처리
            HttpSession session
    ) {
        // 서비스에서 검증 + UserDto 생성
        UserDto userDto = userAccountService.login(req);

        // 세션에 저장
        session.setAttribute(LOGIN_USER_KEY, userDto);

        // 응답도 UserDto 그대로 반환
        return ResponseEntity.ok(userDto);
    }

    // 로그아웃 (세션 무효화)
    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }
}
