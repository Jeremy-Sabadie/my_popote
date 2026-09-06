package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.planning.dto.MealPlanResponse;
import fr.mypopote.my_popote_api.security.CurrentUserService;
import fr.mypopote.my_popote_api.user.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du contrôleur des plannings.
 *
 * L'identité utilisateur doit provenir du JWT authentifié
 * et jamais d'un identifiant fourni par le frontend.
 */
@ExtendWith(MockitoExtension.class)
class MealPlanControllerTest {

    @Mock
    private MealPlanService mealPlanService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private MealPlanController mealPlanController;

    @Test
    void shouldReturnMealPlanForAuthenticatedUserAndWeek() {
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

        when(currentUserService.getUserId(jwt))
            .thenReturn(1L);

        when(mealPlanService.findByUserAndWeek(1L, weekStart))
            .thenReturn(Optional.of(mealPlan));

        MealPlanResponse response =
            mealPlanController.findByWeek(jwt, weekStart);

        assertThat(response.weekStartDate())
            .isEqualTo(weekStart);

        assertThat(response.includeWeekend())
            .isFalse();
    }
}