package com.skytron.security.acl.querydsl.predicate;

import com.querydsl.core.types.Predicate;

/**
 * Creates a predicate.
 *
 * @param <T>
 *          type of entity
 * @since 04.04.2018
 * @author Andreas Gebauer
 */
public interface PredicateFactory<T> {
  /**
   * Creates a predicate for the given class.
   *
   * @param clazz
   *          the domain class
   * @return a predicate to apply to a querydsl query or null
   */
  Predicate apply(Class<T> clazz);
}
