package fr.mypopote.my_popote_api.auth;

import fr.mypopote.my_popote_api.security.SecurityConfig;
import fr.mypopote.my_popote_api.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie le contrat HTTP des endpoints d'authentification.
 *
 * Les tests couvrent :
 * - l'inscription ;
 * - la connexion ;
 * - la validation des données reçues ;
 * - le contenu de la réponse envoyée au frontend.
 */
@WebMvcTest(
    controllers = AuthController.class,
    properties = {
        "app.jwt.secret=my-popote-test-secret-key-12345678901234567890",
        "app.cors.allowed-origins=http://localhost:4200"
    }
)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void shouldRegisterUserAndReturnAccessToken() throws Exception {
        User user = createUser();

        when(
            authService.register(
                anyString(),
                anyString(),
                anyString()
            )
        ).thenReturn(user);

        mockMvc.perform(
            post("/api/auth/register")
                .contentType("application/json")
                .content("""
                    {
                      "email": "jeremy@example.com",
                      "password": "Password123!",
                      "firstName": "Jeremy"
                    }
                    """)
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id")
            .value(42))
        .andExpect(jsonPath("$.email")
            .value("jeremy@example.com"))
        .andExpect(jsonPath("$.firstName")
            .value("Jeremy"))
        .andExpect(jsonPath("$.accessToken")
            .value("test-access-token"));
    }

    @Test
    void shouldLoginUserAndReturnAccessToken() throws Exception {
        User user = createUser();

        when(
            authService.authenticate(
                anyString(),
                anyString()
            )
        ).thenReturn(user);

        mockMvc.perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content("""
                    {
                      "email": "jeremy@example.com",
                      "password": "Password123!"
                    }
                    """)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id")
            .value(42))
        .andExpect(jsonPath("$.email")
            .value("jeremy@example.com"))
        .andExpect(jsonPath("$.firstName")
            .value("Jeremy"))
        .andExpect(jsonPath("$.accessToken")
            .value("test-access-token"));
    }

    @Test
    void shouldRejectRegistrationWithInvalidEmail()
        throws Exception {

        mockMvc.perform(
            post("/api/auth/register")
                .contentType("application/json")
                .content("""
                    {
                      "email": "invalid-email",
                      "password": "Password123!",
                      "firstName": "Jeremy"
                    }
                    """)
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRegistrationWithShortPassword()
        throws Exception {

        mockMvc.perform(
            post("/api/auth/register")
                .contentType("application/json")
                .content("""
                    {
                      "email": "jeremy@example.com",
                      "password": "short",
                      "firstName": "Jeremy"
                    }
                    """)
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectRegistrationWithoutFirstName()
        throws Exception {

        mockMvc.perform(
            post("/api/auth/register")
                .contentType("application/json")
                .content("""
                    {
                      "email": "jeremy@example.com",
                      "password": "Password123!"
                    }
                    """)
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectLoginWithoutEmail()
        throws Exception {

        mockMvc.perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content("""
                    {
                      "password": "Password123!"
                    }
                    """)
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectLoginWithoutPassword()
        throws Exception {

        mockMvc.perform(
            post("/api/auth/login")
                .contentType("application/json")
                .content("""
                    {
                      "email": "jeremy@example.com"
                    }
                    """)
        )
        .andExpect(status().isBadRequest());
    }

    /**
     * Prépare un utilisateur simulé cohérent
     * avec la réponse attendue du contrôleur.
     */
    private User createUser() {
        User user = org.mockito.Mockito.mock(User.class);

        when(user.getId()).thenReturn(42L);
        when(user.getEmail())
            .thenReturn("jeremy@example.com");
        when(user.getFirstName())
            .thenReturn("Jeremy");

        when(
            jwtService.generateToken(
                42L,
                "jeremy@example.com"
            )
        ).thenReturn("test-access-token");

        return user;
    }
}