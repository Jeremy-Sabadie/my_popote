package fr.mypopote.my_popote_api.recipe;

import fr.mypopote.my_popote_api.recipe.dto.RecipeIngredientRequest;
import fr.mypopote.my_popote_api.recipe.dto.RecipeRequest;
import fr.mypopote.my_popote_api.recipe.dto.RecipeResponse;
import fr.mypopote.my_popote_api.user.User;
import fr.mypopote.my_popote_api.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service de gestion des recettes.
 */
@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IngredientService ingredientService;

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

        when(recipeRepository.findAllByUserId(1L))
            .thenReturn(List.of(recipe));

        List<Recipe> result =
            recipeService.findAllByUserId(1L);

        assertThat(result).containsExactly(recipe);
    }

    @Test
    void shouldCreateRecipeWithIngredientsAndSeasons() {
        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        Ingredient chicken = new Ingredient("Poulet");

        RecipeRequest request = new RecipeRequest(
            "Poulet curry",
            "meat",
            2,
            new BigDecimal("8.50"),
            "Faire cuire le poulet.",
            List.of(
                new RecipeIngredientRequest(
                    "Poulet",
                    new BigDecimal("300"),
                    "g"
                )
            ),
            Set.of("winter", "autumn")
        );

        when(userRepository.findById(1L))
            .thenReturn(Optional.of(user));

        when(ingredientService.findOrCreate("Poulet"))
            .thenReturn(chicken);

        when(recipeRepository.save(any(Recipe.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        RecipeResponse result =
            recipeService.create(request, 1L);

        assertThat(result.name())
            .isEqualTo("Poulet curry");

        assertThat(result.category())
            .isEqualTo("MEAT");

        assertThat(result.ingredients())
            .hasSize(1);

        assertThat(result.seasons())
            .containsExactlyInAnyOrder(
                "WINTER",
                "AUTUMN"
            );
    }

    @Test
    void shouldUpdateOnlyOwnedRecipe() {
        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        Recipe recipe = new Recipe(
            user,
            "Ancien nom",
            "MEAT",
            2,
            null,
            null
        );

        Ingredient tomato = new Ingredient("Tomate");

        RecipeRequest request = new RecipeRequest(
            "Nouveau nom",
            "vegetarian",
            4,
            new BigDecimal("5.00"),
            "Nouvelles instructions",
            List.of(
                new RecipeIngredientRequest(
                    "Tomate",
                    new BigDecimal("2"),
                    "piece"
                )
            ),
            Set.of("summer")
        );

        when(recipeRepository.findByIdAndUserId(10L, 1L))
            .thenReturn(Optional.of(recipe));

        when(ingredientService.findOrCreate("Tomate"))
            .thenReturn(tomato);

        when(recipeRepository.save(recipe))
            .thenReturn(recipe);

        RecipeResponse result =
            recipeService.update(
                10L,
                request,
                1L
            );

        assertThat(result.name())
            .isEqualTo("Nouveau nom");

        assertThat(result.category())
            .isEqualTo("VEGETARIAN");

        assertThat(result.ingredients())
            .hasSize(1);

        assertThat(result.seasons())
            .containsExactly("SUMMER");
    }

    @Test
    void shouldDeleteOnlyOwnedRecipe() {
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

        when(recipeRepository.findByIdAndUserId(10L, 1L))
            .thenReturn(Optional.of(recipe));

        recipeService.delete(10L, 1L);

        verify(recipeRepository).delete(recipe);
    }
}