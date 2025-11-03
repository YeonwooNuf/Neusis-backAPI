package com.neusis.backapi.service;

import com.neusis.backapi.domain.*;
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
        Article a = articleRepo.findById(articleId)
                .orElseThrow(()-> new NotFoundException("다음 원문 찾을 수 없음 : " + articleId));
        return ArticleDto.fromEntity(a);
    }

    // 기사 목록 (필터 & 페이지)
    // status / category+기간 / 기간 / 전체
    // 최신(publishedAt, createdAt) 우선 정렬
    public Page<ArticleDto> list(Integer page, Integer size, Category category,
                                 LocalDateTime from, LocalDateTime to, IngestStatus status) {
        Pageable pageable = PageRequest.of(
                page == null ? 0 : page,
                size == null ? 20 : size,
                Sort.by(Sort.Direction.DESC, "publishedAt", "createdAt")
        );

        Page<Article> result;
        if (status != null) {
            result = articleRepo.findByIngestStatus(status, pageable);
        } else if (category != null && from != null && to != null) {
            result = articleRepo.findByCategoryAndPublishedAtBetween(category, from, to, pageable);
        } else if (from != null && to != null) {
            result = articleRepo.findByPublishedAtBetween(from, to, pageable);
        } else {
            result = articleRepo.findAll(pageable);
        }
        return result.map(ArticleDto::fromEntity);
    }
}
