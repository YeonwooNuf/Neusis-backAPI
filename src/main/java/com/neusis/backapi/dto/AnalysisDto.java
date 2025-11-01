package com.neusis.backapi.dto;

import com.neusis.backapi.domain.AnalysisResult;
import com.neusis.backapi.domain.Sentiment;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

// 기사 분석 결과 전달용 Dto
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisDto {

    private Long resultId;              // 분석 결과 고유 ID
    private Long articleId;             // 원문 기사 고유 ID
    private String summary;             // LLM 생성 요약문

    @NotNull
    private Sentiment sentiment;        // 감정 분석 결과

    private List<String> keywords;      // 주요 키워드 리스트
    private Double trendScore;          // 트렌드 점수

    private LocalDateTime processedAt;  // 분석 완료 시각
    private LocalDateTime createdAt;    // DB에 저장된 시각

    // Entity → DTO 변환
    // 엔티티를 그대로 외부 API에 노출하지 않기 위함
    public static AnalysisDto fromEntity(AnalysisResult result) {
        return AnalysisDto.builder()
                .resultId(result.getResultId())
                .articleId(result.getArticle().getArticleId())
                .summary(result.getSummary())
                .sentiment(result.getSentiment())
                .keywords(result.getKeywords())
                .trendScore(result.getTrendScore())
                .processedAt(result.getProcessedAt())
                .createdAt(result.getCreatedAt())
                .build();
    }
}