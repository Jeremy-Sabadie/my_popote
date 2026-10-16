
package fr.mypopote.my_popote_api.auth;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Vérifie que les JWT de My Popote sont également
 * utilisables pour accéder à ProductPulse.
 */
class JwtProductPulseCompatibilityTest {

    private static final String ISSUER = "my-popote-test";
    private static final String MY_POPOTE_AUDIENCE = "mypopote";
    private static final String PRODUCTPULSE_AUDIENCE = "productpulse";

    private static JwtService jwtService;

    @BeforeAll
    static void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        KeyPair keyPair = generator.generateKeyPair();

        jwtService = new JwtService(
                (RSAPrivateKey) keyPair.getPrivate(),
                (RSAPublicKey) keyPair.getPublic(),
                ISSUER,
                MY_POPOTE_AUDIENCE
        );
    }

    /**
     * Le jeton doit conserver l'accès à My Popote.
     */
    @Test
    void shouldKeepMyPopoteAudience() {
        String token = jwtService.generateToken(
                42L,
                "user@example.com"
        );

        Jwt decoded = jwtService.decodeToken(token);

        assertTrue(decoded.getAudience().contains(MY_POPOTE_AUDIENCE));
        assertEquals("42", decoded.getSubject());
    }

    /**
     * Le même jeton doit également autoriser ProductPulse.
     */
    @Test
    void shouldIncludeProductPulseAudience() {
        String token = jwtService.generateToken(
                42L,
                "user@example.com"
        );

        Jwt decoded = jwtService.decodeToken(token);

        assertTrue(
                decoded.getAudience().contains(PRODUCTPULSE_AUDIENCE),
                "Le JWT doit inclure l'audience productpulse"
        );
    }
}
