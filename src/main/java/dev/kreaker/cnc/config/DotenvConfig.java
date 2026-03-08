/* (c) 2026 Alejandro Lopez Monzon <alejandro@kreaker.dev> for Kreaker Developments */
package dev.kreaker.cnc.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

   @Override
   public void initialize(ConfigurableApplicationContext applicationContext) {
      ConfigurableEnvironment environment = applicationContext.getEnvironment();

      try {
         // Try to load .env file from root directory
         Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

         Map<String, Object> dotenvMap = new HashMap<>();

         // Load all entries from .env into a map
         dotenv.entries().forEach(entry -> {
            dotenvMap.put(entry.getKey(), entry.getValue());
            // Also set as system property for compatibility
            System.setProperty(entry.getKey(), entry.getValue());
         });

         // Add to Spring environment with high priority
         environment.getPropertySources()
                  .addFirst(new MapPropertySource("dotenvProperties", dotenvMap));

         log.info("✓ Loaded .env file with {} properties", dotenvMap.size());

      } catch (Exception e) {
         log.info("⚠ Could not load .env file: {}", e.getMessage());
         log.info("  Using default values from application.properties");
      }
   }
}
