package fr.mypopote.my_popote_api.recipe.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record IngredientRequest(

    @NotBlank
    @Size(max = 150)
    String name

) {
}