package com.neusis.backapi.controller;

import com.neusis.backapi.dto.ArticleDto;
import com.neusis.backapi.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    // 기사 생성(ArticleDto 타입 JSON 반환)
    @PostMapping
    public ResponseEntity<ArticleDto> createArticle(@RequestBody ArticleDto dto) {
        ArticleDto created = articleService.create(dto);
        // Http 응답 코드를 이름으로 표현(201 -> CREATED)
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}
