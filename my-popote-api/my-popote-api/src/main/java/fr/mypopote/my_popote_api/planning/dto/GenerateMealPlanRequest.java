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
 * Les recettes candidates sont choisies par l'utilisateur.
 *
 * Les tags préférés permettent au moteur de génération
 * de privilégier certains types de recettes sans en faire
 * des contraintes obligatoires.
 */
public record GenerateMealPlanRequest(

    @NotNull
    LocalDate weekStartDate,

    boolean includeWeekend,

    @Positive
    BigDecimal maxBudget,

    @NotEmpty
    List<Long> recipeIds,

    List<Long> preferredTagIds

) {

    /**
     * Constructeur conservé pour les appels existants
     * qui ne fournissent pas encore de préférences.
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
            List.of()
        );
    }
}