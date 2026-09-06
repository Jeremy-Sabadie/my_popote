package fr.mypopote.my_popote_api.planning;

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
 * Tests unitaires de la gestion des repas placés dans le planning.
 *
 * Un PlannedMeal représente l'association entre une recette
 * et un créneau précis du planning : date + type de repas.
 */
@ExtendWith(MockitoExtension.class)
class PlannedMealServiceTest {

    @Mock
    private PlannedMealRepository plannedMealRepository;

    @InjectMocks
    private PlannedMealService plannedMealService;

    @Test
    void shouldSavePlannedMeal() {
        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        MealPlan mealPlan = new MealPlan(
            user,
            LocalDate.of(2026, 9, 7),
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
            LocalDate.of(2026, 9, 7),
            "LUNCH"
        );

        when(plannedMealRepository.save(plannedMeal))
            .thenReturn(plannedMeal);

        PlannedMeal result = plannedMealService.save(plannedMeal);

        assertThat(result).isSameAs(plannedMeal);
    }
}