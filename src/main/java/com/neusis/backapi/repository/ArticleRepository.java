package com.neusis.backapi.repository;

import com.neusis.backapi.domain.Article;
import com.neusis.backapi.domain.Category;
import com.neusis.backapi.domain.IngestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

// 기사 원문 테이블을 다루는 Repository(DB 접근 계층)
// 어떤 엔티티를 관리할지(Article) & Article의 기본키 타입(Long)
public interface ArticleRepository extends JpaRepository<Article, Long> {

    // 특정 URL이 이미 존재하는지 여부 확인
    boolean existsByUrl(String url);

    // 기사 목록 출판일 순으로 가져오기
    List<Article> findAllByArticleIdInOrderByPublishedAtDesc(List<Long> articleIds);

//    // 카테고리 + 발행일 기간으로 기사 목록 조회 (페이징 포함)
//    Page<Article> findByCategoryAndPublishedAtBetween(
//            Category category, LocalDateTime from, LocalDateTime to, Pageable pageable);
//
//    // 발행일 기준으로 전체 기사 조회 (카테고리 구분 없음)
//    Page<Article> findByPublishedAtBetween(
//            LocalDateTime from, LocalDateTime to, Pageable pageable);
//
//    // 기사 수집/분석 상태별 조회
//    Page<Article> findByIngestStatus(
//            IngestStatus status, Pageable pageable);

    // 카테고리 + 발행일 기간으로 기사 목록 조회 (페이징 포함)
    // 발행일 기준으로 전체 기사 조회 (카테고리 구분 없음)
    // 기사 수집/분석 상태별 조회
    // 위의 조건별 필터링 및 페이징 통합 버전(null 시 전체 범위)
    @Query("""
    select a from Article a
    where (:category is null or a.category = :category)
      and a.publishedAt >= COALESCE(:from, a.publishedAt)
      and a.publishedAt <= COALESCE(:to, a.publishedAt)
      and (:status is null or a.ingestStatus = :status)
    """)
    Page<Article> search(
            @Param("category") Category category,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("status") IngestStatus status,
            Pageable pageable
    );

    // 검색어 있을 때(제목 LIKE)
    @Query("""
    select a from Article a
    where (:category is null or a.category = :category)
      and a.publishedAt >= COALESCE(:from, a.publishedAt)
      and a.publishedAt <= COALESCE(:to, a.publishedAt)
      and (:status is null or a.ingestStatus = :status)
      and lower(a.title) like lower(:search)
    """)
    Page<Article> searchWithTitle(
            @Param("category") Category category,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("status") IngestStatus status,
            @Param("search") String search,
            Pageable pageable
    );

    @Modifying
    @Query("update Article a set a.viewCount = a.viewCount + 1 where a.articleId = :articleId")
    int incrementViewCount(@Param("articleId") Long articleId);

    // 오늘(기간) 저장된 기사 수
    long countByPublishedAtBetween(LocalDateTime start, LocalDateTime end);

    // 오늘 카테고리별 기사 수
    @Query("""
        select a.category as category, count(a) as cnt
        from Article a
        where a.publishedAt between :start and :end
        group by a.category
        """)
    List<TodayCategoryCount> countTodayByCategory(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    // 쿼리 결과를 담는 틀
    interface TodayCategoryCount {
        Category getCategory();
        long getCnt();
    }
}
