package com.neusis.backapi.controller;

import com.neusis.backapi.domain.Category;
import com.neusis.backapi.domain.IngestStatus;
import com.neusis.backapi.dto.ArticleDto;
import com.neusis.backapi.dto.ArticleListDto;
import com.neusis.backapi.service.ArticleListService;
import com.neusis.backapi.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;
    private final ArticleListService articleListService;

    // 기사 생성(ArticleDto 타입 JSON 반환)
    @PostMapping
    public ResponseEntity<ArticleDto> createArticle(@RequestBody ArticleDto dto) {
        ArticleDto created = articleService.create(dto);
        // Http 응답 코드를 이름으로 표현(201 -> CREATED)
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // 기사 단건 조회
    // PathVariable 사용해서 URL 경로에 변수 추가(articleId)
    @GetMapping("/{articleId}")
    public ResponseEntity<ArticleDto> getArticle(@PathVariable Long articleId) {
        return ResponseEntity.ok(articleService.getByArticleId(articleId));
    }

    // 기사 목록 (필터링 + 페이징)
    // 요청 예시 : (URL?key=value)
    // GET /api/articles?page=0&size=10&category=IT&status=ANALYZED
    // required = false -> 없으면 null 처리 (선택적 필터링 가능)
    @GetMapping
    public ResponseEntity<Page<ArticleListDto>> listArticles(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to,
            @RequestParam(required = false) IngestStatus status,
            @RequestParam(required = false) String search
    ) {
        return ResponseEntity.ok(articleListService.getArticleListWithUserFlags(userId, page, size, category, from, to, status, search));
    }

    // 기사 상태 변경
    // Put (전체 수정) vs Patch (일부 수정)
    // 상태만 변경하므로 PatchMapping 사용
    @PatchMapping("/{articleId}/status")
    public ResponseEntity<ArticleDto> updateStatus(
            @PathVariable Long articleId,
            @RequestParam IngestStatus status   // 쿼리 파라미터로 상태값 전달
    ) {
        return ResponseEntity.ok(articleService.updateStatus(articleId, status));
    }

    // 경로 변수 id 기사 삭제
    // 성공 시 204 No Content (본문 없음)로 반환
    // 서비스 메소드에서 CasCade로 1:1 연관관계 삭제 전파
    // Void -> 객체 타입이 필요한 곳에 리턴 타입이 없다는 걸 '객체 형태로' 표현
    @DeleteMapping("/{articleId}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long articleId) {
        articleService.delete(articleId);
        return ResponseEntity.noContent().build();
    }
}
