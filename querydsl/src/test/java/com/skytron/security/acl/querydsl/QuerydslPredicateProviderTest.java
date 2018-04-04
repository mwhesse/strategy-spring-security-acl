/*******************************************************************************
 * Copyright 2002-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 *******************************************************************************/
package com.skytron.security.acl.querydsl;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.skytron.security.acl.querydsl.domain.NoStrategyObject;
import com.skytron.security.acl.querydsl.domain.UnknownStrategyObject;
import com.skytron.security.acl.querydsl.domain.WithoutHandlerObject;
import com.skytron.security.acl.querydsl.predicate.PredicateFactory;
import com.skytron.security.acl.querydsl.repository.AllowedToAllRepository;
import com.skytron.security.acl.querydsl.repository.DeniedToAllRepository;
import com.skytron.security.acl.querydsl.repository.NoAclRepository;
import com.skytron.security.acl.querydsl.repository.NoStrategyRepository;
import com.skytron.security.acl.querydsl.repository.UnknownStrategyRepository;
import com.skytron.security.acl.querydsl.repository.WithoutHandlerRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = QuerydslPredicateTestConfiguration.class)
public class QuerydslPredicateProviderTest {

  @Resource
  private QuerydslPredicateProvider<Object> querydslPredicateProvider;
  @Resource
  private PredicateFactory<?> defaultAclPredicateFactory;
  @Resource
  private AllowedToAllRepository allowedToAllRepository;
  @Resource
  private DeniedToAllRepository deniedToAllRepository;
  @Resource
  private NoAclRepository noAclRepository;
  @Resource
  private NoStrategyRepository noStrategyRepository;
  @Resource
  private UnknownStrategyRepository unknownStrategyRepository;
  @Resource
  private WithoutHandlerRepository withoutHandlerRepository;

  @Test
  public void should_all_acl_repositories_be_loaded() {
    assertThat(allowedToAllRepository).isNotNull();
    assertThat(deniedToAllRepository).isNotNull();
    assertThat(noAclRepository).isNotNull();
    assertThat(noStrategyRepository).isNotNull();
    assertThat(unknownStrategyRepository).isNotNull();
    assertThat(withoutHandlerRepository).isNotNull();
  }

  @Test
  public void should_use_default_handler_when_none_defined() {
    assertThat(querydslPredicateProvider.predicateFor(WithoutHandlerObject.class)).isSameAs(defaultAclPredicateFactory);
  }

  @Test
  public void should_use_default_handler_when_unknown_defined() {
    assertThat(querydslPredicateProvider.predicateFor(UnknownStrategyObject.class))
        .isSameAs(defaultAclPredicateFactory);
  }

  @Test
  public void should_use_default_handler_when_no_strategy_defined() {
    assertThat(querydslPredicateProvider.predicateFor(NoStrategyObject.class)).isSameAs(defaultAclPredicateFactory);
  }
}
