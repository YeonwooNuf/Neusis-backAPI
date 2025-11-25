package com.neusis.backapi.service;

import com.neusis.backapi.domain.Article;
import com.neusis.backapi.domain.Category;
import com.neusis.backapi.domain.User;
import com.neusis.backapi.domain.UserRead;
import com.neusis.backapi.dto.UserReadDto;
import com.neusis.backapi.repository.ArticleRepository;
import com.neusis.backapi.repository.UserReadRepository;
import com.neusis.backapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserReadService {

    private final UserRepository userRepo;
    private final ArticleRepository articleRepo;
    private final UserReadRepository readRepo;

    // 기사 상세 진입 시 호출되는 읽음 기록
    // 유저 & 기사 & 오늘 날짜 조합이 존재하면 기록 안 함
    // 날짜가 바뀌면 새 기록 생성
    // 누적 조회수 1 증가
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

        // api 호출 시마다 누적 조회수 증가
        articleRepo.incrementViewCount(articleId);
    }

    // 최근 조회 기사 목록(중복 기사 제거, 최신순)
    public List<UserReadDto> getRecentReads(Long userId, int limit) {
        List<UserRead> rows = readRepo.findRecentDistinctArticles(userId);

        int size = limit > 0 ? limit :10;

        // stream -> List 형 변환
        return rows.stream()
                .limit(size)
                .map(UserReadDto::fromEntity)
                .toList();
    }

    // 오늘 기준 연속 읽기 일수
    // 최근 30일 내에서 연속 일수 계산
    public int getCurrentStreak(Long userId, int days) {

        // 기본 30일 설정
        if (days <= 0) {
            days = 30;
        }

        // 대한민국 기준 시간 적용
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        // streak 계산을 시작할 기준 날짜
        // 오늘 포함이라 빼는 날짜에서 -1(Long 타입)
        LocalDate from = today.minusDays(days - 1L);

        // 최근 N일 동안 사용자가 읽은 날짜들만 중복제거하여 변환
        List<LocalDate> dates = readRepo.findDistinctReadDates(userId, from);

        // 특정 날짜 읽었는지 여부 확인을 O(1) 로 빠르게 하기 위해.
        // 조회한 날짜 목록을 Set 으로 변경
        Set<LocalDate> set = new HashSet<>(dates);

        // 연속 일 수
        int streak = 0;
        // 오늘부터 하루씩 감소하며 streak를 계산하는 포인터
        LocalDate cursor = today;

        // N일 범위(from~today) 안에서
        // 해당 날자 읽은 기록 있으면 streak 이어지고
        // 읽은 기록이 없어지는 순간 loop 종료
        while (!cursor.isBefore(from) && set.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }

        return streak;
    }

    // 사용자가 조회한 기사 중 상위 N개 카테고리 추출
    public List<String> getTopCategoris(Long userId, int limit) {
        List<Object[]> rows = readRepo.findTopCategories(userId, PageRequest.of(0, limit));
        // 조회수는 필요 없고 카테고리 이름만 뽑아오면 되니까 스트림에서 row[0]만 추출.
        return rows.stream()
                .map(row -> ((Category) row[0]).name())    // category 이름(enum 타입)
                .toList();
    }

    // 사용자 읽은 기사 수 조회
    public long getReadCount(Long userId) {
        return readRepo.countByUserUserId(userId);
    }

    // 전체 출석 날짜 리스트 (달력용)
    public List<LocalDate> getAllReadDates(Long userId) {
        return readRepo.findAllDistinctReadDates(userId);
    }
}
