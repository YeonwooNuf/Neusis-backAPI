package com.neusis.backapi.domain;

public enum IngestStatus {
    PENDING,    // 분석 대기중
    ANALYZED,   // 분석 완료
    FAILED      // 분석 실패
}
