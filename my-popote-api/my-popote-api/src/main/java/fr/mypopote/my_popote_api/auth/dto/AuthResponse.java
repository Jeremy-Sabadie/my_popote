package fr.mypopote.my_popote_api.auth.dto;

/**
 * Réponse renvoyée après une inscription
 * ou une connexion réussie.
 *
 * Le frontend reçoit les informations utiles
 * sur l'utilisateur ainsi que son JWT d'accès.
 */
public record AuthResponse(
    Long id,
    String email,
    String firstName,
    String accessToken
) {
}