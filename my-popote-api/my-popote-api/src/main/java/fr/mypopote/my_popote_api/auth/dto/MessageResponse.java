package fr.mypopote.my_popote_api.auth.dto;

/**
 * Réponse simple utilisée par les opérations
 * d'authentification qui ne retournent pas de JWT.
 */
public record MessageResponse(
    String message
) {
}