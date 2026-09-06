package fr.mypopote.my_popote_api.auth;

import fr.mypopote.my_popote_api.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie la frontière de sécurité HTTP de l'application.
 *
 * Une route protégée doit :
 * - refuser une requête sans token ;
 * - refuser un token invalide ;
 * - accepter un JWT valide généré par My Popote.
 */
@WebMvcTest(
    controllers = ProtectedTestController.class,
    properties = {
        "app.cors.allowed-origins=http://localhost:4200",
        "app.jwt.secret=my-popote-test-secret-key-12345678901234567890"
    }
)
@Import(SecurityConfig.class)
class JwtSecurityTest {

    private static final String TEST_SECRET =
        "my-popote-test-secret-key-12345678901234567890";

    @Autowired
    private MockMvc mockMvc;

    /**
     * Sans authentification, une route métier
     * ne doit jamais être accessible.
     */
    @Test
    void shouldRejectProtectedEndpointWithoutToken()
        throws Exception {

        mockMvc.perform(
            get("/api/test/protected")
        )
        .andExpect(status().isUnauthorized());
    }

    /**
     * Un Bearer token invalide doit être rejeté.
     */
    @Test
    void shouldRejectProtectedEndpointWithInvalidToken()
        throws Exception {

        mockMvc.perform(
            get("/api/test/protected")
                .header(
                    "Authorization",
                    "Bearer invalid-token"
                )
        )
        .andExpect(status().isUnauthorized());
    }

    /**
     * Un JWT correctement signé doit permettre
     * l'accès à une route protégée.
     */
    @Test
    void shouldAllowProtectedEndpointWithValidToken()
        throws Exception {

        JwtService jwtService =
            JwtService.forSecret(TEST_SECRET);

        String token = jwtService.generateToken(
            42L,
            "jeremy@example.com"
        );

        mockMvc.perform(
            get("/api/test/protected")
                .header(
                    "Authorization",
                    "Bearer " + token
                )
        )
        .andExpect(status().isOk())
        .andExpect(content().string("protected"));
    }
}