package com.neusis.backapi.dto;

import com.neusis.backapi.domain.AnalysisResult;
import com.neusis.backapi.domain.Article;
import com.neusis.backapi.domain.Category;
import com.neusis.backapi.domain.IngestStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArticleDto {

    private Long articleId;             // 기사 고유 ID

    @NotBlank
    private String title;               // 기사 제목

    private String content;             // 기사 본문
    private String source;              // 언론사

    @NotBlank
    private String url;                 // 기사 원문 url

    @NotNull
    private Category category;          // 기사 카테고리

    private LocalDateTime publishedAt;  // 기사 발행 시각
    private LocalDateTime createdAt;    // 최초 생성 시각
    private LocalDateTime updatedAt;    // 최종 수정 시각

    private IngestStatus ingestStatus;  // 분석 상태

    private AnalysisDto analysis;       // 분석 결과 (분석 여부에 따라 없을 수도 있음)

    // Entity -> Dto 변환 메소드
    // 엔티티를 그대로 외부 API에 노출하지 않기 위함
    public static ArticleDto fromEntity(Article article) {
        AnalysisResult analysisResult = article.getAnalysisResult();
        return ArticleDto.builder()
                .articleId(article.getArticleId())
                .title(article.getTitle())
                .content(article.getContent())
                .source(article.getSource())
                .url(article.getUrl())
                .category(article.getCategory())
                .publishedAt(article.getPublishedAt())
                .ingestStatus(article.getIngestStatus())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .analysis(analysisResult != null ? AnalysisDto.fromEntity(analysisResult) : null)
                .build();
    }
}
