package fr.mypopote.my_popote_api.planning.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record MealPlanResponse(
    Long id,
    LocalDate weekStartDate,
    boolean includeWeekend,
    BigDecimal maxBudget,
    BigDecimal estimatedCost,
    List<PlannedMealResponse> meals
) {
}