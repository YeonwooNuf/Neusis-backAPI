package com.neusis.backapi.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long readId;

    // 조회는 반드시 특정 사용자와 기사에 속해야 함
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    // 조회한 날짜(출석 및 최근 읽은 카테고리 기록 위함)
    @Column(nullable = false)
    private LocalDate readDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Article article;
}
