package fr.mypopote.my_popote_api.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Données nécessaires pour terminer
 * une réinitialisation de mot de passe.
 */
public record ResetPasswordRequest(

    @NotBlank
    String token,

    @NotBlank
    @Size(min = 8)
    String password

) {
}