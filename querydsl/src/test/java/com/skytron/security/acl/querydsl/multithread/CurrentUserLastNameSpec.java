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

import com.skytron.security.acl.querydsl.domain.Customer;
import com.skytron.security.acl.querydsl.domain.QCustomer;
import com.skytron.security.acl.querydsl.predicate.PredicateFactory;

public class CurrentUserLastNameSpec implements PredicateFactory<Customer> {

  @Override
  public com.querydsl.core.types.Predicate apply(final Class<Customer> t) {
    final String currentUserLastName = Session.currentUserLastName();
    if (currentUserLastName == null) {
      return null;
    } else {
      return QCustomer.customer.lastName.eq(currentUserLastName);
    }
  }
}
