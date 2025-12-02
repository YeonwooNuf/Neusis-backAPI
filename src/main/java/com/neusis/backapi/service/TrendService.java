package com.neusis.backapi.service;

import com.neusis.backapi.domain.Article;
import com.neusis.backapi.domain.KeywordTrend;
import com.neusis.backapi.dto.AnalysisDto;
import com.neusis.backapi.repository.KeywordTrendRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TrendService {

    private final KeywordTrendRepository keywordTrendRepo;

    // ===========================
    // 1) 분석 시 호출되는 메서드
    // ===========================
    // 새 기사 분석 결과를 기반으로 keyword_trend 테이블의
    // 7일/30일 카운트를 업데이트한다.
    // trendScore 계산은 하지 않음.
    @Transactional
    public void updateKeywordTrends(Article article, AnalysisDto analysis) {

        List<String> keywords = analysis.getKeywords();
        if (keywords == null || keywords.isEmpty()) {
            return;
        }

        for (String kw : keywords) {

            if (kw == null || kw.isBlank()) continue;

            // 키워드는 소문자 normalize
            String normalized = kw.trim().toLowerCase();

            // 키워드 row 조회 (없으면 신규 생성)
            KeywordTrend trend = keywordTrendRepo.findById(normalized)
                    .orElseGet(() -> KeywordTrend.builder()
                            .keyword(normalized)
                            .count7d(0)
                            .count30d(0)
                            .build());

            // count 증가
            trend.increaseCounts();

            // 정규화 (음수 방지, 30일 데이터 오래된 값 decay 등 내부 로직)
            trend.normalize();

            // 저장
            keywordTrendRepo.save(trend);
        }
    }

    // ===========================
    // 2) 조회 시 trendScore 계산
    // ===========================
    // article + analysis 결과를 기반으로 동적으로 trendScore 계산
    // DB에 저장하지 않고 ArticleDto 에만 넣는다.
    public double calculateTrendScore(Article article, AnalysisDto analysis) {

        List<String> keywords = (analysis != null) ? analysis.getKeywords() : null;

        // 키워드가 없으면 신선도만 보고 낮은 가중치로 계산
        if (keywords == null || keywords.isEmpty()) {
            double recency = calculateRecencyScore(article.getPublishedAt());
            return clamp(recency * 0.4);
        }

        // 1) 전체 키워드 중 가장 높은 count7d 를 찾는다 (전역 최대값)
        Integer maxCount7d = keywordTrendRepo.findMaxCount7d();
        int max = (maxCount7d == null || maxCount7d <= 0) ? 1 : maxCount7d;

        double sumPopularity = 0.0;
        int validKeywords = 0;

        // 2) 기사 키워드 각각의 인기도(= count7d / max)를 측정하고 평균 낸다
        for (String kw : keywords) {
            if (kw == null || kw.isBlank()) continue;

            String normalized = kw.trim().toLowerCase();

            KeywordTrend trend = keywordTrendRepo.findById(normalized)
                    .orElse(null);

            if (trend == null) continue;

            // 0~1 사이 인기도
            double popularity = (double) trend.getCount7d() / max;

            sumPopularity += popularity;
            validKeywords++;
        }

        double keywordPopularityScore =
                (validKeywords == 0) ? 0.0 : (sumPopularity / validKeywords);

        // 3) 신선도 점수
        double recencyScore = calculateRecencyScore(article.getPublishedAt());

        // 4) 최종 trendScore 가중치 조합
        // 키워드 인기 60%, 신선도 40%
        double raw = 0.6 * keywordPopularityScore + 0.4 * recencyScore;

        return clamp(raw);
    }

    // ===========================
    // 내부 유틸 메서드
    // ===========================

    // 발행일 기준 신선도 점수 계산
    // 오늘은 1.0 / 오래될수록 exp(-λ * days) 형태로 감소
    private double calculateRecencyScore(LocalDateTime publishedAt) {

        if (publishedAt == null) {
            return 0.3;    // 발행일이 없으면 기본값
        }

        long days = Math.max(0, Duration.between(publishedAt, LocalDateTime.now()).toDays());
        double lambda = 0.1;   // 감쇠율 (0.05~0.2 사이 조절 가능)

        return Math.exp(-lambda * days);   // 0~1
    }

    // 값 범위를 0~1로 제한
    private double clamp(double v) {
        if (v < 0.0) return 0.0;
        if (v > 1.0) return 1.0;
        return v;
    }
}