package com.neusis.backapi.service;

import com.neusis.backapi.domain.Category;
import com.neusis.backapi.dto.TodayDashboardDto;
import com.neusis.backapi.repository.AnalysisResultRepository;
import com.neusis.backapi.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ArticleRepository articleRepository;
    private final AnalysisResultRepository analysisResultRepository;

    // 오늘의 대시보드 구성용 메소드
    public TodayDashboardDto getTodayDashboard() {
        // 서버 기본 타임존 기준 (Asia/Seoul)
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();           // 00:00:00
        LocalDateTime end = today.plusDays(1).atStartOfDay(); // 다음날 00:00:00

        // 오늘 저장된 기사 수 (publishedAt 기준)
        long todaySaved = articleRepository.countByPublishedAtBetween(start, end);

        // 오늘 분석된 기사 수 (AnalysisResult.createdAt 기준)
        long todayAnalyzed = analysisResultRepository.countByCreatedAtBetween(start, end);

        // 오늘 카테고리별 기사 수
        var rawCounts = articleRepository.countTodayByCategory(start, end);

        // 조회된 카테고리별 개수를 카테고리와 매핑하여 정리
        Map<Category, Long> countMap = new EnumMap<>(Category.class);
        for (ArticleRepository.TodayCategoryCount c : rawCounts) {
            countMap.put(c.getCategory(), c.getCnt());
        }

        // 전체 기사 수 계산 (비율 계산용)
        long total = countMap.values().stream()
                .mapToLong(Long::longValue)
                .sum();

        // ENUM 타입 카테고리 6개 지 (순서 고정)
        List<Category> categories = List.of(
                Category.POLITICS,
                Category.ECONOMY,
                Category.SOCIETY,
                Category.CULTURE,
                Category.WORLD,
                Category.IT
        );

        // 카테고리 & 비율 계산
        List<TodayDashboardDto.IssueTrendItem> trends = categories.stream()
                .map(cat -> {
                    long cnt = countMap.getOrDefault(cat, 0L);
                    double ratio = (total == 0) ? 0.0 : (cnt * 100.0 / total);
                    return new TodayDashboardDto.IssueTrendItem(cat.name(), ratio);
                })
                .toList();

        return new TodayDashboardDto(todaySaved, todayAnalyzed, trends);
    }
}