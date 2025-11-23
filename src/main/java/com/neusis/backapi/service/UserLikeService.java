package com.neusis.backapi.service;

import com.neusis.backapi.domain.Article;
import com.neusis.backapi.domain.User;
import com.neusis.backapi.domain.UserLike;
import com.neusis.backapi.dto.ArticleDto;
import com.neusis.backapi.repository.ArticleRepository;
import com.neusis.backapi.repository.UserLikeRepository;
import com.neusis.backapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserLikeService {

    private final UserRepository userRepo;
    private final ArticleRepository articleRepo;
    private final UserLikeRepository likeRepo;

    @Transactional
    public boolean toggleLike(Long userId, Long articleId) {
        User user = userRepo.getReferenceById(userId);
        Article article = articleRepo.getReferenceById(articleId);

        // 좋아요 여부
        boolean exists = likeRepo.existsByUserUserIdAndArticleArticleId(userId, articleId);

        if(exists) {
            // 좋아요 눌려있을 시 또 누르면 삭제
            likeRepo.deleteByUserUserIdAndArticleArticleId(userId, articleId);
            return false;
        } else {
            // 좋아요 없을 시 누르면 등록
            likeRepo.save(UserLike.builder().user(user).article(article).build());
            return true;
        }
    }

    // 좋아요 누른 기사 목록 가져오기
    @Transactional(readOnly = true)
    public List<ArticleDto> getLikedArticles(Long userId) {
        // 1) 좋아요 엔티티에서 articleId 목록 가져오기
        List<Long> articleIds = likeRepo.findArticleIdsByUserId(userId);

        if (articleIds.isEmpty()) {
            return List.of();
        }

        // 가져온 id 기반으로 기사 목록 가져오기(최신순 정렬)
        List<Article> articles = articleRepo.findAllByArticleIdInOrderByPublishedAtDesc(articleIds);

        // DTO 변환
        return articles.stream()
                .map(ArticleDto::fromEntity)
                .toList();
    }

    // 좋아요 여부 조회 전용
    @Transactional(readOnly = true)
    public boolean isLiked(Long userId, Long articleId) {
        return likeRepo.existsByUserUserIdAndArticleArticleId(userId, articleId);
    }

    // 사용자 별 좋아요 누른 기사 개수 조회
    @Transactional(readOnly = true)
    public long getLikeCount(Long userId) {
        return likeRepo.countByUserUserId(userId);
    }
}
