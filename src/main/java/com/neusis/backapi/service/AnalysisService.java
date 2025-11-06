package com.neusis.backapi.service;

import com.neusis.backapi.domain.AnalysisResult;
import com.neusis.backapi.domain.Article;
import com.neusis.backapi.domain.IngestStatus;
import com.neusis.backapi.dto.AnalysisDto;
import com.neusis.backapi.exception.NotFoundException;
import com.neusis.backapi.repository.AnalysisResultRepository;
import com.neusis.backapi.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalysisService {
    private final ArticleRepository articleRepo;
    private final AnalysisResultRepository analysisRepo;

    private AnalysisResult getArticleOrThrow(Long articleId) {
        return analysisRepo.findByArticle_ArticleId(articleId)
                .orElseThrow(()-> new NotFoundException("다음 기사를 찾을 수 없음 : " + articleId));
    }

    // 분석 결과 Upsert(Update + Insert)
    @Transactional
    public AnalysisDto upsertAnalysis(Long articleId, AnalysisDto req) {

        // 기사 존재 확인
        Article a = articleRepo.findById(articleId)
                .orElseThrow(() -> new NotFoundException("Article not found: " + articleId));

        // 기존 분석 결과가 있으면 갱신, 없으면 생성
        AnalysisResult r = analysisRepo.findByArticle_ArticleId(articleId)
                .orElseGet(() -> AnalysisResult.builder().article(a).build());

        // 필드 매핑(Dto -> 엔티티)
        // 요청 바디(LLM/ML 결과)를 분석 결과 엔티티에 반영
        r.setSummary(req.getSummary());
        r.setSentiment(req.getSentiment());
        r.setKeywords(req.getKeywords());
        r.setTrendScore(req.getTrendScore());
        r.setProcessedAt(req.getProcessedAt());

        AnalysisResult saved = analysisRepo.save(r);

        // 성공 시 분석 상태 ANALYZED로 상태 변경
        a.setIngestStatus(IngestStatus.ANALYZED);

        // 엔티티 형식을 응답용 Dto로 변환해서 반환
        return AnalysisDto.fromEntity(saved);
    }

    // 기사 별 분석 결과 조회(단건)
    // 기사 원문의 articleId 기반으로 연관 조회
    public AnalysisDto getByArticleId(Long articleId) {
        AnalysisResult r = getArticleOrThrow(articleId);
        return AnalysisDto.fromEntity(r);
    }

    // 기사 별 분석 결과 삭제
    @Transactional
    public void deleteByArticleId(Long articleId) {
        AnalysisResult r = getArticleOrThrow(articleId);
        analysisRepo.delete(r);
    }
}
