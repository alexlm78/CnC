package dev.kreaker.cnc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.nio.file.Path;

@Configuration
public class SqliteDataSourceConfig {

	@Value("${cnc.sqlite.path:data/cnc-users.db}")
	private String sqlitePath;

	@Bean("sqliteJdbcTemplate")
	public JdbcTemplate sqliteJdbcTemplate() {
		Path dbPath = Path.of(sqlitePath);
		dbPath.getParent().toFile().mkdirs();

		DriverManagerDataSource ds = new DriverManagerDataSource();
		ds.setDriverClassName("org.sqlite.JDBC");
		ds.setUrl("jdbc:sqlite:" + dbPath.toAbsolutePath());
		return new JdbcTemplate(ds);
	}
}
