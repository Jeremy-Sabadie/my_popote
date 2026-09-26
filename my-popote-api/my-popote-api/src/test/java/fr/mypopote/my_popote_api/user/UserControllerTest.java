package fr.mypopote.my_popote_api.user;

import fr.mypopote.my_popote_api.security.CurrentUserService;
import fr.mypopote.my_popote_api.user.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du contrôleur utilisateur.
 *
 * L'identité utilisateur doit provenir exclusivement du JWT.
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private CurrentUserService currentUserService;

    @InjectMocks
    private UserController userController;

    @Test
    void shouldReturnCurrentAuthenticatedUser() {
        Jwt jwt = new Jwt(
            "token",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            Map.of("alg", "HS256"),
            Map.of("sub", "1")
        );

        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        when(currentUserService.getUserId(jwt))
            .thenReturn(1L);

        when(userService.findCurrentUser(1L))
            .thenReturn(user);

        UserResponse response =
            userController.findCurrentUser(jwt);

        assertThat(response.email())
            .isEqualTo("jeremy@example.com");

        assertThat(response.firstName())
            .isEqualTo("Jérémy");

        verify(currentUserService)
            .getUserId(jwt);

        verify(userService)
            .findCurrentUser(1L);
    }
}