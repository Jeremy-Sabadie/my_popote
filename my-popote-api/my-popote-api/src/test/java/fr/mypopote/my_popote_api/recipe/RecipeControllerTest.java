package fr.mypopote.my_popote_api.recipe;

import fr.mypopote.my_popote_api.recipe.dto.RecipeResponse;
import fr.mypopote.my_popote_api.security.CurrentUserService;
import fr.mypopote.my_popote_api.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du contrôleur des recettes.
 *
 * L'identité utilisateur doit provenir du JWT authentifié
 * et jamais d'un identifiant fourni par le frontend.
 */
@ExtendWith(MockitoExtension.class)
class RecipeControllerTest {

    @Mock
    private RecipeService recipeService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private RecipeController recipeController;

    @Test
    void shouldReturnRecipesForAuthenticatedUser() {
        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        Recipe recipe = new Recipe(
            user,
            "Poulet curry",
            "MEAT",
            2,
            null,
            null
        );

        when(currentUserService.getUserId(jwt))
            .thenReturn(1L);

        when(recipeService.findAllByUserId(1L))
            .thenReturn(List.of(recipe));

        List<RecipeResponse> response =
            recipeController.findAll(jwt);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).name())
            .isEqualTo("Poulet curry");
        assertThat(response.get(0).category())
            .isEqualTo("MEAT");
        assertThat(response.get(0).servings())
            .isEqualTo(2);
    }
}