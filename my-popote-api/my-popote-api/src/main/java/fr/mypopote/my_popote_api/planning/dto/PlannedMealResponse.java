package fr.mypopote.my_popote_api.planning.dto;

import java.time.LocalDate;

public record PlannedMealResponse(
    Long id,
    Long recipeId,
    String recipeName,
    LocalDate mealDate,
    String mealType
) {
}