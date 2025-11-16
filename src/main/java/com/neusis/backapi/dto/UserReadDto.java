package com.neusis.backapi.dto;

import com.neusis.backapi.domain.UserRead;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserReadDto {

    private Long readId;
    private Long articleId;

    private LocalDate readDate;

    // 최근 조회 기사 목록 구현용
    private String title;
    private String category;

    // Entity -> Dto 변환
    public static UserReadDto fromEntity(UserRead ur) {
        return UserReadDto.builder()
                .readId(ur.getReadId())
                .articleId(ur.getArticle().getArticleId())
                .readDate(ur.getReadDate())
                .title(ur.getArticle().getTitle())
                .category(ur.getArticle().getCategory().name())
                .build();
    }
}
