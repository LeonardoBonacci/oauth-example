package guru.bonacci.oauth;

import java.util.UUID;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.resource.introspection.NimbusOpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class ServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServerApplication.class, args);
	}

	@Bean
	public RegisteredClientRepository registeredClientRepository() {
		RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
			.clientId("my-client")
			.clientSecret("{noop}my-secret")
			.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
			.scope("read")
			.build();

		return new InMemoryRegisteredClientRepository(client);
	}

	@Bean
	public AuthorizationServerSettings authorizationServerSettings() {
		return AuthorizationServerSettings.builder()
			.issuer("http://localhost:9000")
			.build();
	}

	@Bean
	public OAuth2AuthorizationService authorizationService(RegisteredClientRepository clients) {
		return new InMemoryOAuth2AuthorizationService();
	}

	// 1. Security filter chain for Authorization Server endpoints
	@Bean
	@Order(1)  // Make sure this filter chain runs first
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
		OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
		return http.build();
	}

	// 2. Security filter chain for resource server endpoints (/hello)
	@Bean
	@Order(2)  // Runs after the Authorization Server chain
	public SecurityFilterChain resourceServerSecurityFilterChain(HttpSecurity http) throws Exception {
		http
			.securityMatcher("/hello")
			.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
			.oauth2ResourceServer(OAuth2ResourceServerConfigurer::opaqueToken);

		return http.build();
	}
	
	@Bean
	public OpaqueTokenIntrospector introspector() {
	    return new NimbusOpaqueTokenIntrospector(
	      "http://localhost:9000/oauth2/introspect",
	      "my-client",
	      "my-secret"
	    );
	}

	@RestController
	public static class ApiController {
		@GetMapping("/hello")
		public String hello() {
			return "Hello, secured resource!";
		}
	}
}
