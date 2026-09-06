package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.planning.dto.PlannedMealRequest;
import fr.mypopote.my_popote_api.recipe.Recipe;
import fr.mypopote.my_popote_api.recipe.RecipeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service chargé des repas positionnés dans un planning.
 *
 * Les opérations sont toujours limitées aux données
 * de l'utilisateur authentifié.
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

    @Transactional
    public PlannedMeal create(
        Long userId,
        PlannedMealRequest request
    ) {
        MealPlan mealPlan = mealPlanRepository
            .findByIdAndUserId(
                request.mealPlanId(),
                userId
            )
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Meal plan not found"
                )
            );

        Recipe recipe = recipeRepository
            .findByIdAndUserId(
                request.recipeId(),
                userId
            )
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Recipe not found"
                )
            );

        PlannedMeal plannedMeal =
            new PlannedMeal(
                mealPlan,
                recipe,
                request.mealDate(),
                normalizeMealType(
                    request.mealType()
                )
            );

        return plannedMealRepository.save(
            plannedMeal
        );
    }

    /**
     * Remplace la recette d'un créneau existant.
     */
    @Transactional
    public PlannedMeal replaceRecipe(
        Long userId,
        Long plannedMealId,
        Long recipeId
    ) {
        PlannedMeal plannedMeal =
            plannedMealRepository
                .findByIdAndMealPlanUserId(
                    plannedMealId,
                    userId
                )
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Planned meal not found"
                    )
                );

        Recipe recipe = recipeRepository
            .findByIdAndUserId(
                recipeId,
                userId
            )
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Recipe not found"
                )
            );

        plannedMeal.setRecipe(recipe);

        return plannedMealRepository.save(
            plannedMeal
        );
    }

    private String normalizeMealType(
        String mealType
    ) {
        String normalized =
            mealType.trim().toUpperCase();

        if (!normalized.equals("LUNCH")
            && !normalized.equals("DINNER")) {

            throw new IllegalArgumentException(
                "Meal type must be LUNCH or DINNER"
            );
        }

        return normalized;
    }
}