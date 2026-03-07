/* (c) 2026 Alejandro Lopez Monzon <alejandro@kreaker.dev> for Kreaker Developments */
package dev.kreaker.cnc.config;

import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.dialect.DatabaseVersion;
import org.hibernate.dialect.OracleDialect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;

@Configuration
public class HibernateConfig {

  @Primary
  @Bean
  public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
    LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
    em.setDataSource(dataSource);
    em.setPackagesToScan("dev.kreaker.cnc.domain.entity");

    HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
    em.setJpaVendorAdapter(vendorAdapter);

    Properties properties = new Properties();
    // Force Oracle 11g dialect (uses ROWNUM instead of FETCH FIRST)
    properties.setProperty("hibernate.dialect", Oracle11gDialect.class.getName());
    properties.setProperty("hibernate.hbm2ddl.auto", "none");
    properties.setProperty("hibernate.show_sql", "true");
    properties.setProperty("hibernate.format_sql", "true");
    properties.setProperty("hibernate.default_schema", "REPORTUSER");

    em.setJpaProperties(properties);

    return em;
  }

  @Primary
  @Bean
  public PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
    JpaTransactionManager transactionManager = new JpaTransactionManager();
    transactionManager.setEntityManagerFactory(entityManagerFactory);
    return transactionManager;
  }

  /**
   * Custom Oracle 11g Dialect to force ROWNUM-based pagination
   */
  @Slf4j
  public static class Oracle11gDialect extends OracleDialect {
    public Oracle11gDialect() {
      // Force Oracle 11g version (11.2)
      super(DatabaseVersion.make(11, 2));
      log.info("========================================");
      log.info("CUSTOM ORACLE 11G DIALECT LOADED");
      log.info("Version: {}", getVersion());
      log.info("========================================");
    }

    @Override
    public DatabaseVersion getVersion() {
      // Ensure version is always 11.2
      return DatabaseVersion.make(11, 2);
    }
  }
}
