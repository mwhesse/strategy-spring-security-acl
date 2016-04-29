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
package com.github.lothar.security.acl.jpa.query;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.PartTreeJpaQuery;
import org.springframework.data.repository.query.QueryMethod;
import org.springframework.data.repository.query.RepositoryQuery;
import org.springframework.util.ReflectionUtils;

import com.github.lothar.security.acl.jpa.JpaSpecProvider;

public class AclJpaQuery implements RepositoryQuery {


  private Logger logger = LoggerFactory.getLogger(getClass());
  private RepositoryQuery query;
  private Class<?> domainType;
  private EntityManager em;
  private JpaSpecProvider<Object> jpaSpecProvider;
  private Method method;

  public AclJpaQuery(Method method, RepositoryQuery query, Class<?> domainType, EntityManager em,
      JpaSpecProvider<Object> jpaSpecProvider) {
    this.method = method;
    this.query = query;
    this.domainType = domainType;
    this.em = em;
    this.jpaSpecProvider = jpaSpecProvider;
  }

  @Override
  public synchronized Object execute(Object[] parameters) {
    Predicate predicate = installAclSpec();
    try {
      return query.execute(parameters);
    } finally {
      uninstallAclSpec(predicate);
    }
  }

  private void uninstallAclSpec(Predicate predicate) {
    try {
      CriteriaQuery<?> criteriaQuery = criteriaQuery();
      criteriaQuery.where(predicate);
      logger.debug("ACL Jpa Specification uninstalled from method '{}' and query {}", method,
          query);
    } catch (Exception e) {
      logger.warn("Unable to uninstall ACL Jpa Specification from method '" + method
          + "' and query: " + query, e);
    }
  }

  private Predicate installAclSpec() {
    try {
      CriteriaQuery<?> criteriaQuery = criteriaQuery();
      Predicate restriction = criteriaQuery.getRestriction();
      Specification<Object> jpaSpec = jpaSpecProvider.jpaSpecFor(domainType);
      Predicate aclPredicate =
          jpaSpec.toPredicate(root(criteriaQuery), criteriaQuery, em.getCriteriaBuilder());
      criteriaQuery.where(restriction, aclPredicate);
      logger.debug("ACL Jpa Specification installed for method '{}' and query {}: {}", method,
          query, jpaSpec);
      return restriction;
    } catch (Exception e) {
      logger.warn(
          "Unable to install ACL Jpa Specification for method '" + method + "' and query: " + query,
          e);
      return null;
    }
  }

  @Override
  public QueryMethod getQueryMethod() {
    return query.getQueryMethod();
  }

  private CriteriaQuery<?> criteriaQuery() {
    Object queryPreparer = getField(PartTreeJpaQuery.class, query, "query");
    CriteriaQuery<?> criteriaQuery =
        getField(queryPreparer.getClass(), queryPreparer, "cachedCriteriaQuery");
    return criteriaQuery;
  }

  @SuppressWarnings("unchecked")
  private Root<Object> root(CriteriaQuery<?> criteriaQuery) {
    return (Root<Object>) criteriaQuery.getRoots().iterator().next();
  }

  @SuppressWarnings("unchecked")
  private static <T> T getField(Class<?> type, Object object, String fieldName) {
    Field field = ReflectionUtils.findField(type, fieldName);
    field.setAccessible(true);
    Object property = ReflectionUtils.getField(field, object);
    return (T) property;
  }

}