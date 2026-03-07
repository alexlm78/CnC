/* (c) 2026 Alejandro Lopez Monzon <alejandro@kreaker.dev> for Kreaker Developments */
package dev.kreaker.cnc.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import dev.kreaker.cnc.domain.model.CatalogSource;

@Configuration
public class WebConfig implements WebMvcConfigurer {

  @Override
  public void addViewControllers(ViewControllerRegistry registry) {
    registry.addRedirectViewController("/", "/catalogs");
  }

  @Override
  public void addFormatters(FormatterRegistry registry) {
    registry.addConverter(new StringToCatalogSourceConverter());
  }

  public static class StringToCatalogSourceConverter implements Converter<String, CatalogSource> {
    @Override
    public CatalogSource convert(String source) {
      return CatalogSource.valueOf(source.toUpperCase());
    }
  }
}
