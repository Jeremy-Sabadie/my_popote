package fr.mypopote.my_popote_api.recipe;

import fr.mypopote.my_popote_api.recipe.dto.RecipeRequest;
import fr.mypopote.my_popote_api.recipe.dto.RecipeResponse;
import fr.mypopote.my_popote_api.security.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du contrôleur des recettes.
 *
 * L'identité utilisateur provient toujours du JWT.
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
        RecipeResponse recipe = new RecipeResponse(
            10L,
            "Poulet curry",
            "MEAT",
            2,
            new BigDecimal("8.50"),
            "Faire cuire le poulet.",
            List.of(),
            Set.of("WINTER")
        );

        when(currentUserService.getUserId(jwt))
            .thenReturn(1L);

        when(
            recipeService.findAllByUserId(
                1L,
                null,
                null,
                null
            )
        ).thenReturn(List.of(recipe));

        List<RecipeResponse> response =
            recipeController.findAll(
                jwt,
                null,
                null,
                null
            );

        assertThat(response)
            .containsExactly(recipe);
    }

    @Test
    void shouldForwardTagFiltersForAuthenticatedUser() {
        Set<Long> tagIds = Set.of(2L, 5L);

        when(currentUserService.getUserId(jwt))
            .thenReturn(1L);

        when(
            recipeService.findAllByUserId(
                1L,
                null,
                null,
                tagIds
            )
        ).thenReturn(List.of());

        recipeController.findAll(
            jwt,
            null,
            null,
            tagIds
        );

        verify(recipeService)
            .findAllByUserId(
                1L,
                null,
                null,
                tagIds
            );
    }

    @Test
    void shouldCreateRecipeForAuthenticatedUser() {
        RecipeRequest request = new RecipeRequest(
            "Poulet curry",
            "MEAT",
            2,
            null,
            null,
            List.of(),
            Set.of()
        );

        RecipeResponse response = new RecipeResponse(
            10L,
            "Poulet curry",
            "MEAT",
            2,
            null,
            null,
            List.of(),
            Set.of()
        );

        when(currentUserService.getUserId(jwt))
            .thenReturn(1L);

        when(recipeService.create(request, 1L))
            .thenReturn(response);

        RecipeResponse result =
            recipeController.create(request, jwt);

        assertThat(result.id())
            .isEqualTo(10L);

        verify(recipeService)
            .create(request, 1L);
    }

    @Test
    void shouldDeleteRecipeForAuthenticatedUser() {
        when(currentUserService.getUserId(jwt))
            .thenReturn(1L);

        recipeController.delete(10L, jwt);

        verify(recipeService)
            .delete(10L, 1L);
    }
}