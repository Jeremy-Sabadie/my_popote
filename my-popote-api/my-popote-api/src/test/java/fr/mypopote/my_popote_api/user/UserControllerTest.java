package fr.mypopote.my_popote_api.user;

import fr.mypopote.my_popote_api.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du contrôleur utilisateur.
 *
 * Le service est simulé avec Mockito afin de vérifier uniquement
 * le comportement du contrôleur et la transformation vers le DTO.
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Test
    void shouldReturnUserById() {
        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        when(userService.findById(1L))
            .thenReturn(user);

        UserResponse response = userController.findById(1L);

        // Le mot de passe n'est jamais exposé dans le DTO de réponse.
        assertThat(response.email()).isEqualTo("jeremy@example.com");
        assertThat(response.firstName()).isEqualTo("Jérémy");
    }
}