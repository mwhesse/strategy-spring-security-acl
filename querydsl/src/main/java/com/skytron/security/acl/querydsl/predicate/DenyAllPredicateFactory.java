package com.skytron.security.acl.querydsl.predicate;

import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.Expressions;

public class DenyAllPredicateFactory<T> implements PredicateFactory<T> {

  @Override
  public Predicate apply(final Class<T> clazz) {
    return Expressions.asBoolean(true).isFalse();
  }

}
