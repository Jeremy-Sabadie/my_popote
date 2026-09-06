package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.planning.dto.PlannedMealRequest;
import fr.mypopote.my_popote_api.planning.dto.PlannedMealResponse;
import fr.mypopote.my_popote_api.recipe.Recipe;
import fr.mypopote.my_popote_api.security.CurrentUserService;
import fr.mypopote.my_popote_api.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du contrôleur des repas planifiés.
 *
 * L'identité utilisateur doit provenir du JWT
 * et jamais du contenu envoyé par le frontend.
 */
@ExtendWith(MockitoExtension.class)
class PlannedMealControllerTest {

    @Mock
    private PlannedMealService plannedMealService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private PlannedMealController plannedMealController;

    @Test
    void shouldCreatePlannedMealForAuthenticatedUser() {
        Long userId = 42L;
        LocalDate mealDate = LocalDate.of(2026, 9, 7);

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

        PlannedMealRequest request = new PlannedMealRequest(
            10L,
            20L,
            mealDate,
            "LUNCH"
        );

        PlannedMeal plannedMeal = new PlannedMeal(
            mealPlan,
            recipe,
            mealDate,
            "LUNCH"
        );

        when(currentUserService.getUserId(jwt))
            .thenReturn(userId);

        when(plannedMealService.create(userId, request))
            .thenReturn(plannedMeal);

        PlannedMealResponse response =
            plannedMealController.create(jwt, request);

        assertThat(response.recipeName())
            .isEqualTo("Poulet curry");

        assertThat(response.mealDate())
            .isEqualTo(mealDate);

        assertThat(response.mealType())
            .isEqualTo("LUNCH");

        verify(plannedMealService)
            .create(userId, request);
    }
}