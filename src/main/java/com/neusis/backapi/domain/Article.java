package com.neusis.backapi.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "article")   // 분석 결과 테이블
@NoArgsConstructor @AllArgsConstructor
@Builder
@Getter @Setter
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long articleId; // 기사 고유 ID (자동 증가)

    @Column(nullable = false)
    private String title; // 기사 제목

    @Column(columnDefinition = "TEXT")
    private String content; // 기사 본문 내용

    private String source;          // 언론사

    @Column(nullable = false)
    private String url;             // 원문 URL  (중복/유니크 제약은 나중에)

    @Enumerated(EnumType.STRING) // 문자열로 저장
    @Column(nullable = false, length = 20)
    private Category category; // 기사 주제 (Enum으로 관리)

    private LocalDateTime publishedAt; // 기사 발행 시각

    @CreationTimestamp
    private LocalDateTime createdAt; // 생성 시각

    @UpdateTimestamp
    private LocalDateTime updatedAt; // 수정 시각

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=16)
    private IngestStatus ingestStatus = IngestStatus.PENDING;

    @OneToOne(mappedBy = "article", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private AnalysisResult analysisResult;
}
