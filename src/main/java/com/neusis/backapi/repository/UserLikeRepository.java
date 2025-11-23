package com.neusis.backapi.repository;

import com.neusis.backapi.domain.UserLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserLikeRepository extends JpaRepository<UserLike, Long> {

    boolean existsByUserUserIdAndArticleArticleId(Long userId, Long articleId);

    long deleteByUserUserId(Long userId);

    long deleteByUserUserIdAndArticleArticleId(Long userId, Long articleId);

    // 좋아요 누른 기사 목록의 articleId 가져오기
    @Query("SELECT ul.article.articleId FROM UserLike ul WHERE ul.user.userId = :userId")
    List<Long> findArticleIdsByUserId(@Param("userId") Long userId);

    // 한 사용자가 목록 내 10개 기사에 대해 어떤 건 좋아요 눌렀고, 어떤 건 안 눌렀는지 표시
    List<UserLike> findByUserUserIdAndArticleArticleIdIn(Long userId, List<Long> articleIds);

    // 유저 별 좋아요(북마크) 개수
    long countByUserUserId(Long userId);
}
