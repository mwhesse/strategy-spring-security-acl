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

import static java.util.Arrays.asList;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextBeforeModesTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;

import com.querydsl.core.types.Predicate;
import com.skytron.security.acl.querydsl.domain.Customer;
import com.skytron.security.acl.querydsl.domain.QCustomer;
import com.skytron.security.acl.querydsl.repository.CustomerRepository;

/**
 * Test ACL JPA {@link Specification} in multithread environment
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = MultithreadTestConfiguration.class)
@Transactional
@TestExecutionListeners({ TestDataPreparer.class,
    DirtiesContextBeforeModesTestExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class })
public class MultithreadCustomerRepositoryTest {

  @Resource
  private CustomerRepository repository;
  private Customer aliceSmith;
  private Customer bobSmith;
  private Customer johnDoe;
  private ExecutorService executor;
  private Collection<Callable<String>> tasks;
  private static final int TIMES = 100;

  private final QCustomer customer = QCustomer.customer;

  @Before
  public void init() {
    aliceSmith = repository.findOne(customer.firstName.eq("Alice")).orElse(null);
    bobSmith = repository.findOne(customer.firstName.eq("Bob")).orElse(null);
    johnDoe = repository.findOne(customer.firstName.eq("John")).orElse(null);

    executor = newFixedThreadPool(8);
    tasks = new ArrayList<>();
  }

  // count

  @Test
  public void should_count_authorized_customers() throws InterruptedException {
    prepareTaskAs(() -> assertThat(repository.count((Predicate) null)).isEqualTo(2), "Smith");
    prepareTaskAs(() -> assertThat(repository.count((Predicate) null)).isEqualTo(1), "Doe");
    waitForAssertions();
  }

  // exist

  @Test
  public void should_test_customer_existance() throws InterruptedException {
    prepareTaskAs(() -> assertThat(repository.exists(customer.id.eq(johnDoe.getId()))).isFalse(), "Smith");
    prepareTaskAs(() -> assertThat(repository.exists(customer.id.eq(johnDoe.getId()))).isTrue(), "Doe");
    waitForAssertions();
  }

  // findAll

  @Test
  public void should_find_authorized_customers() throws InterruptedException {
    prepareTaskAs(() -> assertThat(repository.findAll((Predicate) null)).containsOnly(aliceSmith, bobSmith), "Smith");
    prepareTaskAs(() -> assertThat(repository.findAll((Predicate) null)).containsOnly(johnDoe), "Doe");
    waitForAssertions();
  }

  @Test
  public void should_find_authorized_customers_using_specific_ids() throws InterruptedException {
    prepareTaskAs(() -> assertThat(repository.findAll(customer.id.in(customerIds()))).containsOnly(aliceSmith,
        bobSmith), "Smith");
    prepareTaskAs(() -> assertThat(repository.findAll(customer.id.in(customerIds()))).containsOnly(johnDoe),
        "Doe");
    waitForAssertions();
  }

  // findOne with lastname predicate

  @Test
  public void should_find_with_method_query__lastName() throws InterruptedException {
    prepareTaskAs(() -> assertThat(repository.findAll(customer.lastName.eq("Doe"))).isEmpty(), "Smith");
    prepareTaskAs(() -> assertThat(repository.findAll(customer.lastName.eq("Doe"))).containsOnly(johnDoe), "Doe");
    waitForAssertions();
  }

  // findOne with firstname predicate

  @Test
  public void should_find_with_method_query__firstName() throws InterruptedException {
    prepareTaskAs(() -> assertThat(repository.findOne(customer.firstName.eq("John"))).isEmpty(), "Smith");
    prepareTaskAs(() -> assertThat(repository.findOne(customer.firstName.eq("John"))).contains(johnDoe), "Doe");
    prepareTaskAs(() -> assertThat(repository.findOne(customer.firstName.eq("Alice"))).contains(aliceSmith), "Smith");
    prepareTaskAs(() -> assertThat(repository.findOne(customer.firstName.eq("Alice"))).isEmpty(), "Doe");
    waitForAssertions();
  }

  // findOne

  @Test
  public void should_findOne_with_method_query() throws InterruptedException {
    prepareTaskAs(() -> assertThat(repository.findOne(customer.id.eq(johnDoe.getId()))).isEmpty(), "Smith");
    prepareTaskAs(() -> assertThat(repository.findOne(customer.id.eq(johnDoe.getId()))).contains(johnDoe), "Doe");
    prepareTaskAs(() -> assertThat(repository.findOne(customer.id.eq(aliceSmith.getId()))).contains(aliceSmith),
        "Smith");
    prepareTaskAs(() -> assertThat(repository.findOne(customer.id.eq(aliceSmith.getId()))).isEmpty(), "Doe");
    waitForAssertions();
  }

  // utils

  private Collection<String> customerIds() {
    return asList(aliceSmith.getId(), bobSmith.getId(), johnDoe.getId());
  }

  private void prepareTaskAs(final Runnable runnable, final String lastName) {
    range(0, TIMES).forEach(i -> {
      tasks.add(() -> {
        Session.login(lastName);
        try {
          runnable.run();
        } catch (final ComparisonFailure failure) {
          throw new ComparisonFailure(lastName + ": " + failure.getMessage(),
              failure.getExpected(), failure.getActual());
        } finally {
          Session.logout();
        }
        return lastName;
      });
    });
  }

  private void waitForAssertions() throws InterruptedException {
    final List<Future<String>> futures = executor.invokeAll(tasks);
    tasks.clear();

    boolean fail = false;
    String message = "";
    for (final Future<String> future : futures) {
      try {
        future.get();
      } catch (InterruptedException | ExecutionException e) {
        message += e.getMessage() + "\n";
        fail = true;
      }
    }
    if (fail) {
      fail(message);
    }
  }

}
