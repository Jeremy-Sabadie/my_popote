package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.planning.dto.GenerateMealPlanRequest;
import fr.mypopote.my_popote_api.planning.dto.MealPlanResponse;
import fr.mypopote.my_popote_api.recipe.Recipe;
import fr.mypopote.my_popote_api.recipe.RecipeRepository;
import fr.mypopote.my_popote_api.user.User;
import fr.mypopote.my_popote_api.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MealPlanServiceTest {

    @Mock
    private MealPlanRepository mealPlanRepository;

    @Mock
    private PlannedMealRepository plannedMealRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MealPlanService mealPlanService;

    @Test
    void shouldGenerateTenMealsWithoutWeekend() {
        Long userId = 1L;
        LocalDate monday =
            LocalDate.of(2026, 9, 7);

        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        Recipe firstRecipe = new Recipe(
            user,
            "Poulet curry",
            "MEAT",
            2,
            new BigDecimal("5.00"),
            null
        );

        Recipe secondRecipe = new Recipe(
            user,
            "Pâtes tomate",
            "VEGETARIAN",
            2,
            new BigDecimal("3.00"),
            null
        );

        GenerateMealPlanRequest request =
            new GenerateMealPlanRequest(
                monday,
                false,
                new BigDecimal("60.00"),
                List.of(10L, 20L)
            );

        when(userRepository.findById(userId))
            .thenReturn(Optional.of(user));

        when(
            recipeRepository.findByIdAndUserId(
                10L,
                userId
            )
        ).thenReturn(Optional.of(firstRecipe));

        when(
            recipeRepository.findByIdAndUserId(
                20L,
                userId
            )
        ).thenReturn(Optional.of(secondRecipe));

        when(
            mealPlanRepository
                .findByUserIdAndWeekStartDate(
                    userId,
                    monday
                )
        ).thenReturn(Optional.empty());

        when(
            mealPlanRepository.save(
                any(MealPlan.class)
            )
        ).thenAnswer(
            invocation -> invocation.getArgument(0)
        );

        when(
            plannedMealRepository.saveAll(
                anyList()
            )
        ).thenAnswer(
            invocation -> invocation.getArgument(0)
        );

        MealPlanResponse result =
            mealPlanService.generate(
                userId,
                request
            );

        assertThat(result.meals())
            .hasSize(10);

        assertThat(result.meals().get(0).mealDate())
            .isEqualTo(monday);

        assertThat(result.meals().get(0).mealType())
            .isEqualTo("LUNCH");

        assertThat(result.meals().get(1).mealType())
            .isEqualTo("DINNER");

        assertThat(result.meals().get(9).mealDate())
            .isEqualTo(monday.plusDays(4));

        assertThat(result.estimatedCost())
            .isEqualByComparingTo("40.00");
    }

    @Test
    void shouldGenerateFourteenMealsWithWeekend() {
        Long userId = 1L;
        LocalDate monday =
            LocalDate.of(2026, 9, 7);

        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        Recipe recipe = new Recipe(
            user,
            "Poulet curry",
            "MEAT",
            2,
            new BigDecimal("5.00"),
            null
        );

        GenerateMealPlanRequest request =
            new GenerateMealPlanRequest(
                monday,
                true,
                null,
                List.of(10L)
            );

        when(userRepository.findById(userId))
            .thenReturn(Optional.of(user));

        when(
            recipeRepository.findByIdAndUserId(
                10L,
                userId
            )
        ).thenReturn(Optional.of(recipe));

        when(
            mealPlanRepository
                .findByUserIdAndWeekStartDate(
                    userId,
                    monday
                )
        ).thenReturn(Optional.empty());

        when(
            mealPlanRepository.save(
                any(MealPlan.class)
            )
        ).thenAnswer(
            invocation -> invocation.getArgument(0)
        );

        when(
            plannedMealRepository.saveAll(
                anyList()
            )
        ).thenAnswer(
            invocation -> invocation.getArgument(0)
        );

        MealPlanResponse result =
            mealPlanService.generate(
                userId,
                request
            );

        assertThat(result.meals())
            .hasSize(14);

        assertThat(
            result.meals().get(13).mealDate()
        ).isEqualTo(
            monday.plusDays(6)
        );
    }
}