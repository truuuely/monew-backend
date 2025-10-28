package com.monew.monew_api.article.controller;

import com.monew.monew_api.article.dto.ArticleDto;
import com.monew.monew_api.article.dto.ArticleViewDto;
import com.monew.monew_api.article.dto.CursorPageResponseArticleDto;
import com.monew.monew_api.article.service.ArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/articles")
public class ArticleController {

    private static final String DEFAULT_ARTICLE_SOURCE = "Naver";
    private final ArticleService articleService;

    /**
     * 기사 조회 기록 등록
     */
    @PostMapping("/{articleId}/article-views")
    public ResponseEntity<ArticleViewDto> viewArticle(
            @PathVariable Long articleId,
            @RequestHeader("Monew-Request-User-ID") Long userId
    ) {
        log.info("[API 요청] POST /api/articles/{}/article-views - 기사 조회 기록 요청, 사용자 ID: {}", articleId, userId);
        ArticleViewDto dto = articleService.recordArticleView(articleId, userId);
        log.info("[API 응답] POST /api/articles/{}/article-views - 조회 기록 성공, 조회 기록 ID: {}", articleId, dto.getId());
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    /**
     * 기사 목록 조회 (검색/필터/페이징 포함)
     */
    @GetMapping
    public ResponseEntity<CursorPageResponseArticleDto<ArticleDto>> getArticles(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long interestId,
            @RequestParam(required = false) List<String> sourceIn,
            //
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime publishDateFrom,
            //
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime publishDateTo,
            //
            @RequestParam(defaultValue = "publishDate") String orderBy,
            @RequestParam(defaultValue = "DESC") String direction,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime after,
            @RequestParam(defaultValue = "10") int limit,
            @RequestHeader("Monew-Request-User-ID") Long userId
    ) {
        log.info("[API 요청] GET /api/articles - 기사 목록 조회 요청, 사용자 ID: {}, 키워드: {}, 관심사 ID: {}",
                userId, keyword, interestId);

        if (sourceIn == null || sourceIn.isEmpty()) {
            sourceIn = List.of(DEFAULT_ARTICLE_SOURCE);
        }

        LocalDateTime now = LocalDateTime.now();
        if (publishDateFrom == null) {
            publishDateFrom = now.minusDays(7);
        }
        if (publishDateTo == null) {
            publishDateTo = now;
        }

        log.debug("[조회 파라미터] sourceIn: {}, 기간: {} ~ {}, 정렬: {} {}, limit: {}",
                sourceIn, publishDateFrom, publishDateTo, orderBy, direction, limit);

        CursorPageResponseArticleDto<ArticleDto> dto = articleService.getArticles(
                keyword, interestId, sourceIn,
                publishDateFrom, publishDateTo,
                orderBy, direction,
                cursor, after, limit, userId
        );

        log.info("[API 응답] GET /api/articles - 조회 성공, 반환된 기사 수: {}", dto.getContent().size());
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    /**
     * 단일 기사 상세 조회
     */
    @GetMapping("/{articleId}")
    public ResponseEntity<ArticleDto> getArticleById(
            @PathVariable Long articleId,
            @RequestHeader("Monew-Request-User-ID") Long userId
    ) {
        log.info("[API 요청] GET /api/articles/{} - 기사 상세 조회 요청, 사용자 ID: {}", articleId, userId);
        ArticleDto dto = articleService.findArticle(articleId, userId);
        log.info("[API 응답] GET /api/articles/{} - 기사 상세 조회 성공", articleId);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    /**
     * 기사 출처 목록 조회
     */
    @GetMapping("/sources")
    public ResponseEntity<List<String>> getSources() {
        log.info("[API 요청] GET /api/articles/sources - 뉴스 출처 목록 조회 요청");
        List<String> sources = articleService.getAllSources();
        log.info("[API 응답] GET /api/articles/sources - 뉴스 출처 목록 조회 성공, 개수: {}", sources.size());
        return ResponseEntity.status(HttpStatus.OK).body(sources);
    }

    /**
     * 기사 논리 삭제
     */
    @DeleteMapping("/{articleId}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long articleId) {
        log.info("[API 요청] DELETE /api/articles/{} - 기사 논리 삭제 요청", articleId);
        articleService.softDeleteArticle(articleId);
        log.info("[API 응답] DELETE /api/articles/{} - 기사 논리 삭제 성공", articleId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 기사 영구 삭제
     */
    @DeleteMapping("/{articleId}/hard")
    public ResponseEntity<Void> hardDeleteArticle(@PathVariable Long articleId) {
        log.info("[API 요청] DELETE /api/articles/{}/hard - 기사 영구 삭제 요청", articleId);
        articleService.hardDeleteArticle(articleId);
        log.info("[API 응답] DELETE /api/articles/{}/hard - 기사 영구 삭제 성공", articleId);
        return ResponseEntity.noContent().build();
    }
}