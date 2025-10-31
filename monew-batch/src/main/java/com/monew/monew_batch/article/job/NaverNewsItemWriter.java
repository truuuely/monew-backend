package com.monew.monew_batch.article.job;

import com.monew.monew_api.article.entity.Article;
import com.monew.monew_api.article.entity.InterestArticles;
import com.monew.monew_api.article.repository.ArticleKeywordLogRepository;
import com.monew.monew_api.article.repository.ArticleRepository;
import com.monew.monew_api.article.repository.InterestArticlesRepository;
import com.monew.monew_api.common.exception.article.ArticleNotFoundException;
import com.monew.monew_api.interest.entity.Interest;
import com.monew.monew_api.interest.entity.InterestKeyword;
import com.monew.monew_api.interest.entity.Keyword;
import com.monew.monew_batch.article.dto.ArticleInterestPair;
import com.monew.monew_batch.article.repository.ArticleJdbcRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverNewsItemWriter implements ItemWriter<List<ArticleInterestPair>> {

    private final ArticleJdbcRepository articleJdbcRepository;
    private final ArticleRepository articleRepository;
    private final InterestArticlesRepository interestArticlesRepository;
    private final ArticleKeywordLogRepository articleKeywordLogRepository;

    @Override
    public void write(Chunk<? extends List<ArticleInterestPair>> chunk) {
        int total = 0, newCount = 0, linkedCount = 0, skippedCount = 0;

        for (List<ArticleInterestPair> batch : chunk) {
            for (ArticleInterestPair pair : batch) {
                total++;
                Article article = pair.article();
                Interest interest = pair.interest();

                // 1. 기사 저장 및 복구 처리
                boolean isNew = handleInsertIgnore(article);
                if (isNew) newCount++;

                Article savedArticle = handleRestoreAndFind(article);

                // 2. 관심사-기사 및 키워드 로그 처리
                ProcessResult result = handleInterestAndLogs(savedArticle, interest);

                linkedCount += result.linkedCount();
                skippedCount += result.skippedCount();
            }
        }

        logSummary(total, newCount, linkedCount, skippedCount);
    }

    /**
     * JdbcTemplate 기반 insertIgnore 실행
     */
    private boolean handleInsertIgnore(Article article) {
        boolean isNew = articleJdbcRepository.insertIgnore(article);

        if (isNew) {
            log.info("🆕 신규 기사 저장: {}", article.getTitle());
        }

        return isNew;
    }

    /**
     * 삭제된 기사 복구 + DB 조회
     */
    private Article handleRestoreAndFind(Article article) {
        if (articleRepository.restoreIfDeleted(article.getSourceUrl()) > 0) {
            log.info("♻️ 복구된 기사: {}", article.getTitle());
        }

        return articleRepository.findBySourceUrl(article.getSourceUrl())
                .orElseThrow(ArticleNotFoundException::new);
    }

    /**
     * 관심사-기사 관계 및 키워드 로그 처리
     */
    private ProcessResult handleInterestAndLogs(Article article, Interest interest) {
        int linkedCount = 0;
        int skippedCount = 0;

        for (InterestKeyword ik : interest.getKeywords()) {
            Keyword keyword = ik.getKeyword();

            // 키워드 로그 중복 무시 (interest 포함)
            articleKeywordLogRepository.insertIgnore(article.getId(), keyword.getId(), interest.getId());

            // 관심사-기사 연결 (현재 연결 상태용)
            if (!interestArticlesRepository.existsByArticleAndInterest(article, interest)) {
                interestArticlesRepository.save(new InterestArticles(article, interest));
                linkedCount++;
                log.info("🔗 [{}] 관심사-기사 연결 완료: {}", interest.getName(), article.getTitle());
            }
        }

        return new ProcessResult(linkedCount, skippedCount);
    }

    /**
     * 결과 요약 로그
     */
    private void logSummary(int total, int newCount, int linkedCount, int skippedCount) {
        log.info("💾 Writer 결과 | 총: {} | 신규 기사: {} | 연결: {} | 스킵(로그 중복): {}",
                total, newCount, linkedCount, skippedCount);
    }

    private record ProcessResult(int linkedCount, int skippedCount) {}
}