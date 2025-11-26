package com.neusis.backapi.service;

import com.neusis.backapi.domain.Article;
import com.neusis.backapi.domain.Category;
import com.neusis.backapi.domain.IngestStatus;
import com.neusis.backapi.dto.ArticleListDto;
import com.neusis.backapi.repository.ArticleRepository;
import com.neusis.backapi.repository.UserLikeRepository;
import com.neusis.backapi.repository.UserReadRepository;
import com.neusis.backapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleListService {   // 기사 목록을 조회하고 사용자 상태를 함께 반환

    private final UserRepository userRepo;
    private final ArticleRepository articleRepo;
    private final UserLikeRepository likeRepo;
    private final UserReadRepository readRepo;

    @Transactional(readOnly = true)
    public Page<ArticleListDto> getArticleListWithUserFlags(
            Long userId,
            Integer page, Integer size,
            Category category,
            LocalDateTime from, LocalDateTime to,
            IngestStatus status,
            String search
    ) {
        Pageable pageable = PageRequest.of(
                page == null ? 0 : page,    // size 기본값 20, 과도한 요청 방지(최대 50)
                size == null ? 20 : Math.min(size, 50),    // 최신 기사 우선 정렬
                Sort.by(Sort.Direction.DESC, "publishedAt", "createdAt")
        );

        // 검색어가 비어 있으면 null, 아니면 %검색어% 형태로 변환
        String likeSearch = (search == null || search.isBlank())
                ? null
                : "%" + search.trim() + "%";

        Page<Article> p;

        if (likeSearch == null) {
            // 검색어 없음 -> 기본 검색 쿼리
            p = articleRepo.search(category, from, to, status, pageable);
        } else {
            // 검색어 있음 -> 제목 LIKE 검색 쿼리
            p = articleRepo.searchWithTitle(category, from, to, status, likeSearch, pageable);
        }

        var ids = p.getContent().stream().map(Article::getArticleId).toList();

        var read = readRepo.findByUserUserIdAndArticleArticleIdIn(userId, ids)
                .stream()
                // 각 UserRead 객체에서 연결된 기사의 Id만 추출
                // 중복 없는 형태로 수집 ( ex -> read = {101, 102, 103}; )
                .map(r -> r.getArticle().getArticleId())
                .collect(Collectors.toSet());

        var liked = likeRepo.findByUserUserIdAndArticleArticleIdIn(userId, ids)
                .stream()
                // 각 UserLike 객체에서 연결된 기사의 Id만 추출
                // 중복 없는 형태로 수집 ( ex -> liked = {101, 104}; )
                .map(l -> l.getArticle().getArticleId())
                .collect(Collectors.toSet());

        return p.map(a -> ArticleListDto.fromEntity(
                a,  // Article 객체
                read.contains(a.getArticleId()),    // 읽음 집합에 포함되면 true 반환
                liked.contains(a.getArticleId())    // 좋아요 집합에 포함되면 true 반환
        ));
    }

//    @Transactional(readOnly = true)
//    public Page<ArticleListDto> getArticleListWithUserFlags(
//            Long userId,
//            Integer page, Integer size,
//            Category category,
//            LocalDateTime from, LocalDateTime to,
//            IngestStatus status
//    ) {
//        Pageable pageable = PageRequest.of(
//                page == null ? 0 : page,
//                size == null ? 20 : Math.min(size, 50), // size 기본값 20, 과도한 요청 방지(최대 50)
//                // 최신 기사 우선 정렬
//                Sort.by(Sort.Direction.DESC, "publishedAt", "createdAt")
//        );
//
//        Page<Article> p;
//        // status 파라미터는 요청값이라 null 가능 -> 분기 필수
//        if(status != null) {
//            p = articleRepo.findByIngestStatus(status, pageable);           // 상태 필터
//        } else if (category != null && from != null && to != null) {
//            p = articleRepo.findByCategoryAndPublishedAtBetween(category, from, to, pageable);   // 카테고리+기간
//        } else if (from != null && to != null) {
//            p = articleRepo.findByPublishedAtBetween(from, to, pageable);   // 기간만
//        } else {
//            p = articleRepo.findAll(pageable);                              // 전체
//        }
//
//        // 페이징 정보(기사 목록, 전체 건수 등)에서 기사 id만 추출
//        // p.getContent -> 현재 페이지의 엔티티 목록을 반환
//        // .stream -> 목록 순회
//        // .map(Article::getArticleId) -> 각각의 Article 객체에서 Id 필드만 추출
//        // .toList -> Stream을 다시 List 형태로 변환
//        // 결과 -> List<Long> ids = [101, 102, 103, 104, 105];
//        var ids = p.getContent().stream().map(Article::getArticleId).toList();
//
//        // UserArticleRead 엔티티 리스트 반환
//        Set<Long> read = readRepo.findByUserUserIdAndArticleArticleIdIn(userId, ids)
//                // 각 UserRead 객체에서 연결된 기사의 Id만 추출
//                .stream().map(userRead -> userRead.getArticle().getArticleId())
//                // 중복 없는 Set<Long> 형태로 수집 ( ex -> read = {101, 102, 103}; )
//                .collect(Collectors.toSet());
//
//        // UserLike 엔티티 리스트 반환
//        Set<Long> liked = likeRepo.findByUserUserIdAndArticleArticleIdIn(userId, ids)
//                // 각 UserLike 객체에서 연결된 기사의 Id만 추출
//                .stream().map(userLike -> userLike.getArticle().getArticleId())
//                // 중복 없는 Set<Long> 형태로 수집 ( ex -> liked = {101, 104}; )
//                .collect(Collectors.toSet());
//
//        // p = 현재 페이지의 기사 엔티티 목록을 포함한 페이징 객체
//        // Page 내부의 각 Article 엔티티를 DTO로 변환
//        return p.map(a -> ArticleListDto.fromEntity(
//                a,  // Article 객체
//                read.contains(a.getArticleId()),    // 읽음 집합에 포함되면 true 반환
//                liked.contains(a.getArticleId())    // 좋아요 집합에 포함되면 true 반환
//        ));
//    }
}
