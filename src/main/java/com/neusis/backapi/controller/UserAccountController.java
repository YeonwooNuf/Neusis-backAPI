package com.neusis.backapi.controller;

import com.neusis.backapi.dto.AuthDtos;
import com.neusis.backapi.service.UserAccountService;
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
    public ResponseEntity<AuthDtos.LoginResponse> login(
            @Valid @RequestBody AuthDtos.LoginRequest req   // 요청 바디를 JSON으로 받고, DTO에서 검증 조건을 처리
    ) {
        AuthDtos.LoginResponse resp = userAccountService.login(req);
        return ResponseEntity.ok(resp);
    }

    // 로그아웃 (토큰/세션 도입 전이므로 일단 형식만)
    public ResponseEntity<Void> logout(@RequestParam Long userId) {
            userAccountService.logout(userId);
            return ResponseEntity.noContent().build();
    }
}
