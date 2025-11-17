package com.neusis.backapi.controller;

import com.neusis.backapi.dto.UserReadDto;
import com.neusis.backapi.service.UserLikeService;
import com.neusis.backapi.service.UserReadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserInteractionController {

    private final UserReadService userReadService;
    private final UserLikeService userLikeService;

    // 기사 상세 진입 시 호출되는 읽음 기록
    @PostMapping("/{userId}/articles/{articleId}/view")
    public ResponseEntity<Void> recordView(
            @PathVariable Long userId,
            @PathVariable Long articleId
    ) {
        userReadService.recordView(userId, articleId);
        return ResponseEntity.ok().build();
    }

    // 좋아요(북마크) 토글
    // 존재하면 false 반환 & 없으면 true 반환
    @PostMapping("/{userId}/articles/{articleId}/like")
    public ResponseEntity<Boolean> toggleLike(
            @PathVariable Long userId,
            @PathVariable Long articleId
    ) {
        boolean liked = userLikeService.toggleLike(userId, articleId);
        return ResponseEntity.ok(liked);
    }

    // 좋아요 여부 확인용 GET 메소드
    @GetMapping("/{userId}/articles/{articleId}/like")
    public ResponseEntity<Boolean> isLiked(
            @PathVariable Long userId,
            @PathVariable Long articleId
    ) {
        boolean liked = userLikeService.isLiked(userId, articleId);
        return ResponseEntity.ok(liked);
    }

    // 사용자 별 좋아요 누른 기사 개수
    @GetMapping("/{userId}/likes/count")
    public ResponseEntity<Long> getLikeCounts(@PathVariable Long userId) {
        Long count = userLikeService.getLikeCount(userId);
        return ResponseEntity.ok(count);
    }

    // 최근 조회 기사 목록(10개)
    // 중복 제거 -> 가장 최신 조회만
    @GetMapping("/{userId}/reads/recent")
    public ResponseEntity<List<UserReadDto>> getRecentReads(
            @PathVariable Long userId,
            @RequestParam(name = "limit", required = false, defaultValue = "10") int limit
    ) {
        List<UserReadDto> recent = userReadService.getRecentReads(userId, limit);
        return ResponseEntity.ok(recent);
    }

    // 연속 읽기 함수
    // 최근 N일 범위에서 오늘부터 연속 기록 계산
    @GetMapping("/{userId}/reads/streak")
    public ResponseEntity<Integer> getCurrentStreak(
            @PathVariable Long userId,
            @RequestParam(name = "days", required = false, defaultValue = "30") int days
    ) {
        int streak = userReadService.getCurrentStreak(userId, days);
        return ResponseEntity.ok(streak);
    }
}
