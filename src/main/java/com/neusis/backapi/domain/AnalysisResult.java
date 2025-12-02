package com.neusis.backapi.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalysisResult {   // 분석 결과 테이블
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long resultId;  // 분석 결과 고유 ID (자동 증가)

    @OneToOne(optional=false, fetch= FetchType.LAZY)
    @JoinColumn(name="article_id", nullable=false, unique=true)
    private Article article;    // 기사(원문)랑 1대1 관계

    // LLM 모델이 생성한 요약문
    @Column(columnDefinition = "TEXT")
    private String summary;

    // 감정 분석 결과 (POSITIVE, NEUTRAL, NEGATIVE 등)
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Sentiment sentiment;

    // 주요 키워드 리스트
    @ElementCollection
    @CollectionTable(
            name = "analysis_keywords",
            joinColumns = @JoinColumn(name = "result_id") // resultId 기준으로 연결
    )
    @Column(name = "keyword", length = 100)
    private List<String> keywords;

    // 분석 완료 시간
    private LocalDateTime processedAt;

    // DB에 저장된 시각
    @CreationTimestamp
    private LocalDateTime createdAt;
}