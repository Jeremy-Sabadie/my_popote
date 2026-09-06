package fr.mypopote.my_popote_api.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires du service chargé de créer les JWT.
 *
 * Le token doit contenir uniquement les informations
 * nécessaires à l'identification de l'utilisateur.
 *
 * Le mot de passe et son hash ne doivent évidemment
 * jamais apparaître dans le JWT.
 */
class JwtServiceTest {

    /**
     * Clé utilisée uniquement pour les tests.
     *
     * En production, la vraie clé sera fournie
     * par une variable d'environnement.
     */
    private static final String TEST_SECRET =
        "my-popote-test-secret-key-12345678901234567890";

    @Test
    void shouldGenerateTokenContainingUserIdentity() {
        JwtService jwtService =
            JwtService.forSecret(TEST_SECRET);

        String token = jwtService.generateToken(
            42L,
            "jeremy@example.com"
        );

        Jwt decodedToken =
            jwtService.decodeToken(token);

        /*
         * Le subject contient l'identifiant technique du compte.
         * C'est cette valeur que nous utiliserons ensuite
         * pour retrouver l'utilisateur authentifié.
         */
        assertThat(decodedToken.getSubject())
            .isEqualTo("42");

        assertThat(
            decodedToken.getClaimAsString("email")
        )
        .isEqualTo("jeremy@example.com");

        /*
         * Un access token doit avoir une durée de vie limitée.
         */
        assertThat(decodedToken.getIssuedAt())
            .isNotNull();

        assertThat(decodedToken.getExpiresAt())
            .isNotNull();

        assertThat(decodedToken.getExpiresAt())
            .isAfter(decodedToken.getIssuedAt());
    }
}