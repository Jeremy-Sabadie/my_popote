package fr.mypopote.my_popote_api.shopping.dto;

import java.math.BigDecimal;

/**
 * Données d'un article renvoyées au frontend.
 */
public record ShoppingItemResponse(
    Long id,
    Long ingredientId,
    String ingredientName,
    BigDecimal quantity,
    String unit,
    boolean checked
) {
}