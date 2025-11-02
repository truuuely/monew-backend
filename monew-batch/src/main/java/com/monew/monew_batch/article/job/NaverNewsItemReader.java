package com.monew.monew_batch.article.job;

import com.monew.monew_api.interest.entity.Interest;
import com.monew.monew_api.interest.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@StepScope
@RequiredArgsConstructor
@Slf4j
public class NaverNewsItemReader implements ItemReader<Interest> {

    private final InterestRepository interestRepository;
    private List<Interest> items;
    private int nextIndex = 0;

    @Override
    public synchronized Interest read() {
        if (items == null) {
            items = interestRepository.findAllWithKeywords();
            log.info("📰 관심사 {}개 로드 완료", items.size());
        }

        if (nextIndex < items.size()) {
            return items.get(nextIndex++);
        } else {
            log.info("✅ 모든 관심사 처리 완료");
            return null;
        }
    }

}
