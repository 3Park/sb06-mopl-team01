package org.example.mopl.contentevaluation.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SubscribeQueryRepository {

  private final JPAQueryFactory queryFactory;

}
