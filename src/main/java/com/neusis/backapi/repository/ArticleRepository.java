package com.neusis.backapi.repository;

import com.neusis.backapi.domain.Article;
import com.neusis.backapi.domain.Category;
import com.neusis.backapi.domain.IngestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

// 기사 원문 테이블을 다루는 Repository(DB 접근 계층)
// 어떤 엔티티를 관리할지(Article) & Article의 기본키 타입(Long)
public interface ArticleRepository extends JpaRepository<Article, Long> {

    // 특정 URL이 이미 존재하는지 여부 확인
    boolean existsByUrl(String url);

    // 카테고리 + 발행일 기간으로 기사 목록 조회 (페이징 포함)
    Page<Article> findByCategoryAndPublishedAtBetween(
            Category category, LocalDateTime from, LocalDateTime to, Pageable pageable);

    // 발행일 기준으로 전체 기사 조회 (카테고리 구분 없음)
    Page<Article> findByPublishedAtBetween(
            LocalDateTime from, LocalDateTime to, Pageable pageable);

    // 기사 수집/분석 상태별 조회
    Page<Article> findByIngestStatus(
            IngestStatus status, Pageable pageable);
}
