package fr.mypopote.my_popote_api.recipe.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record RecipeResponse(
    Long id,
    String name,
    String category,
    Integer servings,
    BigDecimal estimatedCost,
    String instructions,
    List<RecipeIngredientResponse> ingredients,
    Set<String> seasons
) {
}