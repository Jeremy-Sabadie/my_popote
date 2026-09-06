package fr.mypopote.my_popote_api.planning.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Données nécessaires pour générer automatiquement une semaine.
 *
 * Les recettes sont choisies par l'utilisateur.
 * Le backend se charge ensuite de les répartir dans les repas.
 */
public record GenerateMealPlanRequest(

    @NotNull
    LocalDate weekStartDate,

    boolean includeWeekend,

    @Positive
    BigDecimal maxBudget,

    @NotEmpty
    List<@Positive Long> recipeIds

) {
}