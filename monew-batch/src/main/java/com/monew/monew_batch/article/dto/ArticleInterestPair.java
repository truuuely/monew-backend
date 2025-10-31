package com.monew.monew_batch.article.dto;

import com.monew.monew_api.article.entity.Article;
import com.monew.monew_api.interest.entity.Interest;

public record ArticleInterestPair(
        Article article,
        Interest interest
) {}