package fr.mypopote.my_popote_api.auth;

import fr.mypopote.my_popote_api.security.SecurityConfig;
import fr.mypopote.my_popote_api.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests HTTP du contrôleur d'authentification.
 *
 * On vérifie ici :
 * - le contrat REST de l'inscription ;
 * - le contrat REST de la connexion ;
 * - la validation des données reçues ;
 * - le fait que les routes d'authentification restent publiques.
 *
 * AuthService est simulé car sa logique métier est testée séparément.
 */
@WebMvcTest(
    controllers = AuthController.class,
    properties = {
        "app.cors.allowed-origins=http://localhost:4200"
    }
)
@Import(SecurityConfig.class)
class AuthControllerTest {

    /**
     * MockMvc simule des requêtes HTTP sans démarrer
     * un véritable serveur web.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Le service est simulé afin que ces tests restent
     * concentrés sur le comportement de la couche HTTP.
     */
    @MockitoBean
    private AuthService authService;

    /**
     * Une inscription valide doit créer le compte
     * et retourner HTTP 201 Created.
     */
    @Test
    void shouldRegisterUser() throws Exception {
        User registeredUser = new User(
            "jeremy@example.com",
            "encoded-password",
            "Jérémy"
        );

        when(
            authService.register(
                "jeremy@example.com",
                "secret123",
                "Jérémy"
            )
        ).thenReturn(registeredUser);

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "jeremy@example.com",
                      "password": "secret123",
                      "firstName": "Jérémy"
                    }
                    """)
        )
        .andExpect(status().isCreated())
        .andExpect(
            jsonPath("$.email")
                .value("jeremy@example.com")
        )
        .andExpect(
            jsonPath("$.firstName")
                .value("Jérémy")
        );
    }

    /**
     * Une connexion valide doit retourner HTTP 200.
     *
     * Le futur mécanisme d'authentification réel
     * sera ajouté après cette fondation.
     */
    @Test
    void shouldLoginUser() throws Exception {
        User authenticatedUser = new User(
            "jeremy@example.com",
            "encoded-password",
            "Jérémy"
        );

        when(
            authService.authenticate(
                "jeremy@example.com",
                "secret123"
            )
        ).thenReturn(authenticatedUser);

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "jeremy@example.com",
                      "password": "secret123"
                    }
                    """)
        )
        .andExpect(status().isOk())
        .andExpect(
            jsonPath("$.email")
                .value("jeremy@example.com")
        )
        .andExpect(
            jsonPath("$.firstName")
                .value("Jérémy")
        );
    }

    /**
     * Une adresse email incorrecte doit être rejetée
     * avant même d'appeler la couche métier.
     */
    @Test
    void shouldRejectRegistrationWithInvalidEmail()
        throws Exception {

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "invalid-email",
                      "password": "secret123",
                      "firstName": "Jérémy"
                    }
                    """)
        )
        .andExpect(status().isBadRequest());

        verify(authService, never())
            .register(
                "invalid-email",
                "secret123",
                "Jérémy"
            );
    }

    /**
     * Le mot de passe doit avoir une longueur minimale.
     *
     * On fixe ici la règle MVP à 8 caractères minimum.
     */
    @Test
    void shouldRejectRegistrationWithShortPassword()
        throws Exception {

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "jeremy@example.com",
                      "password": "short",
                      "firstName": "Jérémy"
                    }
                    """)
        )
        .andExpect(status().isBadRequest());

        verify(authService, never())
            .register(
                "jeremy@example.com",
                "short",
                "Jérémy"
            );
    }

    /**
     * Le prénom est obligatoire pour créer le compte.
     */
    @Test
    void shouldRejectRegistrationWithoutFirstName()
        throws Exception {

        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "jeremy@example.com",
                      "password": "secret123",
                      "firstName": ""
                    }
                    """)
        )
        .andExpect(status().isBadRequest());

        verify(authService, never())
            .register(
                "jeremy@example.com",
                "secret123",
                ""
            );
    }

    /**
     * Une tentative de connexion sans email
     * doit être rejetée par la validation HTTP.
     */
    @Test
    void shouldRejectLoginWithoutEmail()
        throws Exception {

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "",
                      "password": "secret123"
                    }
                    """)
        )
        .andExpect(status().isBadRequest());

        verify(authService, never())
            .authenticate(
                "",
                "secret123"
            );
    }

    /**
     * Une tentative de connexion sans mot de passe
     * ne doit jamais atteindre le service.
     */
    @Test
    void shouldRejectLoginWithoutPassword()
        throws Exception {

        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "email": "jeremy@example.com",
                      "password": ""
                    }
                    """)
        )
        .andExpect(status().isBadRequest());

        verify(authService, never())
            .authenticate(
                "jeremy@example.com",
                ""
            );
    }
}