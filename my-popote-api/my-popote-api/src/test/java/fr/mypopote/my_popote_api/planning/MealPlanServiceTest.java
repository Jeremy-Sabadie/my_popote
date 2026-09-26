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
        LocalDate monday = LocalDate.of(2026, 9, 7);

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
            mealPlanRepository.findByUserIdAndWeekStartDate(
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
        LocalDate monday = LocalDate.of(2026, 9, 7);

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

        when(
            recipeRepository.findByIdAndUserId(
                10L,
                userId
            )
        ).thenReturn(
            Optional.of(recipe)
        );

        when(
            mealPlanRepository.findByUserIdAndWeekStartDate(
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
        LocalDate monday = LocalDate.of(2026, 7, 6);

        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        Recipe summerRecipe = new Recipe(
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

        Recipe winterRecipe = new Recipe(
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
                List.of(10L, 20L)
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
            mealPlanRepository.findByUserIdAndWeekStartDate(
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

        /*
         * La recette correspondant à la saison
         * doit être prioritaire.
         */
        assertThat(
            result.meals()
                .get(0)
                .recipeName()
        ).isEqualTo(
            "Salade de poulet"
        );

        /*
         * Mais la saison est une préférence,
         * pas un filtre exclusif.
         */
        assertThat(
            result.meals()
                .stream()
                .map(meal -> meal.recipeName())
                .distinct()
                .toList()
        ).containsExactlyInAnyOrder(
            "Salade de poulet",
            "Tartiflette"
        );
    }

    @Test
    void shouldUseSelectedSeasonInsteadOfCalendarSeason() {
        Long userId = 1L;
        LocalDate monday = LocalDate.of(2026, 9, 14);

        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        Recipe autumnRecipe = new Recipe(
            user,
            "Poêlée d'automne",
            "VEGETARIAN",
            2,
            new BigDecimal("5.00"),
            null
        );

        autumnRecipe.addSeason(
            "AUTUMN"
        );

        Recipe winterRecipe = new Recipe(
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
                List.of(10L, 20L),
                List.of(),
                "WINTER"
            );

        when(
            recipeRepository.findByIdAndUserId(
                10L,
                userId
            )
        ).thenReturn(
            Optional.of(autumnRecipe)
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
            mealPlanRepository.findByUserIdAndWeekStartDate(
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

        /*
         * WINTER a été explicitement sélectionné :
         * Tartiflette doit donc passer devant
         * la recette correspondant à la saison calendrier.
         */
        assertThat(
            result.meals()
                .get(0)
                .recipeName()
        ).isEqualTo(
            "Tartiflette"
        );

        /*
         * La recette d'automne reste néanmoins
         * disponible dans la rotation.
         */
        assertThat(
            result.meals()
                .stream()
                .map(meal -> meal.recipeName())
                .distinct()
                .toList()
        ).containsExactlyInAnyOrder(
            "Tartiflette",
            "Poêlée d'automne"
        );
    }

    @Test
    void shouldPreferRecipesMatchingPreferredTags() {
        Long userId = 1L;
        LocalDate monday = LocalDate.of(2026, 7, 6);

        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        Recipe preferredRecipe = new Recipe(
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

        Recipe otherRecipe = new Recipe(
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
                List.of(10L, 20L),
                List.of(100L)
            );

        when(
            recipeRepository.findByIdAndUserId(
                10L,
                userId
            )
        ).thenReturn(
            Optional.of(preferredRecipe)
        );

        when(
            recipeRepository.findByIdAndUserId(
                20L,
                userId
            )
        ).thenReturn(
            Optional.of(otherRecipe)
        );

        when(
            mealPlanRepository.findByUserIdAndWeekStartDate(
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

        /*
         * La recette correspondant au tag préféré
         * doit être prioritaire.
         */
        assertThat(
            result.meals()
                .get(0)
                .recipeName()
        ).isEqualTo(
            "Poulet protéiné"
        );

        /*
         * Mais le tag préféré ne doit plus
         * éliminer les autres recettes.
         */
        assertThat(
            result.meals()
                .stream()
                .map(meal -> meal.recipeName())
                .distinct()
                .toList()
        ).containsExactlyInAnyOrder(
            "Poulet protéiné",
            "Pâtes tomate"
        );
    }

    @Test
    void shouldRotateRecipesWhenSeveralRecipesMatchPreferredTags() {
        Long userId = 1L;
        LocalDate monday = LocalDate.of(2026, 9, 21);

        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        Tag preferredTag =
            mock(Tag.class);

        when(
            preferredTag.getId()
        ).thenReturn(
            100L
        );

        Recipe firstRecipe = new Recipe(
            user,
            "Poulet riz",
            "MEAT",
            2,
            new BigDecimal("5.00"),
            null
        );

        firstRecipe.addTag(
            preferredTag
        );

        Recipe secondRecipe = new Recipe(
            user,
            "Bowl patates",
            "MEAT",
            2,
            new BigDecimal("4.00"),
            null
        );

        secondRecipe.addTag(
            preferredTag
        );

        Recipe thirdRecipe = new Recipe(
            user,
            "Pâtes poulet",
            "MEAT",
            2,
            new BigDecimal("4.50"),
            null
        );

        thirdRecipe.addTag(
            preferredTag
        );

        GenerateMealPlanRequest request =
            new GenerateMealPlanRequest(
                monday,
                false,
                null,
                List.of(
                    10L,
                    20L,
                    30L
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
            recipeRepository.findByIdAndUserId(
                30L,
                userId
            )
        ).thenReturn(
            Optional.of(thirdRecipe)
        );

        when(
            mealPlanRepository.findByUserIdAndWeekStartDate(
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
                .stream()
                .map(meal -> meal.recipeName())
                .distinct()
                .count()
        ).isEqualTo(3);
    }

    @Test
    void shouldRespectBudgetWhileKeepingPreferredRecipesWhenPossible() {
        Long userId = 1L;
        LocalDate monday = LocalDate.of(2026, 7, 6);

        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        Recipe preferredRecipe = new Recipe(
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

        Recipe budgetRecipe = new Recipe(
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
            mealPlanRepository.findByUserIdAndWeekStartDate(
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
            result.estimatedCost()
        ).isLessThanOrEqualTo(
            new BigDecimal("40.00")
        );

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
        ).isGreaterThan(0);

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
        ).isGreaterThan(0);
    }
}