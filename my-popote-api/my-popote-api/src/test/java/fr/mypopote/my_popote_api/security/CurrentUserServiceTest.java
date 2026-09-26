package fr.mypopote.my_popote_api.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests du service permettant de récupérer
 * l'identité utilisateur depuis le JWT.
 */
class CurrentUserServiceTest {

    private final CurrentUserService currentUserService =
        new CurrentUserService();

    @Test
    void shouldReturnUserIdFromJwtSubject() {
        Jwt jwt = new Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "HS256"),
            Map.of("sub", "42")
        );

        Long userId =
            currentUserService.getUserId(jwt);

        assertThat(userId).isEqualTo(42L);
    }
}