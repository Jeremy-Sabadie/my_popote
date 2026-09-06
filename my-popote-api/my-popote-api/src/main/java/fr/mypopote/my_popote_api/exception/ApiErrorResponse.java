package fr.mypopote.my_popote_api.exception;

import java.time.Instant;
import java.util.Map;

/**
 * Format commun utilisé par l'API lorsqu'une requête échoue.
 *
 * Le frontend reçoit ainsi toujours une erreur structurée
 * plutôt qu'une réponse technique générée directement par Spring.
 */
public record ApiErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message,
    Map<String, String> validationErrors
) {
}