package com.neusis.backapi.dto;

import java.util.List;

// 홈페이지 대시보드 요구 데이터를 보내기 위한 dto 클래스
// dto는 불변객체이고 내부에 서브 record 함께 작성하기 위해 record로 선언
// record 는 private final 자동 생성, 생성자 & getter 자동 생성
public record TodayDashboardDto(
        long todaySavedArticles,               // 오늘 저장된 기사 수
        long todayAnalyzedArticles,            // 오늘 분석된 기사 수
        List<IssueTrendItem> issueTrends       // 카테고리 비율 리스트
) {
    public record IssueTrendItem(
            String category,                   // POLITICS / ECONOMY / ...
            double ratio                       // 0 ~ 100 비율
    ) {}
}