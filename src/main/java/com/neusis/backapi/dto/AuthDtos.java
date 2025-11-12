package com.neusis.backapi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

// 사용자 계정 관련 요청/응답 묶음 컨테이너
@NoArgsConstructor(access = AccessLevel.PRIVATE)     // 기본 생성자를 만들되, 외부에서는 못 쓰게 방지
public class AuthDtos {

    // 회원가입 요청
    @Getter @Setter
    @AllArgsConstructor @NoArgsConstructor
    public static class SignupRequest {
        @Email @NotBlank private String email;
        @NotBlank private String password;     // 평문 입력 → 서버에서 해시 후 저장
        @NotBlank private String nickname;
    }

    // 로그인 요청
    @Getter @Setter
    @AllArgsConstructor @NoArgsConstructor
    public static class LoginRequest {
        @Email @NotBlank private String email;
        @NotBlank private String password;
    }

    // 로그인 응답
    @Getter
    @Builder
    @AllArgsConstructor @NoArgsConstructor
    public static class LoginResponse {
        private Long userId;
        private String email;
        private String nickname;
        private String role;
    }

    // 로그아웃 응답(또는 공통 OK)
    @Getter @AllArgsConstructor
    public static class OkResponse {
        private String message;   // "OK", "DELETED" 등
    }

    // 회원 탈퇴 요청(선택)
    @Getter @Setter
    @AllArgsConstructor @NoArgsConstructor
    public static class DeleteRequest {
        @NotBlank private String password;   // 비밀번호 재입력 시 검증용
    }
}
