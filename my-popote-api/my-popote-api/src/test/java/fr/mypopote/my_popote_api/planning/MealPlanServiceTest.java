package fr.mypopote.my_popote_api.planning;

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
 * Tests unitaires de la gestion des plannings hebdomadaires.
 *
 * Un planning est identifié par son utilisateur et par
 * la date de début de la semaine concernée.
 */
@ExtendWith(MockitoExtension.class)
class MealPlanServiceTest {

    @Mock
    private MealPlanRepository mealPlanRepository;

    @InjectMocks
    private MealPlanService mealPlanService;

    @Test
    void shouldFindMealPlanForUserAndWeek() {
        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        // Le lundi représente le début de semaine dans My Popote.
        LocalDate weekStart = LocalDate.of(2026, 9, 7);

        MealPlan mealPlan = new MealPlan(
            user,
            weekStart,
            false,
            null,
            null
        );

        when(
            mealPlanRepository.findByUserIdAndWeekStartDate(
                1L,
                weekStart
            )
        ).thenReturn(Optional.of(mealPlan));

        Optional<MealPlan> result =
            mealPlanService.findByUserAndWeek(
                1L,
                weekStart
            );

        assertThat(result).contains(mealPlan);
    }
}