package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.planning.dto.PlannedMealResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * API REST dédiée aux repas placés dans un planning.
 *
 * Le contrôleur délègue l'enregistrement au service
 * puis transforme le résultat en DTO.
 */
@RestController
@RequestMapping("/api/planned-meals")
public class PlannedMealController {

    private final PlannedMealService plannedMealService;

    public PlannedMealController(
        PlannedMealService plannedMealService
    ) {
        this.plannedMealService = plannedMealService;
    }

    /**
     * Enregistre un repas dans le planning.
     *
     * Cette première version suit notre cycle TDD actuel.
     * Le contrat sera ensuite renforcé pour recevoir
     * PlannedMealRequest plutôt qu'une entité JPA.
     */
    @PostMapping
    public PlannedMealResponse save(
        @RequestBody PlannedMeal plannedMeal
    ) {
        PlannedMeal savedMeal =
            plannedMealService.save(plannedMeal);

        return new PlannedMealResponse(
            savedMeal.getId(),
            savedMeal.getRecipe().getId(),
            savedMeal.getRecipe().getName(),
            savedMeal.getMealDate(),
            savedMeal.getMealType()
        );
    }
}