
package fr.mypopote.my_popote_api.security;

import fr.mypopote.my_popote_api.auth.JwtService;
import fr.mypopote.my_popote_api.user.UserController;
import fr.mypopote.my_popote_api.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests HTTP de la configuration Spring Security.
 *
 * Les dépendances métier et JWT sont simulées.
 * Aucune connexion à MariaDB n'est nécessaire.
 */
@WebMvcTest(
        controllers = UserController.class,
        properties = {
                "app.cors.allowed-origins=http://localhost:4200"
        }
)
@Import(SecurityConfig.class)
class SecurityHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @MockitoBean
    private JwtService jwtService;

    /**
     * Un utilisateur non authentifié ne doit pas
     * accéder aux informations de son compte.
     */
    @Test
    void shouldRejectUnauthenticatedBusinessRequest()
            throws Exception {

        mockMvc.perform(
                        get("/api/users/me")
                )
                .andExpect(status().isUnauthorized());
    }
}
