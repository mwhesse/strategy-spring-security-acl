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

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Collection;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.github.lothar.security.acl.SimpleAclStrategy;
import com.querydsl.core.types.Predicate;
import com.skytron.security.acl.querydsl.QuerydslPredicateFeature;
import com.skytron.security.acl.querydsl.QuerydslPredicateTestConfiguration;
import com.skytron.security.acl.querydsl.domain.Customer;
import com.skytron.security.acl.querydsl.domain.QCustomer;
import com.skytron.security.acl.querydsl.predicate.CustomerPredicateFactory;
import com.skytron.security.acl.querydsl.predicate.PredicateFactory;

/**
 * Test {@link CustomerRepository} with {@link CustomerPredicateFactory} installed. Only Smith family should be visible.
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = QuerydslPredicateTestConfiguration.class)
@Transactional
public class CustomerRepositoryTest {

  private final Logger logger = LoggerFactory.getLogger(getClass());
  @Resource
  private CustomerRepository repository;
  @Resource
  private SimpleAclStrategy customerStrategy;
  @Resource
  private QuerydslPredicateFeature<Customer> querydslPredicateFeature;
  @Resource
  private CustomerPredicateFactory customerPredicateFactory;
  private Customer aliceSmith;
  private Customer bobSmith;
  private Customer johnDoe;

  @Before
  public void init() {
    aliceSmith = repository.save(new Customer("Alice", "Smith"));
    bobSmith = repository.save(new Customer("Bob", "Smith"));
    johnDoe = repository.save(new Customer("John", "Doe"));
    logger.info("Customer strategy : {}", customerStrategy);
  }

  @Test
  public void should_customer_spec_be_registered_in_customer_strategy() {
    final PredicateFactory<Customer> customerPredicateFactory = customerStrategy.handlerFor(querydslPredicateFeature);
    assertThat(customerPredicateFactory) //
        .as("Customer ACL JPA specification not registered") //
        .isNotNull();
  }

  // count

  @Test
  public void should_count_authorized_customers_only_when_strategy_applied() {
    assertThat(repository.count((Predicate) null)).isEqualTo(2);
  }

  @Test
  public void should_count_all_customers_only_when_strategy_not_applied() {
    doWithoutCustomerSpec(new Runnable() {
      @Override
      public void run() {
        assertThat(repository.count((Predicate) null)).isEqualTo(3);
      }
    });
  }

  @Test
  public void should_not_count_members_of_Doe_family_with_method_query() {
    assertThat(repository.count(QCustomer.customer.lastName.eq("Doe"))).isEqualTo(0);
  }

  @Test
  public void should_count_members_of_Smith_family_with_method_query() {
    assertThat(repository.count(QCustomer.customer.lastName.eq("Smith"))).isEqualTo(2);
  }

  // acl disabled on specific query

  @Test
  public void should_count_all_customers_when_NoAcl_annotation_is_present() {
    assertThat(repository.countByLastNameContains("Doe")).isEqualTo(1);
    assertThat(repository.countByLastNameContains("Smith")).isEqualTo(2);
  }

  // exist

  @Test
  public void should_not_say_exist_members_of_Doe_family_with_method_query() {
    assertThat(repository.exists(QCustomer.customer.id.eq(johnDoe.getId()))).isFalse();
  }

  @Test
  public void should_say_exist_members_of_Smith_family_with_method_query() {
    assertThat(repository.exists(QCustomer.customer.id.eq(aliceSmith.getId()))).isTrue();
  }

  // findAll

  @Test
  public void should_find_authorized_customers_only_when_strategy_applied() {
    assertThat(repository.findAll((Predicate) null)).containsOnly(aliceSmith, bobSmith);
  }

  @Test
  public void should_find_all_customers_only_when_strategy_not_applied() {
    doWithoutCustomerSpec(new Runnable() {
      @Override
      public void run() {
        assertThat(repository.findAll()).containsOnly(aliceSmith, bobSmith, johnDoe);
      }
    });
  }

  @Test
  public void should_find_authorized_customers_using_specific_ids_only_when_strategy_applied() {
    assertThat(repository.findAll(QCustomer.customer.id.in(customerIds()))).containsOnly(aliceSmith, bobSmith);
  }

  @Test
  public void should_find_all_customers_using_specific_ids_only_when_strategy_not_applied() {
    doWithoutCustomerSpec(new Runnable() {
      @Override
      public void run() {
        assertThat(repository.findAllById(customerIds())).containsOnly(aliceSmith, bobSmith, johnDoe);
      }
    });
  }

  // findByLastName

  @Test
  public void should_not_find_members_of_Doe_family_with_method_query() {
    assertThat(repository.findAll(QCustomer.customer.lastName.eq("Doe"))).isEmpty();
  }

  @Test
  public void should_find_members_of_Smith_family_with_method_query() {
    assertThat(repository.findAll(QCustomer.customer.lastName.eq("Smith"))).containsOnly(aliceSmith, bobSmith);
  }

  @Test
  public void should_create_predicate_when_query_method_invoked() {
    final PredicateFactory<Customer> customerPredicateFactory = customerStrategy.handlerFor(querydslPredicateFeature);
    final PredicateFactory<Customer> spy = Mockito.spy(customerPredicateFactory);
    customerStrategy.install(querydslPredicateFeature, spy);
    try {
      repository.count(QCustomer.customer.lastName.eq("Smith"));
      repository.findOne(QCustomer.customer.firstName.eq("John"));
      repository.findAll(QCustomer.customer.lastName.eq("Smith"));
      verify(spy, times(3)).apply(Customer.class);
    } finally {
      customerStrategy.install(querydslPredicateFeature, customerPredicateFactory);
    }
  }

  @Test
  public void should_handle_dynamic_specifications() {
    assertThat(repository.findAll(QCustomer.customer.lastName.eq("Smith"))).hasSize(2);
    assertThat(repository.findAll(QCustomer.customer.lastName.eq("Doe"))).hasSize(0);
    customerPredicateFactory.setLastName("Doe");
    try {
      assertThat(repository.findAll(QCustomer.customer.lastName.eq("Smith"))).hasSize(0);
      assertThat(repository.findAll(QCustomer.customer.lastName.eq("Doe"))).hasSize(1);
    } finally {
      customerPredicateFactory.setLastName("Smith");
    }
  }

  // findBy ... with Sort

  @Test
  public void should_find_members_of_Smith_family_with_sortable_query_method() {
    assertThat(repository.findAll(QCustomer.customer.firstName.contains("o"), Sort.by("id"))).containsOnly(bobSmith);
  }

  @Test
  public void should_find_members_of_Smith_family_with_pageable_query_method() {
    assertThat(repository.findAll(QCustomer.customer.firstName.contains("o"), PageRequest.of(0, 10)))
        .containsOnly(bobSmith);
  }

  // utils

  private void doWithoutCustomerSpec(final Runnable runnable) {
    final PredicateFactory<Customer> customerSpec = customerStrategy.uninstall(querydslPredicateFeature);
    try {
      runnable.run();
    } finally {
      customerStrategy.install(querydslPredicateFeature, customerSpec);
    }
  }

  private Collection<String> customerIds() {
    return asList(aliceSmith.getId(), bobSmith.getId(), johnDoe.getId());
  }

}
