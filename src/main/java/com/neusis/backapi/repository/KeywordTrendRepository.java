package com.neusis.backapi.repository;

import com.neusis.backapi.domain.KeywordTrend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface KeywordTrendRepository extends JpaRepository<KeywordTrend, String> {

    // 현재 저장된 키워드들 중에서 7일 기준 최대 카운트
    @Query("select max(k.count7d) from KeywordTrend k")
    Integer findMaxCount7d();
}