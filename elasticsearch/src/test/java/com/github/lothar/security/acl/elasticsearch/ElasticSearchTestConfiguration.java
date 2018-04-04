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
package com.github.lothar.security.acl.elasticsearch;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

import javax.annotation.PreDestroy;

import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.client.NodeClientFactoryBean;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import com.github.lothar.security.acl.AclStrategy;
import com.github.lothar.security.acl.SimpleAclStrategy;
import com.github.lothar.security.acl.elasticsearch.repository.AclElasticsearchRepositoryFactoryBean;

@SpringBootApplication
@EnableElasticsearchRepositories(value = "com.github.lothar.security.acl.elasticsearch.repository", repositoryFactoryBeanClass = AclElasticsearchRepositoryFactoryBean.class)
public class ElasticSearchTestConfiguration {

  private final SimpleAclStrategy customerStrategy = new SimpleAclStrategy();

  @Autowired
  private ElasticsearchProperties properties;

  @Autowired
  private Client nodeClientFactoryBean;

  @Bean
  public AclStrategy withoutHandlerStrategy() {
    return new SimpleAclStrategy();
  }

  @Bean
  public SimpleAclStrategy customerStrategy() {
    return customerStrategy;
  }

  @Bean
  public MatchQueryBuilder smithFamilyFilter(final ElasticSearchFeature elasticSearchFeature) {
    final MatchQueryBuilder smithFamilyFilter = matchQuery("lastName", "Smith");
    customerStrategy.install(elasticSearchFeature, smithFamilyFilter);
    return smithFamilyFilter;
  }

  @Bean
  public ElasticsearchTemplate elasticsearchTemplate(final Client nodeClientFactoryBean) {
    return new ElasticsearchTemplate(nodeClientFactoryBean);
  }

  @Bean
  public NodeClientFactoryBean nodeClientFactoryBean() {
    final NodeClientFactoryBean clientFactorybean = new NodeClientFactoryBean(true);
    clientFactorybean.setClusterName(properties.getClusterName());
    clientFactorybean.setEnableHttp(Boolean.valueOf(properties.getProperties().getOrDefault("http.enabled", "true")));
    clientFactorybean.setPathData(properties.getProperties().get("path.data"));
    clientFactorybean.setPathHome(properties.getProperties().get("path.home"));
    return clientFactorybean;
  }

  @PreDestroy
  public void destroy() {
    nodeClientFactoryBean.close();
  }

}
