package dev.kreaker.cnc.security.repository;

import dev.kreaker.cnc.security.model.CncUser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {

	private final JdbcTemplate jdbcTemplate;

	private static final RowMapper<CncUser> USER_ROW_MAPPER = (rs, rowNum) -> CncUser.builder()
			.id(rs.getLong("id"))
			.username(rs.getString("username"))
			.email(rs.getString("email"))
			.password(rs.getString("password"))
			.displayName(rs.getString("display_name"))
			.enabled(rs.getInt("enabled") == 1)
			.createdAt(rs.getString("created_at"))
			.updatedAt(rs.getString("updated_at"))
			.build();

	public UserRepository(@Qualifier("sqliteJdbcTemplate") JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Optional<CncUser> findByUsername(String username) {
		var users = jdbcTemplate.query(
				"SELECT * FROM users WHERE username = ?",
				USER_ROW_MAPPER,
				username
		);
		return users.stream().findFirst();
	}

	public boolean existsByUsername(String username) {
		var count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM users WHERE username = ?",
				Integer.class,
				username
		);
		return count != null && count > 0;
	}

	public boolean existsByEmail(String email) {
		var count = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM users WHERE email = ?",
				Integer.class,
				email
		);
		return count != null && count > 0;
	}

	public void save(CncUser user) {
		jdbcTemplate.update(
				"INSERT INTO users (username, email, password, display_name, enabled) VALUES (?, ?, ?, ?, ?)",
				user.getUsername(),
				user.getEmail(),
				user.getPassword(),
				user.getDisplayName(),
				user.isEnabled() ? 1 : 0
		);
	}
}
