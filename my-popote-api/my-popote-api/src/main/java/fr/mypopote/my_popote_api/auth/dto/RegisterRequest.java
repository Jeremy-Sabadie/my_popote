package fr.mypopote.my_popote_api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Données nécessaires à la création d'un compte.
 *
 * La validation est réalisée dès l'entrée dans l'API afin
 * de ne jamais transmettre de données manifestement invalides
 * à la couche métier.
 */
public record RegisterRequest(

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    String email,

    /**
     * Huit caractères constituent notre minimum pour le MVP.
     *
     * Le mot de passe sera ensuite hashé avec BCrypt :
     * il ne doit jamais être enregistré en clair.
     */
    @NotBlank(message = "Password is required")
    @Size(
        min = 8,
        message = "Password must contain at least 8 characters"
    )
    String password,

    @NotBlank(message = "First name is required")
    String firstName
) {
}