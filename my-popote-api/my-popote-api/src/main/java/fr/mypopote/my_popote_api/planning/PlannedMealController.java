package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.planning.dto.PlannedMealRequest;
import fr.mypopote.my_popote_api.planning.dto.PlannedMealResponse;
import fr.mypopote.my_popote_api.security.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST dédiée aux repas placés dans un planning.
 *
 * L'identité utilisateur provient exclusivement du JWT.
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

    /**
     * Place une recette dans un planning de l'utilisateur connecté.
     */
    @PostMapping
    public PlannedMealResponse create(
        @AuthenticationPrincipal Jwt jwt,
        @Valid @RequestBody PlannedMealRequest request
    ) {
        Long userId = currentUserService.getUserId(jwt);

        PlannedMeal savedMeal =
            plannedMealService.create(userId, request);

        return new PlannedMealResponse(
            savedMeal.getId(),
            savedMeal.getRecipe().getId(),
            savedMeal.getRecipe().getName(),
            savedMeal.getMealDate(),
            savedMeal.getMealType()
        );
    }
}