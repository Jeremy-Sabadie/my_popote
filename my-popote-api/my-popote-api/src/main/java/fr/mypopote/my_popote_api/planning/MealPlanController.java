package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.planning.dto.MealPlanResponse;
import fr.mypopote.my_popote_api.security.CurrentUserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * API REST dédiée aux plannings hebdomadaires.
 *
 * L'identité utilisateur provient exclusivement du JWT authentifié.
 * Aucun userId fourni par le frontend n'est utilisé.
 */
@RestController
@RequestMapping("/api/meal-plans")
public class MealPlanController {

    private final MealPlanService mealPlanService;
    private final CurrentUserService currentUserService;

    public MealPlanController(
        MealPlanService mealPlanService,
        CurrentUserService currentUserService
    ) {
        this.mealPlanService = mealPlanService;
        this.currentUserService = currentUserService;
    }

    /**
     * Recherche le planning de l'utilisateur connecté
     * pour une semaine donnée.
     */
    @GetMapping
    public MealPlanResponse findByWeek(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam LocalDate weekStartDate
    ) {
        Long userId = currentUserService.getUserId(jwt);

        MealPlan mealPlan = mealPlanService
            .findByUserAndWeek(userId, weekStartDate)
            .orElseThrow(() -> new IllegalArgumentException(
                "Meal plan not found for week: " + weekStartDate
            ));

        return new MealPlanResponse(
            mealPlan.getId(),
            mealPlan.getWeekStartDate(),
            mealPlan.isIncludeWeekend(),
            mealPlan.getMaxBudget(),
            mealPlan.getEstimatedCost(),
            List.of()
        );
    }
}