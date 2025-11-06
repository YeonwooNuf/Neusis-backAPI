package com.neusis.backapi.service;

import com.neusis.backapi.domain.*;
import com.neusis.backapi.dto.AnalysisDto;
import com.neusis.backapi.dto.ArticleDto;
import com.neusis.backapi.exception.NotFoundException;
import com.neusis.backapi.repository.AnalysisResultRepository;
import com.neusis.backapi.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
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
    private final AnalysisResultRepository analysisRepo;   // 분석 결과 repo

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
                .source(req.getSource())
                .url(req.getUrl())
                .category(req.getCategory())
                .publishedAt(req.getPublishedAt())
                .ingestStatus(req.getIngestStatus() != null ? req.getIngestStatus() : IngestStatus.PENDING)
                .build();

        return ArticleDto.fromEntity(articleRepo.save(a));
    }

    // 기사 원문 단건 조회
    // 분석 결과가 있으면 DTO.analysis 에 포함
    public ArticleDto getByArticleId(Long articleId) {
        Article a = getArticleOrThrow(articleId);
        return ArticleDto.fromEntity(a);
    }

    // 기사 목록 (필터 & 페이지)
    // status / category+기간 / 기간 / 전체
    // 최신(publishedAt, createdAt) 우선 정렬[DESC]
    public Page<ArticleDto> list(Integer page, Integer size, Category category,
                                 LocalDateTime from, LocalDateTime to, IngestStatus status) {

        // 페이징 및 정렬 기본값 설정
        // page, size 가 null 이면 기본 0페이지, 20개
        // 최신 순 정렬 : publishedAt 우선, 같으면 createdAt
        Pageable pageable = PageRequest.of(
                page == null ? 0 : page,
                size == null ? 20 : size,
                Sort.by(Sort.Direction.DESC, "publishedAt", "createdAt")
        );

        Page<Article> result;

        // 상태 기반 조회
        // 분석 파이프라인 모니터링/재처리 등에서 사용
        if (status != null) {
            result = articleRepo.findByIngestStatus(status, pageable);
        // 카테고리 + 기간 동시 필터
        // 특정 분야의 기간별 트렌드/집계용 조회
        } else if (category != null && from != null && to != null) {
            result = articleRepo.findByCategoryAndPublishedAtBetween(category, from, to, pageable);
        // 기간만 필터
        // 전 카테고리 대상 기간 범위 조회
        } else if (from != null && to != null) {
            result = articleRepo.findByPublishedAtBetween(from, to, pageable);
        // 필터 없음 → 전체 조회
        // 기본 최신순 페이지네이션
        } else {
            result = articleRepo.findAll(pageable);
        }
        // 엔티티 → DTO 변환
        return result.map(ArticleDto::fromEntity);
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

    // 분석 결과 Upsert(Update + Insert)
    @Transactional
    public AnalysisDto upsertAnalysis(Long articleId, AnalysisDto req) {

        // 기사 존재 확인
        Article a = getArticleOrThrow(articleId);

        // 기존 분석 결과가 있으면 갱신, 없으면 생성
        AnalysisResult r = analysisRepo.findByArticle_ArticleId(articleId)
                .orElseGet(() -> AnalysisResult.builder().article(a).build());

        // 필드 매핑(Dto -> 엔티티)
        // 요청 바디(LLM/ML 결과)를 분석 결과 엔티티에 반영
        r.setSummary(req.getSummary());
        r.setSentiment(req.getSentiment());
        r.setKeywords(req.getKeywords());
        r.setTrendScore(req.getTrendScore());
        r.setProcessedAt(req.getProcessedAt());

        AnalysisResult saved = analysisRepo.save(r);

        // 성공 시 분석 상태 ANALYZED로 상태 변경
        a.setIngestStatus(IngestStatus.ANALYZED);

        // 엔티티 형식을 응답용 Dto로 변환해서 반환
        return AnalysisDto.fromEntity(saved);
    }
}
