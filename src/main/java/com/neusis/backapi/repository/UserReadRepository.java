package com.neusis.backapi.repository;

import com.neusis.backapi.domain.UserRead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserReadRepository extends JpaRepository<UserRead, Long> {

    boolean existsByUserUserIdAndArticleArticleId(Long userId, Long articleId);

    // 한 사용자가 목록 내 10개 기사에 대해 어떤 건 봤었고, 어떤 건 안봤는지 표시
    List<UserRead> findByUserUserIdAndArticleArticleIdIn(Long userId, List<Long> articleIds);
}
