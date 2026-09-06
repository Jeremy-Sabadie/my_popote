package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.planning.dto.GenerateMealPlanRequest;
import fr.mypopote.my_popote_api.planning.dto.MealPlanResponse;
import fr.mypopote.my_popote_api.planning.dto.PlannedMealResponse;
import fr.mypopote.my_popote_api.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

/**
 * API REST dédiée aux plannings hebdomadaires.
 *
 * L'identité utilisateur vient exclusivement du JWT.
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
     * Retourne le planning d'une semaine.
     */
    @GetMapping
    public MealPlanResponse findByWeek(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam LocalDate weekStartDate
    ) {
        Long userId =
            currentUserService.getUserId(jwt);

        return mealPlanService.getWeek(
            userId,
            weekStartDate
        );
    }

    /**
     * Génère ou régénère une semaine à partir
     * des recettes choisies par l'utilisateur.
     */
    @PostMapping("/generate")
    public MealPlanResponse generate(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody GenerateMealPlanRequest request
    ) {
        Long userId =
            currentUserService.getUserId(jwt);

        return mealPlanService.generate(
            userId,
            request
        );
    }

    /**
     * Retourne l'historique des semaines.
     */
    @GetMapping("/history")
    public List<MealPlanResponse> history(
        @AuthenticationPrincipal Jwt jwt
    ) {
        Long userId =
            currentUserService.getUserId(jwt);

        return mealPlanService.getHistory(userId);
    }

    /**
     * Retourne les repas d'une date donnée.
     */
    @GetMapping("/day")
    public List<PlannedMealResponse> findByDate(
        @AuthenticationPrincipal Jwt jwt,
        @RequestParam LocalDate date
    ) {
        Long userId =
            currentUserService.getUserId(jwt);

        return mealPlanService.getMealsForDate(
            userId,
            date
        );
    }
}