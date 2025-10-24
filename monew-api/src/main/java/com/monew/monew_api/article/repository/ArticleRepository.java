package com.monew.monew_api.article.repository;

import com.monew.monew_api.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {

}
