package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.security.CurrentUserService;
import fr.mypopote.my_popote_api.security.SecurityConfig;
import fr.mypopote.my_popote_api.user.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Vérifie que l'accès aux plannings utilise uniquement
 * l'identité de l'utilisateur authentifié.
 */
@WebMvcTest(MealPlanController.class)
@Import(SecurityConfig.class)
class MealPlanControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MealPlanService mealPlanService;

    @MockitoBean
    private CurrentUserService currentUserService;

    /**
     * Le userId utilisé pour rechercher le planning
     * doit provenir du JWT.
     */
    @Test
    void shouldUseAuthenticatedUserIdToFindMealPlan() throws Exception {
        LocalDate weekStartDate = LocalDate.of(2026, 9, 7);

        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        MealPlan mealPlan = new MealPlan(
            user,
            weekStartDate,
            false,
            null,
            null
        );

        when(currentUserService.getUserId(any(Jwt.class)))
            .thenReturn(42L);

        when(mealPlanService.findByUserAndWeek(42L, weekStartDate))
            .thenReturn(Optional.of(mealPlan));

        mockMvc.perform(
                get("/api/meal-plans")
                    .param("weekStartDate", "2026-09-07")
                    .with(jwt().jwt(jwt -> jwt.subject("42")))
            )
            .andExpect(status().isOk());

        verify(mealPlanService)
            .findByUserAndWeek(42L, weekStartDate);
    }
}