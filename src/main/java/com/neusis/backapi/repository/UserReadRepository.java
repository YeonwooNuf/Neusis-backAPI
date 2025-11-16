package com.neusis.backapi.repository;

import com.neusis.backapi.domain.UserRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserReadRepository extends JpaRepository<UserRead, Long> {

    // 특정 유저가 오늘 해당 기사를 읽었는지 여부
    boolean existsByUserUserIdAndArticleArticleIdAndReadDate(
            Long userId,
            Long articleId,
            LocalDate readDate
    );

    // 필요 시 엔티티까지 가져오고 싶을 때
    Optional<UserRead> findByUserUserIdAndArticleArticleIdAndReadDate(
            Long userId,
            Long articleId,
            LocalDate readDate
    );

    // 기사별 최신 조회 정보만 가져오기
    // 최근 조회 목록에서 중복 기사 제거 위함
    @Query("""
    select ur
    from UserRead ur
    where ur.user.userId = :userId
      and ur.readDate = (
        select max(ur2.readDate)
        from UserRead ur2
        where ur2.user.userId = :userId
          and ur2.article.articleId = ur.article.articleId
      )
    order by ur.readDate desc, ur.readId desc
    """)
    List<UserRead> findRecentDistinctArticles(@Param("userId") Long userId);

    // 기사 리스트에 대해 읽음 여부 판단용 (리스트 페이지에서 사용)
    List<UserRead> findByUserUserIdAndArticleArticleIdIn(Long userId, List<Long> articleIds);

    // 연속 출석일 수 계산용
    @Query("""
    select distinct ur.readDate
    from UserRead ur
    where ur.user.userId = :userId
      and ur.readDate >= :from
    """)
    List<LocalDate> findDistinctReadDates(
            @Param("userId") Long userId,
            @Param("from") LocalDate from
    );

    long deleteByUserUserId(Long userId);
}
