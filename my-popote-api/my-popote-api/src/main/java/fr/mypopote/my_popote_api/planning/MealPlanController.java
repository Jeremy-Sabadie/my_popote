package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.planning.dto.MealPlanResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * API REST dédiée aux plannings hebdomadaires.
 *
 * Le planning est recherché avec son utilisateur propriétaire
 * afin de préserver l'isolation des données.
 */
@RestController
@RequestMapping("/api/users/{userId}/meal-plans")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    public MealPlanController(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }

    /**
     * Recherche le planning d'un utilisateur pour une semaine donnée.
     *
     * Exemple :
     * GET /api/users/1/meal-plans?weekStartDate=2026-09-07
     */
    @GetMapping
    public MealPlanResponse findByUserAndWeek(
        @PathVariable Long userId,
        @RequestParam LocalDate weekStartDate
    ) {
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