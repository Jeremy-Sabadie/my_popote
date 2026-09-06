package fr.mypopote.my_popote_api.auth;

import fr.mypopote.my_popote_api.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie que Spring Security reconnaît réellement
 * les JWT produits par notre application.
 *
 * On ne teste pas ici une fonctionnalité métier précise :
 * on teste la frontière de sécurité HTTP.
 */
@WebMvcTest(
    properties = {
        "app.cors.allowed-origins=http://localhost:4200",
        "app.jwt.secret=my-popote-test-secret-key-12345678901234567890"
    }
)
@Import(SecurityConfig.class)
class JwtSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * Sans JWT, une route protégée doit rester inaccessible.
     */
    @Test
    void shouldRejectProtectedEndpointWithoutToken()
        throws Exception {

        mockMvc.perform(
            get("/api/recipes")
        )
        .andExpect(status().isUnauthorized());
    }

    /**
     * Un Bearer token invalide ne doit jamais
     * permettre d'accéder à une route protégée.
     */
    @Test
    void shouldRejectProtectedEndpointWithInvalidToken()
        throws Exception {

        mockMvc.perform(
            get("/api/recipes")
                .header(
                    "Authorization",
                    "Bearer invalid-token"
                )
        )
        .andExpect(status().isUnauthorized());
    }
}