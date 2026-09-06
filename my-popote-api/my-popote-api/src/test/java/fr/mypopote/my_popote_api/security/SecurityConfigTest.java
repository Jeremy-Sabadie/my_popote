package fr.mypopote.my_popote_api.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires de la configuration de sécurité.
 *
 * Ces tests vérifient les règles techniques de base
 * sans démarrer Spring ni accéder à une base de données.
 */
class SecurityConfigTest {

    @Test
    void shouldEncodePasswordWithBCrypt() {
        SecurityConfig securityConfig =
            new SecurityConfig("http://localhost:4200");

        PasswordEncoder passwordEncoder =
            securityConfig.passwordEncoder();

        String encodedPassword =
            passwordEncoder.encode("my-password");

        // Le mot de passe stocké ne doit jamais être le mot de passe brut.
        assertThat(encodedPassword)
            .isNotEqualTo("my-password");

        assertThat(
            passwordEncoder.matches(
                "my-password",
                encodedPassword
            )
        ).isTrue();
    }

    @Test
    void shouldAllowAngularDevelopmentOrigin() {
        SecurityConfig securityConfig =
            new SecurityConfig("http://localhost:4200");

        CorsConfiguration corsConfiguration =
            securityConfig.buildCorsConfiguration();

        assertThat(corsConfiguration.getAllowedOrigins())
            .containsExactly("http://localhost:4200");

        assertThat(corsConfiguration.getAllowedMethods())
            .contains(
                "GET",
                "POST",
                "PUT",
                "PATCH",
                "DELETE",
                "OPTIONS"
            );

        assertThat(corsConfiguration.getAllowedHeaders())
            .contains(
                "Authorization",
                "Content-Type"
            );
    }

    @Test
    void shouldNotAllowEveryOrigin() {
        SecurityConfig securityConfig =
            new SecurityConfig("http://localhost:4200");

        CorsConfiguration corsConfiguration =
            securityConfig.buildCorsConfiguration();

        // Une API authentifiée ne doit pas accepter toutes les origines.
        assertThat(corsConfiguration.getAllowedOrigins())
            .doesNotContain("*");
    }
}