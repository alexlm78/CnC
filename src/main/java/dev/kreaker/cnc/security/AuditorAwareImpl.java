package dev.kreaker.cnc.security;

import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuditorAwareImpl implements AuditorAware<String> {

	@Override
	public Optional<String> getCurrentAuditor() {
		// TODO: Implement actual user retrieval from security context
		// For now, return a default user
		// In production, this should integrate with Spring Security

		// Example with Spring Security:
		// return Optional.ofNullable(SecurityContextHolder.getContext())
		//     .map(SecurityContext::getAuthentication)
		//     .filter(Authentication::isAuthenticated)
		//     .map(Authentication::getName);

		return Optional.of("SYSTEM");
	}
}
