package fr.mypopote.my_popote_api.auth;

import fr.mypopote.my_popote_api.user.User;
import fr.mypopote.my_popote_api.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service d'authentification.
 *
 * L'objectif est de vérifier les règles métier principales :
 * - un utilisateur peut s'inscrire avec une adresse email disponible ;
 * - le mot de passe n'est jamais enregistré en clair ;
 * - une adresse email déjà utilisée est refusée ;
 * - la connexion vérifie réellement le mot de passe fourni ;
 * - les erreurs de connexion restent volontairement génériques.
 */
class AuthServiceTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    /**
     * Chaque test travaille uniquement avec des dépendances simulées.
     *
     * Aucun accès à MariaDB n'est nécessaire ici :
     * nous testons seulement le comportement métier de AuthService.
     */
    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);

        authService = new AuthService(
            userRepository,
            passwordEncoder
        );
    }

    /**
     * Lors d'une inscription valide, le mot de passe doit être
     * encodé avant la sauvegarde de l'utilisateur.
     */
    @Test
    void shouldRegisterUserWithEncodedPassword() {
        when(
            userRepository.findByEmailIgnoreCase(
                "jeremy@example.com"
            )
        ).thenReturn(Optional.empty());

        when(
            passwordEncoder.encode("secret123")
        ).thenReturn("encoded-password");

        /*
         * Le repository simulé retourne directement l'utilisateur reçu.
         * Cela nous permet d'inspecter ce que le service souhaite sauvegarder.
         */
        when(
            userRepository.save(any(User.class))
        ).thenAnswer(invocation ->
            invocation.getArgument(0)
        );

        User registeredUser = authService.register(
            "jeremy@example.com",
            "secret123",
            "Jérémy"
        );

        assertThat(registeredUser.getEmail())
            .isEqualTo("jeremy@example.com");

        assertThat(registeredUser.getFirstName())
            .isEqualTo("Jérémy");

        /*
         * Cette assertion protège une règle de sécurité essentielle :
         * le mot de passe brut ne doit jamais être stocké en base.
         */
        assertThat(registeredUser.getPasswordHash())
            .isEqualTo("encoded-password");

        verify(passwordEncoder)
            .encode("secret123");

        verify(userRepository)
            .save(any(User.class));
    }

    /**
     * Deux comptes ne doivent pas pouvoir utiliser la même adresse email.
     */
    @Test
    void shouldRejectRegistrationWhenEmailAlreadyExists() {
        User existingUser = new User(
            "jeremy@example.com",
            "encoded-password",
            "Jérémy"
        );

        when(
            userRepository.findByEmailIgnoreCase(
                "jeremy@example.com"
            )
        ).thenReturn(Optional.of(existingUser));

        assertThatThrownBy(() ->
            authService.register(
                "jeremy@example.com",
                "secret123",
                "Jérémy"
            )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Email already in use");

        /*
         * En cas d'email déjà existant, aucune sauvegarde
         * ne doit être tentée.
         */
        verify(userRepository, never())
            .save(any(User.class));
    }

    /**
     * Une connexion est acceptée uniquement si le mot de passe fourni
     * correspond au hash enregistré pour l'utilisateur.
     */
    @Test
    void shouldAuthenticateUserWithCorrectPassword() {
        User user = new User(
            "jeremy@example.com",
            "encoded-password",
            "Jérémy"
        );

        when(
            userRepository.findByEmailIgnoreCase(
                "jeremy@example.com"
            )
        ).thenReturn(Optional.of(user));

        when(
            passwordEncoder.matches(
                "secret123",
                "encoded-password"
            )
        ).thenReturn(true);

        User authenticatedUser = authService.authenticate(
            "jeremy@example.com",
            "secret123"
        );

        assertThat(authenticatedUser)
            .isSameAs(user);
    }

    /**
     * Un mauvais mot de passe doit provoquer un refus d'authentification.
     */
    @Test
    void shouldRejectAuthenticationWithWrongPassword() {
        User user = new User(
            "jeremy@example.com",
            "encoded-password",
            "Jérémy"
        );

        when(
            userRepository.findByEmailIgnoreCase(
                "jeremy@example.com"
            )
        ).thenReturn(Optional.of(user));

        when(
            passwordEncoder.matches(
                "wrong-password",
                "encoded-password"
            )
        ).thenReturn(false);

        assertThatThrownBy(() ->
            authService.authenticate(
                "jeremy@example.com",
                "wrong-password"
            )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid credentials");
    }

    /**
     * L'API ne doit pas révéler si une adresse email existe ou non.
     *
     * On retourne donc la même erreur générique qu'en cas
     * de mauvais mot de passe.
     */
    @Test
    void shouldRejectAuthenticationWhenUserDoesNotExist() {
        when(
            userRepository.findByEmailIgnoreCase(
                "unknown@example.com"
            )
        ).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            authService.authenticate(
                "unknown@example.com",
                "secret123"
            )
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Invalid credentials");
    }
}