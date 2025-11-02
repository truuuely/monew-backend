package com.monew.monew_api.interest.repository;

import com.monew.monew_api.interest.entity.Interest;
import com.monew.monew_api.interest.dto.InterestOrderBy;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Repository;

import static com.monew.monew_api.interest.entity.QInterestKeyword.interestKeyword;
import static com.monew.monew_api.interest.entity.QKeyword.keyword1;
import static com.monew.monew_api.interest.entity.QInterest.interest;
import static com.monew.monew_api.subscribe.entity.QSubscribe.subscribe;

@Repository
@RequiredArgsConstructor
public class InterestRepositoryCustomImpl implements InterestRepositoryCustom {

  private final JPAQueryFactory queryFactory;

 @Override
  public Slice<Interest> findAll(
      String searchKeyword,
      InterestOrderBy sortBy,
      Direction direction,
      String cursor,
      LocalDateTime after,
      int limit
  ) {

    BooleanBuilder builder = new BooleanBuilder()
        .and(containsInterestOrKeyword(searchKeyword));
    if (after != null) {
      builder.and(interest.createdAt.goe(after));
    }
    builder.and(cursorCondition(cursor, sortBy, direction));

    // id + 정렬값
    Expression<?> sortExpr = sortExpression(sortBy);

    List<Tuple> rows = queryFactory
        .selectDistinct(interest.id, sortExpr)
        .from(interest)
        .leftJoin(interest.keywords,
            interestKeyword)
        .leftJoin(interestKeyword.keyword, keyword1)
        .where(builder)
        .orderBy(sortBy(sortBy, direction))
        .limit(limit + 1)
        .fetch();

    boolean hasNext = rows.size() > limit;
    if (hasNext)
      rows = rows.subList(0, limit);

    // 추출된 id들만 조회
    List<Long> ids = rows.stream()
        .map(t -> t.get(interest.id))
        .toList();

    List<Interest> interests = queryFactory
        .selectFrom(interest)
        .distinct()
        .leftJoin(interest.keywords, interestKeyword).fetchJoin()
        .leftJoin(interestKeyword.keyword, keyword1).fetchJoin()
        .where(interest.id.in(ids))
        .orderBy(sortBy(sortBy, direction
        ))
        .fetch();

    return new SliceImpl<>(interests, PageRequest.of(0, limit), hasNext);
  }


  private BooleanExpression containsInterestOrKeyword(String searchKeyword) {
    if (searchKeyword == null || searchKeyword.isBlank())
      return null;
    return interest.name.containsIgnoreCase(searchKeyword)
        .or(keyword1.keyword.containsIgnoreCase(searchKeyword));
  }

  private Expression<?> sortExpression(InterestOrderBy sortBy) {
    return switch (sortBy) {
      case name -> interest.name;
      case subscriberCount -> interest.subscriberCount;
    };
  }

  // 커서 조건: cursor는 id로 가정 -> 해당 id 레코드를 읽어 1차 정렬값 + id로 커팅
  private BooleanExpression cursorCondition(
      String cursor, InterestOrderBy sortBy, Direction direction) {
    if (cursor == null || cursor.isBlank())
      return null;

    boolean desc = (direction == Direction.DESC);
    Long cursorId = Long.valueOf(cursor);

    Interest cursorInterest = queryFactory
        .selectFrom(interest)
        .where(interest.id.eq(cursorId))
        .fetchOne();

    if (cursorInterest == null)
      return null;

    return switch (sortBy) {
      case name -> {
        String afterName = cursorInterest.getName();
        yield desc
            ? interest.name.lt(afterName)
            : interest.name.gt(afterName);
      }
      case subscriberCount -> {
        int afterCnt = cursorInterest.getSubscriberCount();
        yield desc
            ? interest.subscriberCount.lt(afterCnt)
            .or(interest.subscriberCount.eq(afterCnt)
                .and(interest.id.lt(cursorId)))
            : interest.subscriberCount.gt(afterCnt)
                .or(interest.subscriberCount.eq(afterCnt)
                    .and(interest.id.gt(cursorId)));
      }
    };
  }

  private OrderSpecifier<?>[] sortBy(InterestOrderBy sortBy, Direction direction) {
    boolean asc = (direction == Direction.ASC);
    return switch (sortBy) {
      case name -> asc
          ? new OrderSpecifier[]{interest.name.asc(), interest.id.asc()}
          : new OrderSpecifier[]{interest.name.desc(), interest.id.desc()};
      case subscriberCount -> asc
          ? new OrderSpecifier[]{interest.subscriberCount.asc(), interest.id.asc()}
          : new OrderSpecifier[]{interest.subscriberCount.desc(), interest.id.desc()};
    };
  }



  @Override
  public long countFilteredTotalElements(String keyword, InterestOrderBy orderBy,
      Direction direction) {

    JPAQuery<Long> query = queryFactory
        .select(interest.countDistinct())
        .from(interest);

    BooleanBuilder builder = new BooleanBuilder();

    // keyword가 있을 때만 조인
    if (keyword != null && !keyword.isBlank()) {
      query
          .leftJoin(interest.keywords, interestKeyword)
          .leftJoin(interestKeyword.keyword, keyword1);
      builder.and(
          interest.name.containsIgnoreCase(keyword)
              .or(keyword1.keyword.containsIgnoreCase(keyword))
      );
    }
    Long count = query.where(builder).fetchOne();
    return (count != null) ? count : 0L;

  }
}


