
package fr.mypopote.my_popote_api.auth;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests unitaires du service JWT RS256 de My Popote.
 */
class JwtServiceTest {

    private static final String ISSUER = "https://auth.my-popote.example";
    private static final String AUDIENCE = "mypopote";

    private static JwtService jwtService;

    /**
     * Prépare une paire RSA temporaire.
     */
    @BeforeAll
    static void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        KeyPair keyPair = generator.generateKeyPair();

        jwtService = new JwtService(
                (RSAPrivateKey) keyPair.getPrivate(),
                (RSAPublicKey) keyPair.getPublic(),
                ISSUER,
                AUDIENCE
        );
    }

    /**
     * Vérifie l'identité, l'émetteur, l'audience
     * et la durée de validité du jeton.
     */
    @Test
    void shouldGenerateTokenContainingUserIdentity() {
        String token = jwtService.generateToken(
                42L,
                "jeremy@example.com"
        );

        Jwt decodedToken = jwtService.decodeToken(token);

        assertThat(decodedToken.getSubject())
                .isEqualTo("42");

        assertThat(decodedToken.getClaimAsString("email"))
                .isEqualTo("jeremy@example.com");

        assertThat(decodedToken.getIssuer().toString())
                .isEqualTo(ISSUER);

        assertThat(decodedToken.getAudience())
                .contains(AUDIENCE);

        assertThat(decodedToken.getIssuedAt())
                .isNotNull();

        assertThat(decodedToken.getExpiresAt())
                .isNotNull()
                .isAfter(decodedToken.getIssuedAt());
    }

    /**
     * Vérifie l'absence de données de mot de passe.
     */
    @Test
    void shouldNotExposePasswordInformation() {
        String token = jwtService.generateToken(
                42L,
                "jeremy@example.com"
        );

        Jwt decodedToken = jwtService.decodeToken(token);

        assertThat(decodedToken.getClaims())
                .doesNotContainKeys("password", "passwordHash");
    }
}
