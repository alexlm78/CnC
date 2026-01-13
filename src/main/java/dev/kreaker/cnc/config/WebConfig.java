package dev.kreaker.cnc.config;

import dev.kreaker.cnc.domain.model.CatalogSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addViewControllers(@NonNull ViewControllerRegistry registry) {
		registry.addRedirectViewController("/", "/catalogs");
	}

	@Override
	public void addFormatters(@NonNull FormatterRegistry registry) {
		registry.addConverter(new StringToCatalogSourceConverter());
	}

	public static class StringToCatalogSourceConverter implements Converter<String, CatalogSource> {
		@Override
		public CatalogSource convert(@NonNull String source) {
			return CatalogSource.valueOf(source.toUpperCase());
		}
	}
}
