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
package com.skytron.security.acl.querydsl.multithread;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.github.lothar.security.acl.AclStrategy;
import com.github.lothar.security.acl.SimpleAclStrategy;
import com.skytron.security.acl.querydsl.QuerydslPredicateFeature;
import com.skytron.security.acl.querydsl.domain.Customer;
import com.skytron.security.acl.querydsl.predicate.AllowAllPredicateFactory;
import com.skytron.security.acl.querydsl.repository.AclQuerydslRepositoryFactoryBean;

@SpringBootApplication
@ComponentScan("com.skytron.security.acl.querydsl")
@EnableJpaRepositories(value = "com.skytron.security.acl.querydsl.repository", repositoryFactoryBeanClass = AclQuerydslRepositoryFactoryBean.class)
public class MultithreadTestConfiguration {

  @Resource
  private SimpleAclStrategy allowAllStrategy;
  @Resource
  private AllowAllPredicateFactory<?> allowAllPredicateFactory;
  @Resource
  private QuerydslPredicateFeature<Customer> querydslPredicateFeature;
  private final SimpleAclStrategy customerStrategy = new SimpleAclStrategy();
  private final CurrentUserLastNameSpec currentUserLastNameSpec = new CurrentUserLastNameSpec();

  @Bean
  public AclStrategy withoutHandlerStrategy() {
    return new SimpleAclStrategy();
  }

  @Bean
  public AclStrategy customerStrategy() {
    return customerStrategy;
  }

  @Bean
  public CurrentUserLastNameSpec currentUserLastNameSpec() {
    return currentUserLastNameSpec;
  }

  @PostConstruct
  public void installStrategy() {
    customerStrategy.install(querydslPredicateFeature, currentUserLastNameSpec);
  }
}
