package fr.mypopote.my_popote_api.security;

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
 * Tests HTTP de la fondation Spring Security.
 *
 * Le contexte est limité à la couche web afin de tester
 * les règles de sécurité sans accéder à MariaDB.
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

    /**
     * Les dépendances du contrôleur sont simulées car ce test
     * porte uniquement sur les règles de sécurité HTTP.
     */
    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CurrentUserService currentUserService;

    /**
     * Un utilisateur non authentifié ne doit pas pouvoir
     * accéder aux informations du compte courant.
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