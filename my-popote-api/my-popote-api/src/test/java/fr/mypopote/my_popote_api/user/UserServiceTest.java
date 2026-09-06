package fr.mypopote.my_popote_api.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service utilisateur.
 *
 * Le repository est simulé avec Mockito afin de tester uniquement
 * la logique du service, sans accès à la base de données.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    // Mockito construit le service et lui injecte automatiquement le repository simulé.
    @InjectMocks
    private UserService userService;

    @Test
    void shouldFindUserById() {
        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        // On définit le comportement attendu du repository pour cet utilisateur.
        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        User result = userService.findById(1L);

        assertThat(result).isSameAs(user);
    }
}