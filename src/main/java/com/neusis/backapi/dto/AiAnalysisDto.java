package com.neusis.backapi.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class AiAnalysisDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private Long articleId;
        private String title;
        private String content;
        private String category;
        private LocalDateTime publishedAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private String summary;             // 요약문
        private String sentiment;           // "POSITIVE" | "NEGATIVE" | "NEUTRAL"
        private List<String> keywords;      // 키워드
        private Double trendScore;          // FastAPI가 계산해서 줄 수도 있음
    }
}