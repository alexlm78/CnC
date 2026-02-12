package dev.kreaker.cnc.config;

import dev.kreaker.cnc.security.model.CncUser;
import dev.kreaker.cnc.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public void run(String... args) {
		if (!userRepository.existsByUsername("kreaker")) {
			var user = CncUser.builder()
					.username("kreaker")
					.email("alejandro@kreaker.dev")
					.password(passwordEncoder.encode("kreaker123"))
					.displayName("Kreaker")
					.enabled(true)
					.build();
			userRepository.save(user);
			log.info("Seed user 'kreaker' created successfully.");
		} else {
			log.info("Seed user 'kreaker' already exists, skipping.");
		}
	}
}
