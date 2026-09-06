package fr.mypopote.my_popote_api.shopping.dto;

/**
 * Requête permettant uniquement de cocher ou décocher
 * un article de la liste de courses.
 */
public record ShoppingItemUpdateRequest(
    boolean checked
) {
}