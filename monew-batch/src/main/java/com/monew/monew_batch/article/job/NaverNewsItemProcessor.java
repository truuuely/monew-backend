package com.monew.monew_batch.article.job;

import com.monew.monew_api.article.entity.Article;
import com.monew.monew_api.interest.entity.Interest;
import com.monew.monew_api.interest.entity.InterestKeyword;
import com.monew.monew_batch.article.dto.ArticleInterestPair;
import com.monew.monew_batch.article.properties.NaverApiProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverNewsItemProcessor implements ItemProcessor<Interest, List<ArticleInterestPair>> {

    private static final int DISPLAY_COUNT = 10;
    private static final String SORT_TYPE = "sim";
    private static final int REQUEST_DELAY_MS = 400;

    private final RestTemplate restTemplate;
    private final NaverApiProperties properties;

    @Override
    public List<ArticleInterestPair> process(Interest interest) {
        List<ArticleInterestPair> collectedArticles = new ArrayList<>();
        int totalFetched = 0;

        for (InterestKeyword ik : interest.getKeywords()) {
            String keyword = ik.getKeyword().getKeyword();
            log.info("🧩 [{}] '{}' 뉴스 수집 시작", interest.getName(), keyword);

            try {
                List<Map<String, Object>> items = fetchNewsItems(keyword);
                if (items.isEmpty()) {
                    log.warn("⚠️ [{} - {}] 뉴스 없음", interest.getName(), keyword);
                    continue;
                }

                totalFetched += items.size();
                collectedArticles.addAll(convertToPairs(items, interest));

                log.info("✅ [{} - {}] 뉴스 {}건 수집 완료 (누적 {})",
                        interest.getName(), keyword, items.size(), totalFetched);

                Thread.sleep(REQUEST_DELAY_MS);

            } catch (Exception e) {
                log.error("❌ [{} - {}] 뉴스 수집 실패: {}", interest.getName(), keyword, e.getMessage(), e);
            }
        }

        log.info("📊 [{}] 총 {}건 기사 수집 완료", interest.getName(), totalFetched);
        return collectedArticles;
    }

    /**
     * 네이버 뉴스 API 호출
     */
    private List<Map<String, Object>> fetchNewsItems(String keyword) {
        String uri = UriComponentsBuilder.fromHttpUrl(properties.getBaseUrl())
                .queryParam("query", keyword)
                .queryParam("display", DISPLAY_COUNT)
                .queryParam("sort", SORT_TYPE)
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", properties.getClientId());
        headers.set("X-Naver-Client-Secret", properties.getClientSecret());

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        Map<String, Object> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class).getBody();
        if (response == null) return Collections.emptyList();

        return (List<Map<String, Object>>) response.getOrDefault("items", Collections.emptyList());
    }

    /**
     * API 응답 데이터를 Article-Interest 쌍으로 변환
     */
    private List<ArticleInterestPair> convertToPairs(List<Map<String, Object>> items, Interest interest) {
        List<ArticleInterestPair> pairs = new ArrayList<>();

        for (Map<String, Object> item : items) {
            String title = cleanText((String) item.get("title"));
            String link = Optional.ofNullable((String) item.get("link")).orElse("");
            String desc = cleanText((String) item.get("description"));
            String pubDateStr = (String) item.get("pubDate");

            LocalDateTime publishDate = parsePublishDate(pubDateStr);

            Article article = new Article("Naver", link, title, publishDate, desc);
            pairs.add(new ArticleInterestPair(article, interest));
        }

        return pairs;
    }

    /**
     * HTML 태그 제거 유틸
     */
    private String cleanText(String text) {
        return Optional.ofNullable(text)
                .map(t -> t.replaceAll("<[^>]*>", ""))
                .orElse("");
    }

    /**
     * pubDate 문자열을 LocalDateTime으로 변환
     */
    private LocalDateTime parsePublishDate(String pubDateStr) {
        try {
            return ZonedDateTime.parse(pubDateStr, DateTimeFormatter.RFC_1123_DATE_TIME)
                    .toLocalDateTime();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

}