package fr.mypopote.my_popote_api.recipe.dto;

/**
 * Représentation REST d'un tag associé à une recette.
 *
 * L'identifiant permettra au frontend de sélectionner un tag,
 * tandis que le nom et le groupe serviront à son affichage.
 */
public record TagResponse(
    Long id,
    String name,
    String groupName
) {
}