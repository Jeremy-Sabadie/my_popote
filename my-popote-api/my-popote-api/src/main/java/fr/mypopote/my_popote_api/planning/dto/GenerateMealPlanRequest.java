
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
 * Les tags permettent de privilégier certaines recettes.
 * La saison choisie peut remplacer celle déduite de la date.
 */
public record GenerateMealPlanRequest(
    @NotNull LocalDate weekStartDate,
    boolean includeWeekend,
    @Positive BigDecimal maxBudget,
    @NotEmpty List<Long> recipeIds,
    List<Long> preferredTagIds,
    String preferredSeason
) {

    /**
     * Conserve les appels existants sans préférences.
     */
    public GenerateMealPlanRequest(
        LocalDate weekStartDate,
        boolean includeWeekend,
        BigDecimal maxBudget,
        List<Long> recipeIds
    ) {
        this(
            weekStartDate,
            includeWeekend,
            maxBudget,
            recipeIds,
            List.of(),
            null
        );
    }

    /**
     * Conserve les appels existants avec préférences de tags.
     * Sans saison explicite, le moteur utilise la date de la semaine.
     */
    public GenerateMealPlanRequest(
        LocalDate weekStartDate,
        boolean includeWeekend,
        BigDecimal maxBudget,
        List<Long> recipeIds,
        List<Long> preferredTagIds
    ) {
        this(
            weekStartDate,
            includeWeekend,
            maxBudget,
            recipeIds,
            preferredTagIds,
            null
        );
    }
}