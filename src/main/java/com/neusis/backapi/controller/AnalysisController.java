package com.neusis.backapi.controller;

import com.neusis.backapi.dto.AnalysisDto;
import com.neusis.backapi.service.AnalysisService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles/{articleId}/analysis")
public class AnalysisController {

    private final AnalysisService analysisService;

    // 기사 분석 결과 Upsert API
    // 분석 결과 전체 수정이므로 Patch 안씀
    // 분석 결과가 전과 동일한 상태가 아닐 수 있으므로 Put 안씀
    @PostMapping
    public ResponseEntity<AnalysisDto> upsertAnalysis(
            @PathVariable Long articleId,
            @RequestBody @Valid AnalysisDto dto
    ) {
        return ResponseEntity.ok(analysisService.upsertAnalysis(articleId, dto));
    }

    // 기사 분석 결과 단건 조회
    @GetMapping
    public ResponseEntity<AnalysisDto> getAnalysis(@PathVariable Long articleId) {
        return ResponseEntity.ok(analysisService.getByArticleId(articleId));
    }

    // 기사 분석 결과 삭제
    @DeleteMapping
    public ResponseEntity<Void> deleteAnalysis(@PathVariable Long articleId) {
        analysisService.deleteByArticleId(articleId);
        return ResponseEntity.noContent().build();
    }
}
