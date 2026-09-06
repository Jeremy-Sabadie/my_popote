package fr.mypopote.my_popote_api.planning.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

/**
 * Données nécessaires pour placer une recette
 * dans un planning hebdomadaire.
 */
public record PlannedMealRequest(

    @NotNull
    @Positive
    Long mealPlanId,

    @NotNull
    @Positive
    Long recipeId,

    @NotNull
    LocalDate mealDate,

    @NotBlank
    String mealType

) {
}