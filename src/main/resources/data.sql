-- 기사 원문 테이블
INSERT INTO article (title, content, source, url, category, published_at, ingest_status, created_at, updated_at)
VALUES
('AI 투자 확대', '정부와 민간이 AI 투자 규모를 키운다.', '연합뉴스', 'https://example.com/ai1', 'IT', '2025-11-01 10:00:00', 'PENDING', NOW(), NOW()),
('한국 경제 성장', '수출과 내수가 동반 상승하며 경제가 회복세를 보인다.', 'KBS', 'https://example.com/economy1', 'ECONOMY', '2025-10-28 09:00:00', 'PENDING', NOW(), NOW()),
('환경 이슈 증가', '기후 변화로 인한 사회적 비용이 커지고 있다.', 'SBS', 'https://example.com/env1', 'SOCIETY', '2025-11-02 14:30:00', 'ANALYZED', NOW(), NOW());

-- 분석 결과 테이블
INSERT INTO analysis_result (article_id, summary, sentiment, trend_score, processed_at, created_at)
VALUES (
  (SELECT article_id FROM article WHERE url = 'https://example.com/env1'),
  '기후 변화의 영향이 확대되며 사회적 대응 필요성이 커지고 있다.',
  'NEGATIVE',   -- 임시로 허용되는 값
  0.74,
  '2025-11-02 15:00:00',
  NOW()
);

-- 키워드 테이블
INSERT INTO analysis_keywords (result_id, keyword)
SELECT r.result_id, k
FROM analysis_result r
JOIN article a ON a.article_id = r.article_id
CROSS JOIN (VALUES ('기후변화'),('온실가스'),('정책대응'),('사회비용'),('환경위기')) AS t(k)
WHERE a.url = 'https://example.com/env1';