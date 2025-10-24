package com.monew.monew_api.article.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ArticleQueryRepositoryImpl implements ArticleQueryRepository {

    private final JPAQueryFactory queryFactory;
}
