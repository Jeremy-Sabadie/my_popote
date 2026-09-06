package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.planning.dto.PlannedMealResponse;
import fr.mypopote.my_popote_api.recipe.Recipe;
import fr.mypopote.my_popote_api.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du contrôleur des repas planifiés.
 *
 * Ce premier test vérifie qu'un repas transmis au service
 * est correctement transformé en DTO de réponse.
 */
@ExtendWith(MockitoExtension.class)
class PlannedMealControllerTest {

    @Mock
    private PlannedMealService plannedMealService;

    @InjectMocks
    private PlannedMealController plannedMealController;

    @Test
    void shouldSavePlannedMeal() {
        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        LocalDate mealDate = LocalDate.of(2026, 9, 7);

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

        PlannedMeal plannedMeal = new PlannedMeal(
            mealPlan,
            recipe,
            mealDate,
            "LUNCH"
        );

        when(plannedMealService.save(plannedMeal))
            .thenReturn(plannedMeal);

        PlannedMealResponse response =
            plannedMealController.save(plannedMeal);

        assertThat(response.recipeName()).isEqualTo("Poulet curry");
        assertThat(response.mealDate()).isEqualTo(mealDate);
        assertThat(response.mealType()).isEqualTo("LUNCH");
    }
}