package com.neusis.backapi.service;

import com.neusis.backapi.client.AiClient;
import com.neusis.backapi.domain.Article;
import com.neusis.backapi.domain.IngestStatus;
import com.neusis.backapi.domain.Sentiment;
import com.neusis.backapi.dto.AiAnalysisDto;
import com.neusis.backapi.dto.AnalysisDto;
import com.neusis.backapi.dto.ArticleDto;
import com.neusis.backapi.exception.NotFoundException;
import com.neusis.backapi.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

// final 이 붙은 필드 자동으로 생성자 파라미터로 변환
@RequiredArgsConstructor

// 이 클래스가 실행되는 동안 DB 작업을 하나의 트랜잭션 단위로 묶음
// 기본은 읽기 전용, 쓰기 메서드에만 별도 @Transactional
@Transactional(readOnly = true)

// Article & Analysis 비즈니스 로직
@Service
public class ArticleService {
    private final ArticleRepository articleRepo;    // 기사 원문 repo
    private final AnalysisService analysisService;
    private final AiClient aiClient;
    private final TrendService trendService;

    private Article getArticleOrThrow(Long articleId) {
        return articleRepo.findById(articleId)
                .orElseThrow(()-> new NotFoundException("다음 기사를 찾을 수 없음 : " + articleId));
    }

    @Transactional
    public ArticleDto create(ArticleDto req) {  // req = 요청(request) 데이터를 담고 있는 DTO 객체

        // url 기반 기사 원문 중복 저장 방지
        if (articleRepo.existsByUrl(req.getUrl())) {
            throw new IllegalArgumentException("중복된 URL : " + req.getUrl());
        }

        // DTO → Entity 변환
        // 요청 데이터를 엔티티로 만들어서 DB에 저장
        Article a = Article.builder()
                .title(req.getTitle())
                .content(req.getContent())
                .author(req.getAuthor())
                .source(req.getSource())
                .url(req.getUrl())
                .imageUrl(req.getImageUrl())
                .category(req.getCategory())
                .publishedAt(req.getPublishedAt())
                .viewCount(req.getViewCount())
                .ingestStatus(req.getIngestStatus() != null ? req.getIngestStatus() : IngestStatus.PENDING)
                .build();

        return ArticleDto.fromEntity(articleRepo.save(a));
    }

    // 기사 원문 단건 조회
    // 분석 결과가 있으면 DTO.analysis 에 포함
    public ArticleDto getByArticleId(Long articleId) {
        Article article = getArticleOrThrow(articleId);

        // 1) 기본 DTO 생성 (analysis 포함)
        ArticleDto dto = ArticleDto.fromEntity(article);

        // 2) 분석 완료 상태라면 trendScore 계산 후 DTO에 반영
        if (article.getIngestStatus() == IngestStatus.ANALYZED && dto.getAnalysis() != null) {
            double trendScore = trendService.calculateTrendScore(article, dto.getAnalysis());
            dto.setTrendScore(trendScore);
        }

        return dto;
    }

    // 기사 AI 분석 트리거
    // FastAPI에 기사 원문 전달
    // 분석 결과 → AnalysisDto 로 변환
    // AnalysisService.upsertAnalysis() 로 저장
    // 트렌드 점수 계산 및 반영
    // ingestStatus(PENDING → ANALYZED/FAILED) 관리
    // 최종 ArticleDto 반환
    @Transactional
    public ArticleDto analyzeArticle(Long articleId) {

        // 1) 기사 조회
        Article article = articleRepo.findById(articleId)
                .orElseThrow(() -> new NotFoundException("Article not found: " + articleId));

        if (article.getIngestStatus() == IngestStatus.ANALYZED) {
            return getByArticleId(articleId);
        }

        // 2) PENDING 상태로 변경
        article.setIngestStatus(IngestStatus.PENDING);

        try {
            // 3) FastAPI 요청 DTO 구성
            AiAnalysisDto.Request request = AiAnalysisDto.Request.builder()
                    .articleId(article.getArticleId())
                    .title(article.getTitle())
                    .content(article.getContent())
                    .category(article.getCategory().name())
                    .publishedAt(article.getPublishedAt())
                    .build();

            // 4) FastAPI 호출
            AiAnalysisDto.Response response = aiClient.analyze(request);

            // 5) 문자열 → Enum 매핑
            Sentiment sentiment = mapSentiment(response.getSentiment());

            // 6) DB에 저장할 분석 결과 DTO 생성 (trendScore 없음)
            AnalysisDto analysisReq = AnalysisDto.builder()
                    .articleId(articleId)
                    .summary(response.getSummary())
                    .sentiment(sentiment)
                    .keywords(response.getKeywords())
                    .processedAt(LocalDateTime.now())
                    .build();

            // 7) 키워드 기반 트렌드 메트릭 업데이트 (trendScore 계산 X)
            trendService.updateKeywordTrends(article, analysisReq);

            // 8) 분석 결과 저장 (INGEST_STATUS = ANALYZED 처리 포함)
            analysisService.upsertAnalysis(articleId, analysisReq);

            // 9) 조회 시 trendScore를 계산해 ArticleDto에 담아 반환
            ArticleDto dto = getByArticleId(articleId);

            // 조회 시점에 trendScore 동적 계산
            double trendScore = trendService.calculateTrendScore(article, dto.getAnalysis());
            dto.setTrendScore(trendScore);

            return dto;

        } catch (Exception ex) {
            article.setIngestStatus(IngestStatus.FAILED);
            throw ex;
        }
    }

    private Sentiment mapSentiment(String raw) {
        if (raw == null) return Sentiment.NEUTRAL;
        try {
            return Sentiment.valueOf(raw.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Sentiment.NEUTRAL;
        }
    }

    // Pending 상태의 기사를 Failed 나 ANALYZED 로 변경
    @Transactional
    public ArticleDto updateStatus(Long articleId, IngestStatus status) {
        Article a = getArticleOrThrow(articleId);
        a.setIngestStatus(status);
        return ArticleDto.fromEntity(a);
    }

    // 기사 삭제 (원문 삭제 시 분석 결과도 Cascade로 함께 삭제)
    @Transactional
    public void delete(Long articleId) {
        Article a = getArticleOrThrow(articleId);
        articleRepo.delete(a);
    }
}
