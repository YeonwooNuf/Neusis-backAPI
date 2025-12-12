package com.neusis.backapi.dto;

import com.neusis.backapi.domain.AnalysisResult;
import com.neusis.backapi.domain.Article;
import com.neusis.backapi.domain.Category;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleListDto {       // 목록 표시용 Dto
    private Long articleId;
    private String title;
    private String contentPreview;  // 본문 미리보기
    private String source;
    private LocalDateTime publishedAt;
    private Category category;
    private String summary; // AnalysisResult.summary (있으면)

    // 사용자별 플래그
    private Boolean isRead;
    private Boolean isLiked;

    public static ArticleListDto fromEntity(Article a, boolean isRead, boolean isLiked) {
        AnalysisResult ar = a.getAnalysisResult();

        return ArticleListDto.builder()
                .articleId(a.getArticleId())
                .title(a.getTitle())
                .contentPreview(
                        a.getContent() != null
                                ? a.getContent().substring(0, Math.min(40, a.getContent().length()))
                                : null
                )
                .source(a.getSource())
                .publishedAt(a.getPublishedAt())
                .category(a.getCategory())
                .summary(ar != null ? ar.getSummary() : null)   // 요약 정보 존재 시 반환
                .isRead(isRead)
                .isLiked(isLiked)
                .build();
    }
}
