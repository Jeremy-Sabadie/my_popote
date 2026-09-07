package fr.mypopote.my_popote_api.shopping.dto;

import java.util.List;

/**
 * Liste de courses retournée au frontend.
 *
 * Les articles que l'utilisateur possède déjà sont séparés
 * des articles qu'il doit réellement acheter.
 */
public record ShoppingListResponse(
    Long id,
    Long mealPlanId,
    List<ShoppingItemResponse> items,
    List<ShoppingItemResponse> alreadyOwnedItems
) {

    /**
     * Constructeur conservé pour les usages existants.
     */
    public ShoppingListResponse(
            Long id,
            Long mealPlanId,
            List<ShoppingItemResponse> items) {

        this(
            id,
            mealPlanId,
            items,
            List.of()
        );
    }
}