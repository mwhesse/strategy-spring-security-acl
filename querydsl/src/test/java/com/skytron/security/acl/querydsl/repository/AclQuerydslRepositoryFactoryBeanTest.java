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
package com.skytron.security.acl.querydsl.repository;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.skytron.security.acl.querydsl.QuerydslPredicateProvider;
import com.skytron.security.acl.querydsl.QuerydslPredicateTestConfiguration;
import com.skytron.security.acl.querydsl.domain.AllowedToAllObject;
import com.skytron.security.acl.querydsl.domain.DeniedToAllObject;
import com.skytron.security.acl.querydsl.domain.NoAclObject;
import com.skytron.security.acl.querydsl.domain.NoStrategyObject;
import com.skytron.security.acl.querydsl.domain.UnknownStrategyObject;
import com.skytron.security.acl.querydsl.domain.WithoutHandlerObject;
import com.skytron.security.acl.querydsl.predicate.AllowAllPredicateFactory;
import com.skytron.security.acl.querydsl.predicate.DenyAllPredicateFactory;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = QuerydslPredicateTestConfiguration.class)
public class AclQuerydslRepositoryFactoryBeanTest {

  @Resource
  private QuerydslPredicateProvider<Object> querydslPredicateProvider;
  @Resource
  private AllowAllPredicateFactory<Object> allowAllPredicate;
  @Resource
  private DenyAllPredicateFactory<Object> denyAllPredicate;

  @Test
  public void should_provider_return_allowAll_spec() {
    assertThat(querydslPredicateProvider.predicateFor(AllowedToAllObject.class)).isSameAs(allowAllPredicate);
  }

  @Test
  public void should_provider_return_denyAll_spec() {
    assertThat(querydslPredicateProvider.predicateFor(DeniedToAllObject.class)).isSameAs(denyAllPredicate);
  }

  @Test
  public void should_provider_return_allowAll_spec_for_noAcl() {
    assertThat(querydslPredicateProvider.predicateFor(NoAclObject.class)).isSameAs(allowAllPredicate);
  }

  @Test
  public void should_provider_return_allowAll_spec_for_noStrategy() {
    assertThat(querydslPredicateProvider.predicateFor(NoStrategyObject.class)).isSameAs(allowAllPredicate);
  }

  @Test
  public void should_provider_return_allowAll_spec_for_unknownStrategy() {
    assertThat(querydslPredicateProvider.predicateFor(UnknownStrategyObject.class)).isSameAs(allowAllPredicate);
  }

  @Test
  public void should_provider_return_allowAll_spec_for_withoutHandlerStrategy() {
    assertThat(querydslPredicateProvider.predicateFor(WithoutHandlerObject.class)).isSameAs(allowAllPredicate);
  }

}
