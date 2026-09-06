package fr.mypopote.my_popote_api.planning.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MealPlanRequest(

    @NotNull
    LocalDate weekStartDate,

    boolean includeWeekend,

    @DecimalMin(value = "0.00")
    BigDecimal maxBudget

) {
}