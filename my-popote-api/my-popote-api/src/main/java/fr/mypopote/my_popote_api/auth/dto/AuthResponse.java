package fr.mypopote.my_popote_api.auth.dto;

/**
 * Informations utilisateur pouvant être retournées par l'API.
 *
 * Le hash du mot de passe n'apparaît volontairement jamais
 * dans ce DTO.
 *
 * Le futur mécanisme d'authentification (token ou autre)
 * complétera cette réponse si nécessaire.
 */
public record AuthResponse(
    Long id,
    String email,
    String firstName
) {
}