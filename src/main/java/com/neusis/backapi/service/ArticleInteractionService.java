package com.neusis.backapi.service;

import com.neusis.backapi.domain.Article;
import com.neusis.backapi.domain.User;
import com.neusis.backapi.domain.UserLike;
import com.neusis.backapi.domain.UserRead;
import com.neusis.backapi.repository.ArticleRepository;
import com.neusis.backapi.repository.UserLikeRepository;
import com.neusis.backapi.repository.UserReadRepository;
import com.neusis.backapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ArticleInteractionService {    // 읽음 기록 + 좋아요 토글 등 기사 상호작용 메소드

    private final UserRepository userRepo;
    private final ArticleRepository articleRepo;
    private final UserReadRepository readRepo;
    private final UserLikeRepository likeRepo;


    /**
     * 기사 상세 진입 시 호출되는 읽음 기록
     * - 유저/기사/오늘 날짜 조합이 존재하면 기록 안 함
     * - 날짜가 바뀌면 새 기록 생성
     */
    @Transactional
    public void recordView(Long userId, Long articleId) {

        // Id만 가져올 때 혹은 연관관계 주입할 때
        // getReferencedById 사용(참조만 반환)
        User user = userRepo.getReferenceById(userId);
        Article article = articleRepo.getReferenceById(articleId);

        // 오늘 날짜 받아오기
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));

        // 오늘 이미 읽었으면 추가 저장 X
        boolean existsToday = readRepo.existsByUserUserIdAndArticleArticleIdAndReadDate(
                userId, articleId, today
        );

        // 처음으로 상세 진입 시 읽음 기록
        if (!existsToday) {
            readRepo.save(UserRead.builder()
                    .user(user)
                    .article(article)
                    .readDate(today)
                    .build()
            );
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
