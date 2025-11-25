package com.neusis.backapi.repository;

import com.neusis.backapi.domain.UserRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
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

    // 전체 출석 날짜 리스트 (전체 누적)
    @Query("""
        select distinct ur.readDate
        from UserRead ur
        where ur.user.userId = :userId
        order by ur.readDate asc
    """)
    List<LocalDate> findAllDistinctReadDates(@Param("userId") Long userId);

    // 사용자가 조회한 기사의 카테고리 중 Top3 추출(조회 빈도 기준)
    // select category, count(ur) 두 개의 컬럼을 쿼리에서 선택했기 때문에
    // 각 row는 [카테고리명, 조회수] 형태. -> Object[]
    @Query("""
         select ur.article.category, count(ur)
         from UserRead ur
         where ur.user.userId = :userId
         group by ur.article.category
         order by count(ur) desc
     """)
    List<Object[]> findTopCategories(@Param("userId") Long userId, Pageable pageable);

    // 사용자 읽은 기사 수 조회
    long countByUserUserId(Long userId);

    long deleteByUserUserId(Long userId);
}
