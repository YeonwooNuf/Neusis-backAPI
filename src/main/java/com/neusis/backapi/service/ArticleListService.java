package com.neusis.backapi.service;

import com.neusis.backapi.domain.Article;
import com.neusis.backapi.domain.Category;
import com.neusis.backapi.domain.IngestStatus;
import com.neusis.backapi.dto.ArticleListDto;
import com.neusis.backapi.repository.ArticleRepository;
import com.neusis.backapi.repository.UserArticleReadRepository;
import com.neusis.backapi.repository.UserLikeRepository;
import com.neusis.backapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ArticleListService {   // 기사 목록을 조회하고 사용자 상태를 함께 반환

    private final UserRepository userRepo;
    private final ArticleRepository articleRepo;
    private final UserLikeRepository likeRepo;
    private final UserArticleReadRepository readRepo;

    @Transactional(readOnly = true)
    public Page<ArticleListDto> getArticleListWithUserFlags(
            Long userId,
            Integer page, Integer size,
            Category category,
            LocalDateTime from, LocalDateTime to,
            IngestStatus status
    ) {
        Pageable pageable = PageRequest.of(
                page == null ? 0 : page,
                size == null ? 20 : Math.min(size, 50), // size 기본값 20, 과도한 요청 방지(최대 50)
                // 최신 기사 우선 정렬
                Sort.by(Sort.Direction.DESC, "publishedAt", "createdAt")
        );

        Page<Article> p;
        // status 파라미터는 요청값이라 null 가능 -> 분기 필수
        if(status != null) {
            p = articleRepo.findByIngestStatus(status, pageable);           // 상태 필터
        } else if (category != null && from != null && to != null) {
            p = articleRepo.findByCategoryAndPublishedAtBetween(category, from, to, pageable);   // 카테고리+기간
        } else if (from != null && to != null) {
            p = articleRepo.findByPublishedAtBetween(from, to, pageable);   // 기간만
        } else {
            p = articleRepo.findAll(pageable);                              // 전체
        }
        
        // 페이징 정보(기사 목록, 전체 건수 등)에서 기사 id만 추출
        // p.getContent -> 현재 페이지의 엔티티 목록을 반환
        // .stream -> 목록 순회
        // .map(Article::getArticleId) -> 각각의 Article 객체에서 id 필드만 추출
        // .toList -> Stream을 다시 List 형태로 변환
        // 결과 -> List<Long> ids = [101, 102, 103, 104, 105];
        var ids = p.getContent().stream().map(Article::getArticleId).toList();
    }
}
