
package fr.mypopote.my_popote_api.auth;

import fr.mypopote.my_popote_api.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie la protection HTTP des routes métier avec JWT RS256.
 */
@WebMvcTest(
        controllers = ProtectedTestController.class,
        properties = {
                "app.cors.allowed-origins=http://localhost:4200"
        }
)
@Import(SecurityConfig.class)
class JwtSecurityTest {

    private static final String ISSUER = "https://auth.my-popote.example";
    private static final String AUDIENCE = "mypopote";

    private static final JwtService JWT_SERVICE = createJwtService();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    /**
     * Refuse l'accès sans authentification.
     */
    @Test
    void shouldRejectProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/api/test/protected"))
                .andExpect(status().isUnauthorized());
    }

    /**
     * Refuse un Bearer token invalide.
     */
    @Test
    void shouldRejectProtectedEndpointWithInvalidToken() throws Exception {
        when(jwtDecoder.decode("invalid-token"))
                .thenAnswer(invocation ->
                        JWT_SERVICE.decodeToken("invalid-token")
                );

        mockMvc.perform(
                        get("/api/test/protected")
                                .header("Authorization", "Bearer invalid-token")
                )
                .andExpect(status().isUnauthorized());
    }

    /**
     * Autorise un JWT RS256 correctement signé.
     */
    @Test
    void shouldAllowProtectedEndpointWithValidToken() throws Exception {
        String token = JWT_SERVICE.generateToken(
                42L,
                "jeremy@example.com"
        );

        when(jwtDecoder.decode(anyString()))
                .thenAnswer(invocation ->
                        JWT_SERVICE.decodeToken(invocation.getArgument(0))
                );

        mockMvc.perform(
                        get("/api/test/protected")
                                .header("Authorization", "Bearer " + token)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("protected"));
    }

    /**
     * Prépare le service JWT de test avec une paire RSA temporaire.
     */
    private static JwtService createJwtService() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);

            KeyPair keyPair = generator.generateKeyPair();

            return new JwtService(
                    (RSAPrivateKey) keyPair.getPrivate(),
                    (RSAPublicKey) keyPair.getPublic(),
                    ISSUER,
                    AUDIENCE
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Impossible de préparer les clés RSA de test",
                    exception
            );
        }
    }
}
