package fr.mypopote.my_popote_api.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Vérifie que l'identité utilisée par le backend
 * provient bien du sujet du JWT authentifié.
 */
class CurrentUserServiceTest {

    private final CurrentUserService currentUserService =
        new CurrentUserService();

    @Test
    void shouldReturnUserIdFromJwtSubject() {
        Jwt jwt = new Jwt(
            "test-token",
            Instant.now(),
            Instant.now().plusSeconds(1800),
            java.util.Map.of("alg", "HS256"),
            java.util.Map.of("sub", "42")
        );

        Long userId =
            currentUserService.getUserId(jwt);

        assertEquals(42L, userId);
    }
}