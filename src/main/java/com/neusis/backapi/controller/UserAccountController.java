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

    // 세션 정보 가져오기 및 공통 예외처리 메소드
    private UserDto getLoginUserOrThrow(HttpSession session) {
        // 세션 정보 가져오기
        UserDto sessionUser = (UserDto) session.getAttribute(LOGIN_USER_KEY);
        if (sessionUser == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "로그인이 필요합니다."
            );
        }
        return sessionUser;
    }

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

    // 내 정보 조회
    @GetMapping("/users/me")
    public ResponseEntity<UserDto> getMyInfo(HttpSession session) {

        UserDto sessionUser = getLoginUserOrThrow(session);

        // 세션에 저장되어있는 userId 가져오기
        UserDto dto = userAccountService.getMyInfo(sessionUser.getUserId());
        return ResponseEntity.ok(dto);
    }

    @DeleteMapping("/users/me")
    public ResponseEntity<Void> deleteUser(
            HttpSession session,
            @RequestBody(required = false) AuthDtos.DeleteRequest req
    ) {
        UserDto sessionUser = getLoginUserOrThrow(session);


        // 로그인 된 상태면 → 그 안의 password 사용
        // 비로그인 상태에서 탈퇴 요청 → 비밀번호 검증
        String rawPassword = (req != null) ? req.getPassword() : null;
        userAccountService.deleteMe(sessionUser.getUserId(), rawPassword);

        // 세션 무효화
        session.invalidate();
        return ResponseEntity.noContent().build();
    }
}
