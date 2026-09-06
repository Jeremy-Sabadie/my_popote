package fr.mypopote.my_popote_api.recipe.dto;

import java.math.BigDecimal;

public record RecipeIngredientResponse(
    Long ingredientId,
    String ingredientName,
    BigDecimal quantity,
    String unit
) {
}