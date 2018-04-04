package com.skytron.security.acl.querydsl.predicate;

import com.github.lothar.security.acl.named.NamedBean;
import com.querydsl.core.types.Predicate;

/**
 * Allows all access.
 * 
 * @param <T>
 *          type of entity
 * @since 04.04.2018
 * @author Andreas Gebauer
 */
public class AllowAllPredicateFactory<T> extends NamedBean implements PredicateFactory<T> {

  @Override
  public Predicate apply(final Class<T> clazz) {
    return null;
  }

}
