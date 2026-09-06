package fr.mypopote.my_popote_api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Identifiants reçus lors d'une tentative de connexion.
 *
 * La validation vérifie uniquement la forme des données.
 * La vérification réelle des identifiants reste la
 * responsabilité de AuthService.
 */
public record LoginRequest(

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,

    @NotBlank(message = "Password is required")
    String password
) {
}