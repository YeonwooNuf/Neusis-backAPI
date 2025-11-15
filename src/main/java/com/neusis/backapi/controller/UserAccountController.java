package com.neusis.backapi.controller;

import com.neusis.backapi.dto.AuthDtos;
import com.neusis.backapi.service.UserAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserAccountController {

    private final UserAccountService userAccountService;

    // 회원가입
    public ResponseEntity<Long> signup(@Valid @RequestBody AuthDtos.SignupRequest req) {
        Long userId = userAccountService.signup(req);

        URI location = URI.create("/api/users/" + userId);
        // Location 헤더에 생성된 리소스의 URL을 넣어줌
        return ResponseEntity.created(location).body(userId);
    }
}
