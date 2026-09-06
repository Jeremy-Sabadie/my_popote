package fr.mypopote.my_popote_api.planning.dto;

import java.time.LocalDate;

/**
 * Repas planifié retourné au frontend.
 */
public record PlannedMealResponse(
    Long id,
    Long recipeId,
    String recipeName,
    LocalDate mealDate,
    String mealType
) {
}