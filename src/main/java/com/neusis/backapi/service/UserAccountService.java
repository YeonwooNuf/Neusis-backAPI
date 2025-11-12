package com.neusis.backapi.service;

import com.neusis.backapi.domain.User;
import com.neusis.backapi.dto.AuthDtos;
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
}
