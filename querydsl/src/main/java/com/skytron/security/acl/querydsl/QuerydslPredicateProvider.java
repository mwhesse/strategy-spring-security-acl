package com.skytron.security.acl.querydsl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.lothar.security.acl.AclStrategy;
import com.github.lothar.security.acl.AclStrategyProvider;
import com.skytron.security.acl.querydsl.predicate.PredicateFactory;

public class QuerydslPredicateProvider<T> {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final AclStrategyProvider strategyProvider;
  private final QuerydslPredicateFeature<T> querydslPredicateFeature;
  private final PredicateFactory<T> defaultQuerydslPredicate;

  public QuerydslPredicateProvider(final AclStrategyProvider strategyProvider,
      final QuerydslPredicateFeature<T> querydslPredicateFeature,
      final PredicateFactory<T> defaultQuerydslPredicate) {
    super();
    this.strategyProvider = strategyProvider;
    this.querydslPredicateFeature = querydslPredicateFeature;
    this.defaultQuerydslPredicate = defaultQuerydslPredicate;
  }

  public PredicateFactory<T> predicateFor(final Class<? extends T> domainType) {
    PredicateFactory<T> aclQuerydslJpaSpec = this.defaultQuerydslPredicate;

    final AclStrategy strategy = strategyProvider.strategyFor(domainType);

    if (strategy == null) {
      logger.debug("No strategy found for '{}' in strategy provider", domainType.getSimpleName());

    } else {
      final PredicateFactory<T> aclQuerydslJpaSpecification = strategy.handlerFor(querydslPredicateFeature);
      if (aclQuerydslJpaSpecification == null) {
        logger.debug(
            "No ACL querydsl predicate found in strategy {} > fall back on default ACL JPA specification", strategy);
      } else {
        aclQuerydslJpaSpec = aclQuerydslJpaSpecification;
      }
    }

    logger.debug("Using ACL querydsl predicate for '{}': {}", domainType.getSimpleName(),
        aclQuerydslJpaSpec);
    return aclQuerydslJpaSpec;
  }
}
