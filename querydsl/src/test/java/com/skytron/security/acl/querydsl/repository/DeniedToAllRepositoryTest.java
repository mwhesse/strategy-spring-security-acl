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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.Predicate;
import com.skytron.security.acl.querydsl.QuerydslPredicateTestConfiguration;
import com.skytron.security.acl.querydsl.domain.DeniedToAllObject;
import com.skytron.security.acl.querydsl.predicate.CustomerPredicateFactory;

/**
 * Test {@link CustomerRepository} with {@link CustomerPredicateFactory} installed. Only Smith family should be visible.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = QuerydslPredicateTestConfiguration.class)
@Transactional
public class DeniedToAllRepositoryTest {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  @Resource
  private DeniedToAllRepository repository;
  private DeniedToAllObject deniedToAll;

  @Before
  public void init() {
    deniedToAll = repository.save(new DeniedToAllObject(1L));
  }

  @Test
  public void testNoAccess() {
    assertThat(repository.count((Predicate) null)).isEqualTo(0);
  }

}
