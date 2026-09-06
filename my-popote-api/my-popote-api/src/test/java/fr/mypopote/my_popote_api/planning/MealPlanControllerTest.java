package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.planning.dto.MealPlanResponse;
import fr.mypopote.my_popote_api.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du contrôleur des plannings.
 *
 * Un planning est recherché à partir de l'utilisateur
 * et du lundi correspondant au début de semaine.
 */
@ExtendWith(MockitoExtension.class)
class MealPlanControllerTest {

    @Mock
    private MealPlanService mealPlanService;

    @InjectMocks
    private MealPlanController mealPlanController;

    @Test
    void shouldReturnMealPlanForUserAndWeek() {
        LocalDate weekStart = LocalDate.of(2026, 9, 7);

        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        MealPlan mealPlan = new MealPlan(
            user,
            weekStart,
            false,
            null,
            null
        );

        when(mealPlanService.findByUserAndWeek(1L, weekStart))
            .thenReturn(Optional.of(mealPlan));

        MealPlanResponse response =
            mealPlanController.findByUserAndWeek(1L, weekStart);

        assertThat(response.weekStartDate()).isEqualTo(weekStart);
        assertThat(response.includeWeekend()).isFalse();
    }
}