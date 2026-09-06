package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.planning.dto.PlannedMealRequest;
import fr.mypopote.my_popote_api.recipe.Recipe;
import fr.mypopote.my_popote_api.recipe.RecipeRepository;
import fr.mypopote.my_popote_api.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlannedMealServiceTest {

    @Mock
    private PlannedMealRepository plannedMealRepository;

    @Mock
    private MealPlanRepository mealPlanRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private PlannedMealService plannedMealService;

    @Test
    void shouldCreatePlannedMealForAuthenticatedUser() {
        Long userId = 42L;
        LocalDate mealDate =
            LocalDate.of(2026, 9, 7);

        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        MealPlan mealPlan = new MealPlan(
            user,
            mealDate,
            false,
            null,
            null
        );

        Recipe recipe = new Recipe(
            user,
            "Poulet curry",
            "MEAT",
            2,
            null,
            null
        );

        PlannedMealRequest request =
            new PlannedMealRequest(
                10L,
                20L,
                mealDate,
                "lunch"
            );

        when(
            mealPlanRepository.findByIdAndUserId(
                10L,
                userId
            )
        ).thenReturn(Optional.of(mealPlan));

        when(
            recipeRepository.findByIdAndUserId(
                20L,
                userId
            )
        ).thenReturn(Optional.of(recipe));

        when(
            plannedMealRepository.save(
                any(PlannedMeal.class)
            )
        ).thenAnswer(
            invocation -> invocation.getArgument(0)
        );

        PlannedMeal result =
            plannedMealService.create(
                userId,
                request
            );

        assertThat(result.getMealPlan())
            .isSameAs(mealPlan);

        assertThat(result.getRecipe())
            .isSameAs(recipe);

        assertThat(result.getMealDate())
            .isEqualTo(mealDate);

        assertThat(result.getMealType())
            .isEqualTo("LUNCH");
    }

    @Test
    void shouldReplaceRecipeOnlyForOwnedPlannedMeal() {
        Long userId = 42L;
        LocalDate mealDate =
            LocalDate.of(2026, 9, 7);

        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        MealPlan mealPlan = new MealPlan(
            user,
            mealDate,
            false,
            null,
            null
        );

        Recipe oldRecipe = new Recipe(
            user,
            "Poulet curry",
            "MEAT",
            2,
            null,
            null
        );

        Recipe newRecipe = new Recipe(
            user,
            "Pâtes tomate",
            "VEGETARIAN",
            2,
            null,
            null
        );

        PlannedMeal plannedMeal =
            new PlannedMeal(
                mealPlan,
                oldRecipe,
                mealDate,
                "LUNCH"
            );

        when(
            plannedMealRepository
                .findByIdAndMealPlanUserId(
                    30L,
                    userId
                )
        ).thenReturn(
            Optional.of(plannedMeal)
        );

        when(
            recipeRepository.findByIdAndUserId(
                20L,
                userId
            )
        ).thenReturn(
            Optional.of(newRecipe)
        );

        when(
            plannedMealRepository.save(
                plannedMeal
            )
        ).thenReturn(plannedMeal);

        PlannedMeal result =
            plannedMealService.replaceRecipe(
                userId,
                30L,
                20L
            );

        assertThat(result.getRecipe())
            .isSameAs(newRecipe);

        verify(plannedMealRepository)
            .findByIdAndMealPlanUserId(
                30L,
                userId
            );
    }
}