package fr.mypopote.my_popote_api.shopping.dto;

import java.math.BigDecimal;

/**
 * Article d'une liste de courses renvoyé au frontend.
 */
public record ShoppingItemResponse(
    Long id,
    Long ingredientId,
    String ingredientName,
    BigDecimal quantity,
    String unit,
    boolean checked,
    boolean alreadyOwned,
    boolean manual
) {

    /**
     * Constructeur conservé pour les usages existants
     * écrits avant l'ajout de la personnalisation.
     */
    public ShoppingItemResponse(
            Long id,
            Long ingredientId,
            String ingredientName,
            BigDecimal quantity,
            String unit,
            boolean checked) {

        this(
            id,
            ingredientId,
            ingredientName,
            quantity,
            unit,
            checked,
            false,
            false
        );
    }
}