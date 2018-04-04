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
package com.skytron.security.acl.querydsl.config;

import static org.assertj.core.api.Assertions.assertThat;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.skytron.security.acl.querydsl.QuerydslPredicateFeature;
import com.skytron.security.acl.querydsl.QuerydslPredicateTestConfiguration;
import com.skytron.security.acl.querydsl.predicate.PredicateFactory;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = QuerydslPredicateTestConfiguration.class)
public class QuerydslPredicateAclConfigurationTest<T> {

  @Resource
  private PredicateFactory<T> allowAllPredicateFactory;
  @Resource
  private PredicateFactory<T> denyAllPredicateFactory;
  @Resource
  private QuerydslPredicateFeature<?> querydslPredicateFeature;

  @Test
  public void should_querydslPredicateFeature_be_loaded() {
    assertThat(querydslPredicateFeature).isNotNull();
  }

  @Test
  public void should_allowAllPredicateFactory_be_loaded() {
    assertThat(allowAllPredicateFactory).isNotNull();
  }

  @Test
  public void should_denyAllPredicateFactory_be_loaded() {
    assertThat(denyAllPredicateFactory).isNotNull();
  }

}
