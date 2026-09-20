package fr.mypopote.my_popote_api.shopping.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Adresse choisie par l'utilisateur pour recevoir sa liste de courses.
 */
public record SendShoppingListEmailRequest(
    @NotBlank @Email String recipient
) {
}