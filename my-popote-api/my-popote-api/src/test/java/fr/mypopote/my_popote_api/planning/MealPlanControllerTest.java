package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.planning.dto.MealPlanResponse;
import fr.mypopote.my_popote_api.security.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
        LocalDate weekStart =
            LocalDate.of(2026, 9, 7);

        MealPlanResponse expected =
            new MealPlanResponse(
                10L,
                weekStart,
                false,
                null,
                null,
                List.of()
            );

        when(currentUserService.getUserId(jwt))
            .thenReturn(1L);

        when(
            mealPlanService.getWeek(
                1L,
                weekStart
            )
        ).thenReturn(expected);

        MealPlanResponse response =
            mealPlanController.findByWeek(
                jwt,
                weekStart
            );

        assertThat(response)
            .isEqualTo(expected);
    }
}