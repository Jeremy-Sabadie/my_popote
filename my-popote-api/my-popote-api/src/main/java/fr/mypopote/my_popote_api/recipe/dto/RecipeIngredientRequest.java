package fr.mypopote.my_popote_api.recipe.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RecipeIngredientRequest(

    @NotBlank
    @Size(max = 150)
    String ingredientName,

    @NotNull
    @DecimalMin(value = "0.001")
    BigDecimal quantity,

    @NotBlank
    @Size(max = 30)
    String unit

) {
}