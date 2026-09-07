package fr.mypopote.my_popote_api.shopping.dto;

/**
 * Permet d'indiquer si un article doit réellement être acheté.
 *
 * alreadyOwned = true correspond côté IHM à :
 * "C'est bon, j'en ai déjà".
 */
public record ShoppingItemAlreadyOwnedRequest(
    boolean alreadyOwned
) {
}