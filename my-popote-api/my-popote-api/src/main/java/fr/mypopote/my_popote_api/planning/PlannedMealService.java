package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.planning.dto.PlannedMealRequest;
import fr.mypopote.my_popote_api.recipe.Recipe;
import fr.mypopote.my_popote_api.recipe.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service chargé des repas positionnés dans un planning.
 *
 * Le planning et la recette sont toujours recherchés
 * avec l'identifiant de l'utilisateur authentifié.
 */
@Service
public class PlannedMealService {

    private final PlannedMealRepository plannedMealRepository;
    private final MealPlanRepository mealPlanRepository;
    private final RecipeRepository recipeRepository;

    public PlannedMealService(
        PlannedMealRepository plannedMealRepository,
        MealPlanRepository mealPlanRepository,
        RecipeRepository recipeRepository
    ) {
        this.plannedMealRepository = plannedMealRepository;
        this.mealPlanRepository = mealPlanRepository;
        this.recipeRepository = recipeRepository;
    }

    /**
     * Ajoute un repas à un planning appartenant
     * à l'utilisateur authentifié.
     */
    @Transactional
    public PlannedMeal create(
        Long userId,
        PlannedMealRequest request
    ) {
        MealPlan mealPlan = mealPlanRepository
            .findByIdAndUserId(request.mealPlanId(), userId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Meal plan not found"
            ));

        Recipe recipe = recipeRepository
            .findByIdAndUserId(request.recipeId(), userId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Recipe not found"
            ));

        PlannedMeal plannedMeal = new PlannedMeal(
            mealPlan,
            recipe,
            request.mealDate(),
            request.mealType()
        );

        return plannedMealRepository.save(plannedMeal);
    }
}