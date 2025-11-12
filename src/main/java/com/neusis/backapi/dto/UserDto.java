package com.neusis.backapi.dto;

import com.neusis.backapi.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long userId;
    private String email;
    private String nickname;
    private String role;

    public static UserDto fromEntity(User user) {
        return UserDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .build();
    }
}
