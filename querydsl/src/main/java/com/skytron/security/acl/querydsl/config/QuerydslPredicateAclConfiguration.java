package com.skytron.security.acl.querydsl.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.github.lothar.security.acl.AclStrategyProvider;
import com.github.lothar.security.acl.SimpleAclStrategy;
import com.github.lothar.security.acl.jpa.config.JpaSpecAclConfiguration;
import com.skytron.security.acl.querydsl.QuerydslPredicateFeature;
import com.skytron.security.acl.querydsl.QuerydslPredicateProvider;
import com.skytron.security.acl.querydsl.predicate.AllowAllPredicateFactory;
import com.skytron.security.acl.querydsl.predicate.DenyAllPredicateFactory;
import com.skytron.security.acl.querydsl.predicate.PredicateFactory;

@Configuration
@Import(JpaSpecAclConfiguration.class)
public class QuerydslPredicateAclConfiguration<T> {

  private final QuerydslPredicateFeature<T> querydslPredicateFeature = new QuerydslPredicateFeature<>();
  private final Logger logger = LoggerFactory.getLogger(QuerydslPredicateAclConfiguration.class);

  public QuerydslPredicateAclConfiguration() {
    logger.info("Configured feature : {}", querydslPredicateFeature);
  }

  @Bean
  public QuerydslPredicateFeature<T> querydslPredicateFeature() {
    return querydslPredicateFeature;
  }

  @Bean
  public QuerydslPredicateProvider<T> querydslPredicateProvider(
      final AclStrategyProvider strategyProvider, final PredicateFactory<T> allowAllPredicateFactory) {
    return new QuerydslPredicateProvider<>(strategyProvider, querydslPredicateFeature, allowAllPredicateFactory);
  }

  @Bean(name = { "allowAllPredicateFactory", "defaultAclPredicateFactory" })
  public PredicateFactory<T> allowAllPredicateFactory(final SimpleAclStrategy allowAllStrategy) {
    final AllowAllPredicateFactory<T> allowAllPredicateFactory = new AllowAllPredicateFactory<>();
    allowAllStrategy.install(querydslPredicateFeature, allowAllPredicateFactory);
    return allowAllPredicateFactory;
  }

  @Bean
  public DenyAllPredicateFactory<T> denyAllPredicateFactory(final SimpleAclStrategy denyAllStrategy) {
    final DenyAllPredicateFactory<T> denyAllPredicateFactory = new DenyAllPredicateFactory<>();
    denyAllStrategy.install(querydslPredicateFeature, denyAllPredicateFactory);
    return denyAllPredicateFactory;
  }

}
