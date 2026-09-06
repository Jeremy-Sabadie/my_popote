package fr.mypopote.my_popote_api.recipe.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public record RecipeRequest(

    @NotBlank
    @Size(max = 150)
    String name,

    @NotBlank
    @Size(max = 50)
    String category,

    @NotNull
    @Positive
    Integer servings,

    @DecimalMin(value = "0.00")
    BigDecimal estimatedCost,

    @Size(max = 10000)
    String instructions,

    @NotEmpty
    List<@Valid RecipeIngredientRequest> ingredients,

    @NotEmpty
    Set<
        @NotBlank
        @Size(max = 20)
        String
    > seasons

) {
}