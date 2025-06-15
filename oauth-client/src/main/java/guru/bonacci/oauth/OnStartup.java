package guru.bonacci.oauth;

import java.util.Map;

import org.springframework.boot.CommandLineRunner;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class OnStartup implements CommandLineRunner {

	private final RestClient restClient;

	public OnStartup(RestClient restClient) {
		this.restClient = restClient;
	}

	@Override
	public void run(String... args) throws Exception {
		try {
			String token = getAccessToken();

			String response = restClient.get().uri("http://localhost:9000/hello").headers(headers -> headers.setBearerAuth(token))
					.accept(MediaType.TEXT_PLAIN).retrieve().body(String.class);
			System.out.println(response);

		} catch (RestClientResponseException e) {
			System.out.println("Error: " + e.getRawStatusCode() + " " + e.getResponseBodyAsString());
		}
	}

	private String getAccessToken() {
		Map<String, Object> tokenResponse = restClient.post().uri("http://localhost:9000/oauth2/token").headers(headers -> {
			headers.setBasicAuth("my-client", "my-secret");
			headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		}).body("grant_type=client_credentials&scope=read").retrieve().body(Map.class);

		return (String) tokenResponse.get("access_token");
	}
}
