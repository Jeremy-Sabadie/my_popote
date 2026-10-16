
package fr.mypopote.my_popote_api.security;

import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires de la configuration CORS.
 */
class SecurityConfigTest {

    @Test
    void shouldAllowConfiguredOrigin() {
        SecurityConfig securityConfig =
                new SecurityConfig("http://localhost:4200");

        CorsConfiguration configuration =
                securityConfig.buildCorsConfiguration();

        assertThat(configuration.getAllowedOrigins())
                .containsExactly("http://localhost:4200");
    }

    @Test
    void shouldSupportMultipleConfiguredOrigins() {
        SecurityConfig securityConfig =
                new SecurityConfig(
                        "http://localhost:4200, https://my-popote.onrender.com"
                );

        CorsConfiguration configuration =
                securityConfig.buildCorsConfiguration();

        assertThat(configuration.getAllowedOrigins())
                .containsExactly(
                        "http://localhost:4200",
                        "https://my-popote.onrender.com"
                );
    }

    @Test
    void shouldConfigureAllowedMethodsAndHeaders() {
        SecurityConfig securityConfig =
                new SecurityConfig("http://localhost:4200");

        CorsConfiguration configuration =
                securityConfig.buildCorsConfiguration();

        assertThat(configuration.getAllowedMethods())
                .containsExactly(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                );

        assertThat(configuration.getAllowedHeaders())
                .containsExactly(
                        "Authorization",
                        "Content-Type"
                );

        assertThat(configuration.getAllowCredentials())
                .isFalse();

        assertThat(configuration.getExposedHeaders())
                .isEqualTo(List.of("Location"));
    }
}
