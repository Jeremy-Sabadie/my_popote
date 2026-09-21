package fr.mypopote.my_popote_api.shopping.dto;

import java.time.LocalDate;

/**
 * Résumé d'une liste de courses dans l'historique.
 * weekStartDate correspond au lundi de la semaine planifiée.
 */
public record ShoppingListHistoryResponse(
    Long id,
    Long mealPlanId,
    LocalDate weekStartDate
) {
}