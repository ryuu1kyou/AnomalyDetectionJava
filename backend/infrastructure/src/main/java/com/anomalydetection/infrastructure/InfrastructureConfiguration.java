package com.anomalydetection.infrastructure;

import com.anomalydetection.infrastructure.multitenancy.TenantAwareHibernateJpaDialect;
import java.util.List;
import java.util.Locale;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

@Configuration
@EnableCaching
@EnableJpaRepositories(basePackages = "com.anomalydetection.infrastructure")
public class InfrastructureConfiguration {

  @Bean
  public LocaleResolver localeResolver() {
    var resolver = new AcceptHeaderLocaleResolver();
    resolver.setSupportedLocales(List.of(Locale.ENGLISH, Locale.JAPANESE));
    resolver.setDefaultLocale(Locale.ENGLISH);
    return resolver;
  }

  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(
      DataSource dataSource, JpaProperties jpaProperties) {
    var adapter = new HibernateJpaVendorAdapter();
    adapter.setShowSql(jpaProperties.isShowSql());

    var factory = new LocalContainerEntityManagerFactoryBean();
    factory.setDataSource(dataSource);
    factory.setJpaVendorAdapter(adapter);
    factory.setJpaDialect(new TenantAwareHibernateJpaDialect());
    factory.setPackagesToScan(
        "com.anomalydetection.domain",
        "com.anomalydetection.infrastructure");
    factory.setJpaPropertyMap(jpaProperties.getProperties());
    return factory;
  }

  @Bean
  public PlatformTransactionManager transactionManager(
      LocalContainerEntityManagerFactoryBean emf) {
    return new JpaTransactionManager(emf.getObject());
  }
}
