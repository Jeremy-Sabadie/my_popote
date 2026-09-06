package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.planning.dto.MealPlanResponse;
import fr.mypopote.my_popote_api.security.CurrentUserService;
import fr.mypopote.my_popote_api.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MealPlanController.class)
@Import(SecurityConfig.class)
class MealPlanControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MealPlanService mealPlanService;

    @MockitoBean
    private CurrentUserService currentUserService;

    @Test
    void shouldUseAuthenticatedUserIdToFindMealPlan()
        throws Exception {

        LocalDate weekStartDate =
            LocalDate.of(2026, 9, 7);

        MealPlanResponse response =
            new MealPlanResponse(
                10L,
                weekStartDate,
                false,
                null,
                null,
                List.of()
            );

        when(
            currentUserService.getUserId(
                any(Jwt.class)
            )
        ).thenReturn(42L);

        when(
            mealPlanService.getWeek(
                42L,
                weekStartDate
            )
        ).thenReturn(response);

        mockMvc.perform(
                get("/api/meal-plans")
                    .param(
                        "weekStartDate",
                        "2026-09-07"
                    )
                    .with(
                        jwt().jwt(token ->
                            token.subject("42")
                        )
                    )
            )
            .andExpect(status().isOk());

        verify(mealPlanService)
            .getWeek(
                42L,
                weekStartDate
            );
    }
}