package fr.mypopote.my_popote_api.recipe.dto;

import java.math.BigDecimal;

/**
 * Représente un ingrédient retourné avec une recette.
 *
 * Toutes les informations nécessaires sont exposées afin que
 * le frontend puisse afficher et préremplir une recette existante.
 */
public record RecipeIngredientResponse(
    Long ingredientId,
    String ingredientName,
    BigDecimal quantity,
    String unit
) {
}