
package se.magnus.microservices.composite.post;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import static org.springframework.http.HttpMethod.*;

@EnableWebFluxSecurity
public class SecurityConfig {

	@Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
		http
			.authorizeExchange()
				.pathMatchers("/actuator/**").permitAll()
				.pathMatchers(POST, "/post-composite/**").hasAuthority("SCOPE_post:write")
				.pathMatchers(DELETE, "/post-composite/**").hasAuthority("SCOPE_post:write")
				.pathMatchers(GET, "/post-composite/**").hasAuthority("SCOPE_post:read")
				.anyExchange().authenticated()
				.and()
			.oauth2ResourceServer()
				.jwt();
		return http.build();
	}
}
