package com.neusis.backapi.service;

import com.neusis.backapi.domain.Article;
import com.neusis.backapi.domain.User;
import com.neusis.backapi.domain.UserArticleRead;
import com.neusis.backapi.repository.ArticleRepository;
import com.neusis.backapi.repository.UserArticleReadRepository;
import com.neusis.backapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ArticleInteractionService {    // 읽음 기록 + 좋아요 토글 등 기사 상호작용 메소드
    private final UserRepository userRepo;
    private final ArticleRepository articleRepo;
    private final UserArticleReadRepository readRepo;

    @Transactional
    public void recordView(Long userId, Long articleId) {

        // Id만 가져올 때 혹은 연관관계 주입할 때
        // getReferencedById 사용(참조만 반환)
        User user = userRepo.getReferenceById(userId);
        Article article = articleRepo.getReferenceById(articleId);

        // 처음으로 상세 진입 시 읽음 기록
        if(!readRepo.existsByUserUserIdAndArticleArticleId(userId, articleId)) {
            readRepo.save(UserArticleRead.builder()
                    .user(user).article(article).build();
        }
    }
}
