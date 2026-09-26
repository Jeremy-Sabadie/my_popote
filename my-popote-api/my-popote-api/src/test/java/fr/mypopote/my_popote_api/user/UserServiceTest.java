package fr.mypopote.my_popote_api.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service utilisateur.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldFindCurrentAuthenticatedUser() {
        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        User result =
            userService.findCurrentUser(1L);

        assertThat(result).isSameAs(user);

        verify(userRepository).findById(1L);
    }

    @Test
    void shouldRejectUnknownAuthenticatedUser() {
        when(userRepository.findById(999L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
            userService.findCurrentUser(999L)
        )
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("User not found");

        verify(userRepository).findById(999L);
    }
}