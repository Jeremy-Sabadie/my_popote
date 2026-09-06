package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.planning.dto.PlannedMealRequest;
import fr.mypopote.my_popote_api.planning.dto.PlannedMealResponse;
import fr.mypopote.my_popote_api.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST dédiée aux repas placés dans un planning.
 */
@RestController
@RequestMapping("/api/planned-meals")
public class PlannedMealController {

    private final PlannedMealService plannedMealService;
    private final CurrentUserService currentUserService;

    public PlannedMealController(
        PlannedMealService plannedMealService,
        CurrentUserService currentUserService
    ) {
        this.plannedMealService = plannedMealService;
        this.currentUserService = currentUserService;
    }

    @PostMapping
    public PlannedMealResponse create(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody PlannedMealRequest request
    ) {
        Long userId =
            currentUserService.getUserId(jwt);

        return toResponse(
            plannedMealService.create(
                userId,
                request
            )
        );
    }

    /**
     * Remplace manuellement la recette d'un repas.
     */
    @PatchMapping("/{plannedMealId}/recipe")
    public PlannedMealResponse replaceRecipe(
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable Long plannedMealId,
        @RequestParam Long recipeId
    ) {
        Long userId =
            currentUserService.getUserId(jwt);

        return toResponse(
            plannedMealService.replaceRecipe(
                userId,
                plannedMealId,
                recipeId
            )
        );
    }

    private PlannedMealResponse toResponse(
        PlannedMeal plannedMeal
    ) {
        return new PlannedMealResponse(
            plannedMeal.getId(),
            plannedMeal.getRecipe().getId(),
            plannedMeal.getRecipe().getName(),
            plannedMeal.getMealDate(),
            plannedMeal.getMealType()
        );
    }
}