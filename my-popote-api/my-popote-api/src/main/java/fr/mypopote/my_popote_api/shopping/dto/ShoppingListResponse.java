package fr.mypopote.my_popote_api.shopping.dto;

import java.util.List;

/**
 * Liste de courses retournée au frontend.
 */
public record ShoppingListResponse(
    Long id,
    Long mealPlanId,
    List<ShoppingItemResponse> items
) {
}