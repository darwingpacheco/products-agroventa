package com.agro_venta.products.infrastructure.config;

import com.agro_venta.products.infrastructure.security.JwtAuthenticationFilter;
import com.agro_venta.products.infrastructure.security.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .authorizeExchange(exchanges -> exchanges
                        // Public: catalogue browsing (no auth needed)
                        .pathMatchers(HttpMethod.GET, "/api/agro-venta/v1/products/catalog").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/agro-venta/v1/products/{id}").permitAll()
                        // Actuator
                        .pathMatchers("/actuator/health", "/actuator/info").permitAll()
                        // Sellers only
                        .pathMatchers(HttpMethod.POST, "/api/agro-venta/v1/products/sell").hasRole("AGRICULTOR")
                        .pathMatchers(HttpMethod.PUT,  "/api/agro-venta/v1/products/{id}").hasRole("AGRICULTOR")
                        .pathMatchers(HttpMethod.PATCH,"/api/agro-venta/v1/products/{id}/status").hasRole("AGRICULTOR")
                        .pathMatchers(HttpMethod.GET,  "/api/agro-venta/v1/products/my-products").hasRole("AGRICULTOR")
                        .pathMatchers(HttpMethod.PATCH,"/api/agro-venta/v1/shipments/{productId}/dispatch").hasRole("AGRICULTOR")
                        // Buyers only
                        .pathMatchers(HttpMethod.POST, "/api/agro-venta/v1/orders").hasRole("COMPRADOR")
                        // Both roles
                        .pathMatchers(HttpMethod.GET,  "/api/agro-venta/v1/orders/my-orders").authenticated()
                        .pathMatchers(HttpMethod.GET,  "/api/agro-venta/v1/orders/{id}").authenticated()
                        // Payments
                        .pathMatchers("/api/agro-venta/v1/payments/**").authenticated()
                        // Wompi webhook is public (validated by signature internally)
                        .pathMatchers(HttpMethod.POST, "/api/agro-venta/v1/payments/webhook").permitAll()
                        .anyExchange().authenticated())
                .build();
    }
}
