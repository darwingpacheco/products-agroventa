package com.agro_venta.products.infrastructure.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * HTTP client for the authentication service.
 * Used to fetch user profiles (name + email) for notification events.
 */
@Component
public class AuthServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceClient.class);

    private final WebClient webClient;

    public AuthServiceClient(
            WebClient.Builder builder,
            @Value("${app.auth.base-url:http://localhost:8080}") String authBaseUrl) {
        this.webClient = builder
                .baseUrl(authBaseUrl)
                .build();
    }

    /**
     * Fetch user profile from the auth service.
     * Returns a UserInfo with name and email, or empty on error.
     */
    public Mono<UserInfo> getUserInfo(UUID userId) {
        return webClient.get()
                .uri("/api/agro-venta/v1/auth/me")
                .header("X-User-Id", userId.toString())
                .retrieve()
                .bodyToMono(UserInfo.class)
                .onErrorResume(e -> {
                    log.warn("Could not fetch user profile for {}: {}", userId, e.getMessage());
                    return Mono.empty();
                });
    }

    public record UserInfo(
            String id,
            String name,
            String lastName,
            String phone,
            String email
    ) {
        public String fullName() {
            return (name != null ? name : "") + (lastName != null ? " " + lastName : "");
        }
    }
}
