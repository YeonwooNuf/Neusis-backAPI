package com.neusis.backapi.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


// 키워드별 트렌드 통계 엔티티
// 최근 N일 기사 수 집계용 (v1에서는 단순 카운트로 시작)
@Entity
@Table(name = "keyword_trend")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeywordTrend {

    @Id
    @Column(name = "keyword", length = 100)
    private String keyword;        // 키워드 자체가 PK

    @Column(name = "count_7d", nullable = false)
    private int count7d;           // 최근 7일 기준 등장 횟수 (v1: 누적 개념으로 시작해도 OK)

    @Column(name = "count_30d", nullable = false)
    private int count30d;          // 최근 30일 기준 등장 횟수

    @UpdateTimestamp
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;  // 마지막 갱신 시각


    // ====== 편의 메서드 ======

    // 새 기사에 이 키워드가 등장했을 때 호출.
    // 7일/30일 카운트를 1씩 증가시킨다.
    // (추후 sliding window 로직 필요하면 여기 확장)
    public void increaseCounts() {
        this.count7d += 1;
        this.count30d += 1;
    }

    // 최소값 보정용 헬퍼 (0 미만 방지용)
    public void normalize() {
        if (this.count7d < 0) this.count7d = 0;
        if (this.count30d < 0) this.count30d = 0;
    }
}