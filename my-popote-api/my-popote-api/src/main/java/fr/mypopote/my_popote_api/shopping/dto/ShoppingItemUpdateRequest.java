package fr.mypopote.my_popote_api.shopping.dto;

/**
 * Requête permettant de cocher ou décocher
 * un article pendant les courses.
 */
public record ShoppingItemUpdateRequest(
    boolean checked
) {
}