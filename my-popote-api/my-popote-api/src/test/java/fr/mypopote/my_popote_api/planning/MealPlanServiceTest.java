package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.planning.dto.GenerateMealPlanRequest;
import fr.mypopote.my_popote_api.planning.dto.MealPlanResponse;
import fr.mypopote.my_popote_api.recipe.Recipe;
import fr.mypopote.my_popote_api.recipe.RecipeRepository;
import fr.mypopote.my_popote_api.recipe.Tag;
import fr.mypopote.my_popote_api.user.User;

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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MealPlanServiceTest {

    @Mock
    private MealPlanRepository mealPlanRepository;

    @Mock
    private PlannedMealRepository plannedMealRepository;

    @Mock
    private RecipeRepository recipeRepository;

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

        Recipe firstRecipe =
            new Recipe(
                user,
                "Poulet curry",
                "MEAT",
                2,
                new BigDecimal("5.00"),
                null
            );

        Recipe secondRecipe =
            new Recipe(
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
                List.of(
                    10L,
                    20L
                )
            );

        when(
            recipeRepository.findByIdAndUserId(
                10L,
                userId
            )
        ).thenReturn(
            Optional.of(firstRecipe)
        );

        when(
            recipeRepository.findByIdAndUserId(
                20L,
                userId
            )
        ).thenReturn(
            Optional.of(secondRecipe)
        );

        when(
            mealPlanRepository
                .findByUserIdAndWeekStartDate(
                    userId,
                    monday
                )
        ).thenReturn(
            Optional.empty()
        );

        when(
            mealPlanRepository.save(
                any(MealPlan.class)
            )
        ).thenAnswer(
            invocation ->
                invocation.getArgument(0)
        );

        when(
            plannedMealRepository.saveAll(
                anyList()
            )
        ).thenAnswer(
            invocation ->
                invocation.getArgument(0)
        );

        MealPlanResponse result =
            mealPlanService.generate(
                userId,
                request
            );

        assertThat(
            result.meals()
        ).hasSize(10);

        assertThat(
            result.meals()
                .get(0)
                .mealDate()
        ).isEqualTo(
            monday
        );

        assertThat(
            result.meals()
                .get(0)
                .mealType()
        ).isEqualTo(
            "LUNCH"
        );

        assertThat(
            result.meals()
                .get(1)
                .mealType()
        ).isEqualTo(
            "DINNER"
        );

        assertThat(
            result.meals()
                .get(9)
                .mealDate()
        ).isEqualTo(
            monday.plusDays(4)
        );

        assertThat(
            result.estimatedCost()
        ).isEqualByComparingTo(
            "40.00"
        );
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

        Recipe recipe =
            new Recipe(
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

        when(
            recipeRepository.findByIdAndUserId(
                10L,
                userId
            )
        ).thenReturn(
            Optional.of(recipe)
        );

        when(
            mealPlanRepository
                .findByUserIdAndWeekStartDate(
                    userId,
                    monday
                )
        ).thenReturn(
            Optional.empty()
        );

        when(
            mealPlanRepository.save(
                any(MealPlan.class)
            )
        ).thenAnswer(
            invocation ->
                invocation.getArgument(0)
        );

        when(
            plannedMealRepository.saveAll(
                anyList()
            )
        ).thenAnswer(
            invocation ->
                invocation.getArgument(0)
        );

        MealPlanResponse result =
            mealPlanService.generate(
                userId,
                request
            );

        assertThat(
            result.meals()
        ).hasSize(14);

        assertThat(
            result.meals()
                .get(13)
                .mealDate()
        ).isEqualTo(
            monday.plusDays(6)
        );
    }

    @Test
    void shouldPreferRecipesMatchingWeekSeason() {
        Long userId = 1L;

        LocalDate monday =
            LocalDate.of(2026, 7, 6);

        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        Recipe summerRecipe =
            new Recipe(
                user,
                "Salade de poulet",
                "MEAT",
                2,
                new BigDecimal("5.00"),
                null
            );

        summerRecipe.addSeason(
            "SUMMER"
        );

        Recipe winterRecipe =
            new Recipe(
                user,
                "Tartiflette",
                "MEAT",
                2,
                new BigDecimal("7.00"),
                null
            );

        winterRecipe.addSeason(
            "WINTER"
        );

        GenerateMealPlanRequest request =
            new GenerateMealPlanRequest(
                monday,
                false,
                null,
                List.of(
                    10L,
                    20L
                )
            );

        when(
            recipeRepository.findByIdAndUserId(
                10L,
                userId
            )
        ).thenReturn(
            Optional.of(summerRecipe)
        );

        when(
            recipeRepository.findByIdAndUserId(
                20L,
                userId
            )
        ).thenReturn(
            Optional.of(winterRecipe)
        );

        when(
            mealPlanRepository
                .findByUserIdAndWeekStartDate(
                    userId,
                    monday
                )
        ).thenReturn(
            Optional.empty()
        );

        when(
            mealPlanRepository.save(
                any(MealPlan.class)
            )
        ).thenAnswer(
            invocation ->
                invocation.getArgument(0)
        );

        when(
            plannedMealRepository.saveAll(
                anyList()
            )
        ).thenAnswer(
            invocation ->
                invocation.getArgument(0)
        );

        MealPlanResponse result =
            mealPlanService.generate(
                userId,
                request
            );

        assertThat(
            result.meals()
        ).hasSize(10);

        assertThat(
            result.meals()
        ).allSatisfy(
            meal ->
                assertThat(
                    meal.recipeName()
                ).isEqualTo(
                    "Salade de poulet"
                )
        );
    }

    @Test
    void shouldPreferRecipesMatchingPreferredTags() {
        Long userId = 1L;

        LocalDate monday =
            LocalDate.of(2026, 7, 6);

        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        Recipe preferredRecipe =
            new Recipe(
                user,
                "Poulet protéiné",
                "MEAT",
                2,
                new BigDecimal("5.00"),
                null
            );

        preferredRecipe.addSeason(
            "SUMMER"
        );

        Tag preferredTag =
            mock(Tag.class);

        when(
            preferredTag.getId()
        ).thenReturn(
            100L
        );

        preferredRecipe.addTag(
            preferredTag
        );

        Recipe otherRecipe =
            new Recipe(
                user,
                "Pâtes tomate",
                "VEGETARIAN",
                2,
                new BigDecimal("3.00"),
                null
            );

        otherRecipe.addSeason(
            "SUMMER"
        );

        GenerateMealPlanRequest request =
            new GenerateMealPlanRequest(
                monday,
                false,
                null,
                List.of(
                    10L,
                    20L
                ),
                List.of(
                    100L
                )
            );

        when(
            recipeRepository.findByIdAndUserId(
                10L,
                userId
            )
        ).thenReturn(
            Optional.of(
                preferredRecipe
            )
        );

        when(
            recipeRepository.findByIdAndUserId(
                20L,
                userId
            )
        ).thenReturn(
            Optional.of(
                otherRecipe
            )
        );

        when(
            mealPlanRepository
                .findByUserIdAndWeekStartDate(
                    userId,
                    monday
                )
        ).thenReturn(
            Optional.empty()
        );

        when(
            mealPlanRepository.save(
                any(MealPlan.class)
            )
        ).thenAnswer(
            invocation ->
                invocation.getArgument(0)
        );

        when(
            plannedMealRepository.saveAll(
                anyList()
            )
        ).thenAnswer(
            invocation ->
                invocation.getArgument(0)
        );

        MealPlanResponse result =
            mealPlanService.generate(
                userId,
                request
            );

        assertThat(
            result.meals()
        ).hasSize(10);

        assertThat(
            result.meals()
        ).allSatisfy(
            meal ->
                assertThat(
                    meal.recipeName()
                ).isEqualTo(
                    "Poulet protéiné"
                )
        );
    }

    @Test
    void shouldRespectBudgetWhileKeepingPreferredRecipesWhenPossible() {
        Long userId = 1L;

        LocalDate monday =
            LocalDate.of(2026, 7, 6);

        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        /*
         * Cette recette correspond à la préférence choisie,
         * mais elle ne peut pas remplir seule toute la semaine
         * sans dépasser le budget.
         */
        Recipe preferredRecipe =
            new Recipe(
                user,
                "Poulet protéiné",
                "MEAT",
                2,
                new BigDecimal("5.00"),
                null
            );

        preferredRecipe.addSeason(
            "SUMMER"
        );

        Tag preferredTag =
            mock(Tag.class);

        when(
            preferredTag.getId()
        ).thenReturn(
            100L
        );

        preferredRecipe.addTag(
            preferredTag
        );

        /*
         * Cette recette moins chère permet de compléter
         * la semaine lorsque le budget ne permet pas
         * d'utiliser uniquement la recette préférée.
         */
        Recipe budgetRecipe =
            new Recipe(
                user,
                "Pâtes tomate",
                "VEGETARIAN",
                2,
                new BigDecimal("3.00"),
                null
            );

        budgetRecipe.addSeason(
            "SUMMER"
        );

        GenerateMealPlanRequest request =
            new GenerateMealPlanRequest(
                monday,
                false,
                new BigDecimal("40.00"),
                List.of(
                    10L,
                    20L
                ),
                List.of(
                    100L
                )
            );

        when(
            recipeRepository.findByIdAndUserId(
                10L,
                userId
            )
        ).thenReturn(
            Optional.of(
                preferredRecipe
            )
        );

        when(
            recipeRepository.findByIdAndUserId(
                20L,
                userId
            )
        ).thenReturn(
            Optional.of(
                budgetRecipe
            )
        );

        when(
            mealPlanRepository
                .findByUserIdAndWeekStartDate(
                    userId,
                    monday
                )
        ).thenReturn(
            Optional.empty()
        );

        when(
            mealPlanRepository.save(
                any(MealPlan.class)
            )
        ).thenAnswer(
            invocation ->
                invocation.getArgument(0)
        );

        when(
            plannedMealRepository.saveAll(
                anyList()
            )
        ).thenAnswer(
            invocation ->
                invocation.getArgument(0)
        );

        MealPlanResponse result =
            mealPlanService.generate(
                userId,
                request
            );

        /*
         * Le budget devient une vraie contrainte
         * de la proposition générée.
         */
        assertThat(
            result.estimatedCost()
        ).isLessThanOrEqualTo(
            new BigDecimal("40.00")
        );

        /*
         * Les préférences restent prioritaires :
         * on conserve autant que possible
         * des recettes correspondant aux tags choisis.
         */
        assertThat(
            result.meals()
                .stream()
                .filter(meal ->
                    meal.recipeName()
                        .equals(
                            "Poulet protéiné"
                        )
                )
                .count()
        ).isGreaterThan(
            0
        );

        /*
         * L'alternative économique doit pouvoir être utilisée
         * pour éviter le dépassement du budget.
         */
        assertThat(
            result.meals()
                .stream()
                .filter(meal ->
                    meal.recipeName()
                        .equals(
                            "Pâtes tomate"
                        )
                )
                .count()
        ).isGreaterThan(
            0
        );
    }
}