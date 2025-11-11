package com.neusis.backapi.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserArticleRead {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long readId;

    // 조회는 반드시 특정 사용자와 기사에 속해야 함
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Article article;
}
