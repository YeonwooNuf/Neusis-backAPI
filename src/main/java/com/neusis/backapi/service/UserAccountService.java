package com.neusis.backapi.service;

import com.neusis.backapi.domain.User;
import com.neusis.backapi.dto.AuthDtos;
import com.neusis.backapi.dto.UserDto;
import com.neusis.backapi.exception.NotFoundException;
import com.neusis.backapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAccountService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    // 공통 예외처리
    private User getUserOrThrow(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }

    // 회원가입
    // 기본 role : USER
    @Transactional
    public Long signup(AuthDtos.SignupRequest req) {
        // 이메일 중복 방지
        if(userRepo.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }

        User user = User.builder()
                .email(req.getEmail())
                .passwordHash(passwordEncoder.encode(req.getPassword()))    // 비밀번호 해시화
                .nickname(req.getNickname())
                .role("USER")   // 기본값
                .build();

        return userRepo.save(user).getUserId();
    }

    // 로그인
    public AuthDtos.LoginResponse login(AuthDtos.LoginRequest req) {

        // 아이디(이메일) 검증
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("아이디 혹은 비밀번호가 옳지 않습니다."));

        // 비밀번호 검증
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("아이디 혹은 비밀번호가 옳지 않습니다.");
        }

        // 성공 시 userId 등 최소 정보 반환
        return AuthDtos.LoginResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
    }

    // 로그아웃
    @Transactional
    public void logout(Long userId) {
    }

    // 내 정보 조회
    public UserDto getMyInfo(Long userId) {
        User user = getUserOrThrow(userId);
        return UserDto.fromEntity(user);
    }
}
