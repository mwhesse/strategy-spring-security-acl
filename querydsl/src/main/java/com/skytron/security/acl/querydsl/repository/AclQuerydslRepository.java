package com.skytron.security.acl.querydsl.repository;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.QuerydslJpaRepository;
import org.springframework.data.querydsl.EntityPathResolver;

import com.github.lothar.security.acl.jpa.JpaSpecProvider;
import com.github.lothar.security.acl.jpa.repository.AclJpaRepository;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.JPQLQuery;
import com.skytron.security.acl.querydsl.QuerydslPredicateProvider;
import com.skytron.security.acl.querydsl.predicate.PredicateFactory;

public class AclQuerydslRepository<T, ID extends Serializable> extends QuerydslJpaRepository<T, ID> {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  private final AclJpaRepository<T, ID> aclJpaRepository;
  private final QuerydslPredicateProvider<T> querydslPredicateProvider;

  public AclQuerydslRepository(final JpaEntityInformation<T, ID> entityInformation, final EntityManager em,
      final EntityPathResolver resolver,
      final JpaSpecProvider<T> jpaSpecProvider, final QuerydslPredicateProvider<T> querydslPredicateProvider) {
    super(entityInformation, em, resolver);
    this.aclJpaRepository = new AclJpaRepository<>(entityInformation, em, jpaSpecProvider);
    this.querydslPredicateProvider = querydslPredicateProvider;
  }

  // reflection invocation by
  // com.github.lothar.security.acl.jpa.repository.AclJpaRepositoryFactoryBean.Factory.getTargetRepository(RepositoryInformation,
  // EntityManager)
  public AclQuerydslRepository(final JpaEntityInformation<T, ID> entityInformation, final EntityManager em,
      final JpaSpecProvider<T> jpaSpecProvider, final QuerydslPredicateProvider<T> querydslPredicateProvider) {
    super(entityInformation, em);
    this.aclJpaRepository = new AclJpaRepository<>(entityInformation, em, jpaSpecProvider);
    this.querydslPredicateProvider = querydslPredicateProvider;
  }

  @Override
  public long count() {
    return this.aclJpaRepository.count();
  }

  @Override
  public boolean existsById(final ID id) {
    return this.aclJpaRepository.existsById(id);
  }

  @Override
  public Optional<T> findById(final ID id) {
    return this.aclJpaRepository.findById(id);
  }

  @Override
  public T getOne(final ID id) {
    return this.aclJpaRepository.getOne(id);
  }

  @Override
  protected <S extends T> TypedQuery<Long> getCountQuery(final Specification<S> spec, final Class<S> domainClass) {
    return this.aclJpaRepository.getCountQuery(spec, domainClass);
  }

  @Override
  protected <S extends T> TypedQuery<S> getQuery(final Specification<S> spec, final Class<S> domainClass,
      final Sort sort) {
    return this.aclJpaRepository.getQuery(spec, domainClass, sort);
  }

  // query dsl stuff starting here

  @Override
  protected JPQLQuery<?> createCountQuery(final Predicate... predicate) {
    return super.createCountQuery(combinePredicates(predicate));
  }

  @Override
  protected JPQLQuery<?> createQuery(final Predicate... predicate) {
    return super.createQuery(combinePredicates(predicate));
  }

  private Predicate[] combinePredicates(final Predicate[] predicate) {
    final PredicateFactory<T> aclQuerydslPredicateFactory = aclQuerydslPredicateFactory();
    if (aclQuerydslPredicateFactory == null) {
      return predicate;
    }
    final Predicate aclPredicate = aclQuerydslPredicateFactory.apply(getDomainClass());
    if (aclPredicate == null) {
      return predicate;
    }

    final Predicate[] combinedPredicates = Arrays.copyOf(predicate, predicate.length + 1);
    combinedPredicates[predicate.length] = aclPredicate;

    return combinedPredicates;
  }

  private PredicateFactory<T> aclQuerydslPredicateFactory() {
    final PredicateFactory<T> predicate = querydslPredicateProvider.predicateFor(getDomainClass());
    logger.debug("Using ACL Querydsl predicate for objects '{}': {}",
        getDomainClass().getSimpleName(), predicate);
    return predicate;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "<" + getDomainClass().getSimpleName() + ">";
  }
}
