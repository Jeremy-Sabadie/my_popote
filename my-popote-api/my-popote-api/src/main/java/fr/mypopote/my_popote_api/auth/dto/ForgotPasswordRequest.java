package fr.mypopote.my_popote_api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Demande de réinitialisation initiée depuis l'écran de connexion.
 */
public record ForgotPasswordRequest(

    @NotBlank
    @Email
    String email

) {
}