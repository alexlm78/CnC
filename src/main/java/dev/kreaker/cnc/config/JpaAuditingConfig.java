/* (c) 2026 Alejandro Lopez Monzon <alejandro@kreaker.dev> for Kreaker Developments */
package dev.kreaker.cnc.config;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import dev.kreaker.cnc.security.AuditorAwareImpl;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider", dateTimeProviderRef = "dateTimeProvider")
public class JpaAuditingConfig {

   @Bean
   public AuditorAware<String> auditorProvider() {
      return new AuditorAwareImpl();
   }

   @Bean
   public DateTimeProvider dateTimeProvider() {
      return () -> Optional.of(LocalDateTime.now());
   }
}
