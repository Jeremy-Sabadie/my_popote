package fr.mypopote.my_popote_api.shopping.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Représente une envie ajoutée manuellement à la liste de courses.
 */
public record ManualShoppingItemRequest(

    @NotBlank(message = "Item name is required")
    @Size(max = 150)
    String name,

    @NotNull(message = "Quantity is required")
    @DecimalMin(
        value = "0.001",
        message = "Quantity must be greater than zero"
    )
    BigDecimal quantity,

    @NotBlank(message = "Unit is required")
    @Size(max = 30)
    String unit

) {
}