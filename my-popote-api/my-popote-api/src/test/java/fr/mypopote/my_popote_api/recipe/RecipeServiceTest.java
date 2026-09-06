package fr.mypopote.my_popote_api.recipe;

import fr.mypopote.my_popote_api.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service de gestion des recettes.
 *
 * Les recherches sont toujours limitées à l'utilisateur concerné.
 * Cette règle participe à l'isolation des données entre comptes.
 */
@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private RecipeService recipeService;

    @Test
    void shouldReturnOnlyRecipesOwnedByUser() {
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

        // Le repository ne recherche que les recettes appartenant à cet utilisateur.
        when(recipeRepository.findAllByUserId(1L))
            .thenReturn(List.of(recipe));

        List<Recipe> result = recipeService.findAllByUserId(1L);

        assertThat(result).containsExactly(recipe);
    }
}