package com.skytron.security.acl.querydsl.repository;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Optional;

import javax.annotation.Resource;
import javax.persistence.EntityManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.query.PartTreeJpaQuery;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.NamedQueries;
import org.springframework.data.repository.core.RepositoryInformation;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.query.EvaluationContextProvider;
import org.springframework.data.repository.query.QueryLookupStrategy;
import org.springframework.data.repository.query.QueryLookupStrategy.Key;
import org.springframework.data.repository.query.RepositoryQuery;

import com.github.lothar.security.acl.Acl;
import com.github.lothar.security.acl.jpa.JpaSpecProvider;
import com.github.lothar.security.acl.jpa.annotation.NoAcl;
import com.github.lothar.security.acl.jpa.query.AclJpaQuery;
import com.skytron.security.acl.querydsl.QuerydslPredicateProvider;

public class AclQuerydslRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable>
    extends JpaRepositoryFactoryBean<T, S, ID> {

  @Resource
  private JpaSpecProvider<Object> jpaSpecProvider;

  @Resource
  private QuerydslPredicateProvider<Object> querydslPredicateProvider;

  public AclQuerydslRepositoryFactoryBean(final Class<? extends T> repositoryInterface) {
    super(repositoryInterface);
  }

  @Override
  protected RepositoryFactorySupport createRepositoryFactory(final EntityManager entityManager) {
    return new Factory(entityManager);
  }

  private class Factory extends JpaRepositoryFactory {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final EntityManager em;

    public Factory(final EntityManager entityManager) {
      super(entityManager);
      em = entityManager;
    }

    @Override
    protected Class<?> getRepositoryBaseClass(final RepositoryMetadata metadata) {
      return !hasAclStrategyAnnotation(metadata.getDomainType()) //
          ? super.getRepositoryBaseClass(metadata) //
          : AclQuerydslRepository.class;
    }

    @Override
    protected Optional<QueryLookupStrategy> getQueryLookupStrategy(final Key key,
        final EvaluationContextProvider evaluationContextProvider) {
      return Optional.of(new AclQueryLookupStrategy(key, evaluationContextProvider));
    }

    @Override
    protected SimpleJpaRepository<?, ?> getTargetRepository(final RepositoryInformation information,
        final EntityManager entityManager) {
      final Class<?> domainType = information.getDomainType();
      if (!hasAclStrategyAnnotation(domainType)) {
        logger.warn("Domain type {} does not have @Acl annotation. Queries will not be restricted by permissions",
            domainType);
        return super.getTargetRepository(information, entityManager);
      }

      final JpaEntityInformation<?, Serializable> entityInformation = getEntityInformation(domainType);

      // invokes
      // com.github.lothar.security.acl.jpa.repository.AclQuerydslRepository.AclQuerydslRepository(JpaEntityInformation<T,
      // ?>, EntityManager, JpaSpecProvider<T>, QuerydslPredicateProvider<T>)
      final SimpleJpaRepository<?, ?> repository = getTargetRepositoryViaReflection(information,
          entityInformation, information.getRepositoryInterface(), entityManager, jpaSpecProvider,
          querydslPredicateProvider);
      logger.debug("Created {}", repository);

      return repository;
    }

    private boolean hasAclStrategyAnnotation(final Class<?> domainType) {
      return domainType.getAnnotation(Acl.class) != null;
    }

    private class AclQueryLookupStrategy implements QueryLookupStrategy {
      private final Key key;
      private final EvaluationContextProvider evaluationContextProvider;

      public AclQueryLookupStrategy(final Key key, final EvaluationContextProvider evaluationContextProvider) {
        this.key = key;
        this.evaluationContextProvider = evaluationContextProvider;
      }

      @Override
      public RepositoryQuery resolveQuery(final Method method, final RepositoryMetadata metadata,
          final ProjectionFactory factory, final NamedQueries namedQueries) {
        final QueryLookupStrategy queryLookupStrategy = Factory.super.getQueryLookupStrategy(key,
            evaluationContextProvider)
                .orElseThrow(() -> new IllegalStateException("Unable to determine QueryLookupStrategy"));

        final RepositoryQuery query = queryLookupStrategy.resolveQuery(method, metadata, factory, namedQueries);
        return wrapQuery(method, metadata, query);
      }

      private RepositoryQuery wrapQuery(final Method method, final RepositoryMetadata metadata, RepositoryQuery query) {
        if (method.getAnnotation(NoAcl.class) != null) {
          // no acl applied here
          return query;
        }
        if (query instanceof PartTreeJpaQuery) {
          query = new AclJpaQuery(method, query, metadata.getDomainType(), em, jpaSpecProvider);
        } else {
          logger.warn(
              "Unsupported query type for method '{}' > ACL Jpa Specification not installed: {}",
              method, query.getClass());
        }
        return query;
      }
    }
  }
}
