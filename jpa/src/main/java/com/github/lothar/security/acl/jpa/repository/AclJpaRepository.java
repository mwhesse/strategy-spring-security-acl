/*******************************************************************************
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.github.lothar.security.acl.jpa.repository;

import static com.github.lothar.security.acl.jpa.spec.AclJpaSpecifications.idEqualTo;
import static org.springframework.data.jpa.domain.Specification.where;

import java.io.Serializable;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.util.ReflectionUtils;

import com.github.lothar.security.acl.jpa.JpaSpecProvider;
import com.github.lothar.security.acl.jpa.annotation.NoAcl;

public class AclJpaRepository<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> {

  private Logger logger = LoggerFactory.getLogger(getClass());
  private JpaSpecProvider<T> jpaSpecProvider;
  private boolean useAclPredicateForFindBy;

  public AclJpaRepository(Class<T> domainClass, EntityManager em,
      JpaSpecProvider<T> jpaSpecProvider) {
    super(domainClass, em);
    this.jpaSpecProvider = jpaSpecProvider;
  }

  // reflection invocation by
  // com.github.lothar.security.acl.jpa.repository.AclJpaRepositoryFactoryBean.Factory.getTargetRepository(RepositoryInformation,
  // EntityManager)
  public AclJpaRepository(JpaEntityInformation<T, ?> entityInformation, Class<?> repositoryInterface,
      EntityManager entityManager,
      JpaSpecProvider<T> jpaSpecProvider) {
    super(entityInformation, entityManager);
    this.jpaSpecProvider = jpaSpecProvider;

    // check if repository has @NoAcl on method findById()
    // if that's the case we don't want to apply aclJpaSpec()
    useAclPredicateForFindBy = ReflectionUtils.findMethod(repositoryInterface, "findById", Object.class)
        .getAnnotation(NoAcl.class) == null;

    logger.debug("Use filter query on findById: {}", useAclPredicateForFindBy);
  }

  @Override
  public long count() {
    return super.count((Specification<T>) null);
  }

  @Override
  public boolean existsById(ID id) {
    return findById(id).isPresent();
  }

  @Override
  @SuppressWarnings("unchecked")
  public Optional<T> findById(ID id) {
    if (!useAclPredicateForFindBy) {
      try {
        return Optional.of(super.getQuery(idEqualTo(id), getDomainClass(), Sort.unsorted()).getSingleResult());
      } catch (final NoResultException e) {
        return Optional.empty();
      }
    }

    return super.findOne((Specification<T>) idEqualTo(id));
  }

  @Override
  public T getOne(ID id) {
    return this.findById(id)
        .orElseThrow(
            () -> new EntityNotFoundException("Unable to find " + getDomainClass().getName() + " with id " + id));
  }

  @Override
  @SuppressWarnings("unchecked")
  public <S extends T> TypedQuery<Long> getCountQuery(Specification<S> spec,
      Class<S> domainClass) {
    return super.getCountQuery(((Specification<S>) aclJpaSpec()).and(spec), domainClass);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <S extends T> TypedQuery<S> getQuery(Specification<S> spec, Class<S> domainClass,
      Sort sort) {
    return super.getQuery(((Specification<S>) aclJpaSpec()).and(spec), domainClass, sort);
  }

  Specification<T> aclJpaSpec() {
    Specification<T> spec = jpaSpecProvider.jpaSpecFor(getDomainClass());
    logger.debug("Using ACL JPA specification for objects '{}': {}",
        getDomainClass().getSimpleName(), spec);
    return where(spec);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "<" + getDomainClass().getSimpleName() + ">";
  }
}
