package com.neusis.backapi.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "article")   // 기사 원문 테이블
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

    private String author;          // 기자 이름

    private String source;          // 언론사

    @Column(nullable = false, unique = true)
    private String url;             // 원문 URL  (중복 방지 unique = true)

    @Column(name = "image_url", nullable = true)
    private String imageUrl;   // 기사 대표 이미지 URL (optional)

    @Enumerated(EnumType.STRING) // 문자열로 저장
    @Column(nullable = false, length = 20)
    private Category category; // 기사 주제 (Enum으로 관리)

    private LocalDateTime publishedAt; // 기사 발행 시각

    @CreationTimestamp
    private LocalDateTime createdAt; // 생성 시각

    @UpdateTimestamp
    private LocalDateTime updatedAt; // 수정 시각

    @Column(nullable = false, columnDefinition = "bigint default 0")
    private long viewCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=16)
    private IngestStatus ingestStatus = IngestStatus.PENDING;

    @OneToOne(mappedBy = "article", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private AnalysisResult analysisResult;
}
