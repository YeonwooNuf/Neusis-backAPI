package com.neusis.backapi.service;

import com.neusis.backapi.domain.Article;
import com.neusis.backapi.domain.User;
import com.neusis.backapi.domain.UserRead;
import com.neusis.backapi.domain.UserLike;
import com.neusis.backapi.repository.ArticleRepository;
import com.neusis.backapi.repository.UserReadRepository;
import com.neusis.backapi.repository.UserLikeRepository;
import com.neusis.backapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleInteractionService {    // 읽음 기록 + 좋아요 토글 등 기사 상호작용 메소드

    private final UserRepository userRepo;
    private final ArticleRepository articleRepo;
    private final UserReadRepository readRepo;
    private final UserLikeRepository likeRepo;

    @Transactional
    public void recordView(Long userId, Long articleId) {

        // Id만 가져올 때 혹은 연관관계 주입할 때
        // getReferencedById 사용(참조만 반환)
        User user = userRepo.getReferenceById(userId);
        Article article = articleRepo.getReferenceById(articleId);

        // 처음으로 상세 진입 시 읽음 기록
        if(!readRepo.existsByUserUserIdAndArticleArticleId(userId, articleId)) {
            readRepo.save(UserRead.builder()
                    .user(user).article(article).build());
        }
    }

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
}
