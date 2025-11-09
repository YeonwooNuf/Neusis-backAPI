package com.neusis.backapi.repository;

import com.neusis.backapi.domain.AnalysisResult;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// 분석 결과 테이블을 다루는 Repository(DB 접근 계층)
// Article 과 1:1 관계이므로 Article ID 기반 쿼리를 지원
public interface AnalysisResultRepository extends JpaRepository<AnalysisResult, Long> {

    // 기사 ID(articleId) 기준으로 분석 결과 조회
    // 분석 상태에 따라 없을 수도 있으므로 Optional 반환
    @EntityGraph(attributePaths = {"keywords", "article"})
    Optional<AnalysisResult> findByArticle_ArticleId(Long articleId);

    // 기사 존재 여부 확인용
    // 중복 분석 등록 방지 등에서 활용 가능
    boolean existsByArticle_ArticleId(Long articleId);
}
