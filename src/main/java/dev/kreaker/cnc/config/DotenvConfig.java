package dev.kreaker.cnc.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

public class DotenvConfig implements ApplicationContextInitializer<ConfigurableApplicationContext> {

	@Override
	public void initialize(ConfigurableApplicationContext applicationContext) {
		ConfigurableEnvironment environment = applicationContext.getEnvironment();

		try {
			// Try to load .env file from root directory
			Dotenv dotenv = Dotenv.configure()
					.ignoreIfMissing()
					.load();

			Map<String, Object> dotenvMap = new HashMap<>();

			// Load all entries from .env into a map
			dotenv.entries().forEach(entry -> {
				dotenvMap.put(entry.getKey(), entry.getValue());
				// Also set as system property for compatibility
				System.setProperty(entry.getKey(), entry.getValue());
			});

			// Add to Spring environment with high priority
			environment.getPropertySources().addFirst(
					new MapPropertySource("dotenvProperties", dotenvMap)
			);

			System.out.println("✓ Loaded .env file with " + dotenvMap.size() + " properties");

		} catch (Exception e) {
			System.out.println("⚠ Could not load .env file: " + e.getMessage());
			System.out.println("  Using default values from application.properties");
		}
	}
}
