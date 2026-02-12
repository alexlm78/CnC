package dev.kreaker.cnc.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SqliteSchemaInitializer {

	private final JdbcTemplate jdbcTemplate;

	public SqliteSchemaInitializer(@Qualifier("sqliteJdbcTemplate") JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@PostConstruct
	public void initSchema() {
		log.info("Initializing SQLite schema for user authentication...");
		jdbcTemplate.execute("""
				CREATE TABLE IF NOT EXISTS users (
					id INTEGER PRIMARY KEY AUTOINCREMENT,
					username TEXT NOT NULL UNIQUE,
					email TEXT NOT NULL UNIQUE,
					password TEXT NOT NULL,
					display_name TEXT,
					enabled INTEGER NOT NULL DEFAULT 1,
					created_at TEXT NOT NULL DEFAULT (datetime('now')),
					updated_at TEXT NOT NULL DEFAULT (datetime('now'))
				)
				""");
		log.info("SQLite schema initialized successfully.");
	}
}
