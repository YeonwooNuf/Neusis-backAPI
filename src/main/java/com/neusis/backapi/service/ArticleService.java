package com.neusis.backapi.service;

import com.neusis.backapi.domain.Article;
import com.neusis.backapi.domain.IngestStatus;
import com.neusis.backapi.dto.ArticleDto;
import com.neusis.backapi.exception.NotFoundException;
import com.neusis.backapi.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// final 이 붙은 필드 자동으로 생성자 파라미터로 변환
@RequiredArgsConstructor

// 이 클래스가 실행되는 동안 DB 작업을 하나의 트랜잭션 단위로 묶음
// 기본은 읽기 전용, 쓰기 메서드에만 별도 @Transactional
@Transactional(readOnly = true)

// Article & Analysis 비즈니스 로직
@Service
public class ArticleService {
    private final ArticleRepository articleRepo;    // 기사 원문 repo

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
        Article a = getArticleOrThrow(articleId);
        return ArticleDto.fromEntity(a);
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
