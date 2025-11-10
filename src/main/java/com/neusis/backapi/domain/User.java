package com.neusis.backapi.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter @Setter
@Table(name = "users")
@ToString(exclude = "passwordHash")     // 엔티티 로그 찍을 때 비밀번호 해시 노출 방지용
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    // 로그인에 사용하는 이메일(이메일 형식 검증)
    @Email @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String passwordHash;

    // 사용자 프로필 닉네임
    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    @Builder.Default
    private String role = "USER";
}
