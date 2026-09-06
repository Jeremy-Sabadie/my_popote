package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.planning.dto.GenerateMealPlanRequest;
import fr.mypopote.my_popote_api.planning.dto.MealPlanResponse;
import fr.mypopote.my_popote_api.planning.dto.PlannedMealResponse;
import fr.mypopote.my_popote_api.recipe.Recipe;
import fr.mypopote.my_popote_api.recipe.RecipeRepository;
import fr.mypopote.my_popote_api.user.User;
import fr.mypopote.my_popote_api.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service chargé de la planification hebdomadaire.
 *
 * L'utilisateur choisit les recettes.
 * My Popote les répartit ensuite dans les créneaux de la semaine.
 */
@Service
@Transactional(readOnly = true)
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final PlannedMealRepository plannedMealRepository;
    private final RecipeRepository recipeRepository;
    private final UserRepository userRepository;

    public MealPlanService(
        MealPlanRepository mealPlanRepository,
        PlannedMealRepository plannedMealRepository,
        RecipeRepository recipeRepository,
        UserRepository userRepository
    ) {
        this.mealPlanRepository = mealPlanRepository;
        this.plannedMealRepository = plannedMealRepository;
        this.recipeRepository = recipeRepository;
        this.userRepository = userRepository;
    }

    public Optional<MealPlan> findByUserAndWeek(
        Long userId,
        LocalDate weekStartDate
    ) {
        return mealPlanRepository.findByUserIdAndWeekStartDate(
            userId,
            weekStartDate
        );
    }

    /**
     * Génère ou régénère une semaine complète.
     */
    @Transactional
    public MealPlanResponse generate(
        Long userId,
        GenerateMealPlanRequest request
    ) {
        validateMonday(request.weekStartDate());

        User user = userRepository.findById(userId)
            .orElseThrow(() ->
                new IllegalArgumentException("User not found")
            );

        List<Recipe> recipes = findOwnedRecipes(
            userId,
            request.recipeIds()
        );

        MealPlan mealPlan = mealPlanRepository
            .findByUserIdAndWeekStartDate(
                userId,
                request.weekStartDate()
            )
            .orElseGet(() ->
                new MealPlan(
                    user,
                    request.weekStartDate(),
                    request.includeWeekend(),
                    request.maxBudget(),
                    BigDecimal.ZERO
                )
            );

        mealPlan.setIncludeWeekend(request.includeWeekend());
        mealPlan.setMaxBudget(request.maxBudget());

        MealPlan savedPlan =
            mealPlanRepository.save(mealPlan);

        /*
         * Régénérer une semaine remplace les anciens créneaux.
         */
        plannedMealRepository.deleteAllByMealPlanId(
            savedPlan.getId()
        );

        List<PlannedMeal> generatedMeals =
            generateMeals(savedPlan, recipes);

        plannedMealRepository.saveAll(generatedMeals);

        BigDecimal estimatedCost =
            calculateEstimatedCost(generatedMeals);

        savedPlan.setEstimatedCost(estimatedCost);
        mealPlanRepository.save(savedPlan);

        return toResponse(savedPlan, generatedMeals);
    }

    /**
     * Retourne une semaine avec tous ses repas.
     */
    public MealPlanResponse getWeek(
        Long userId,
        LocalDate weekStartDate
    ) {
        MealPlan mealPlan = mealPlanRepository
            .findByUserIdAndWeekStartDate(
                userId,
                weekStartDate
            )
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Meal plan not found"
                )
            );

        List<PlannedMeal> meals =
            plannedMealRepository
                .findAllByMealPlanIdOrderByMealDateAscMealTypeAsc(
                    mealPlan.getId()
                );

        return toResponse(mealPlan, meals);
    }

    /**
     * Retourne l'historique des semaines.
     */
    public List<MealPlanResponse> getHistory(Long userId) {
        return mealPlanRepository
            .findAllByUserIdOrderByWeekStartDateDesc(userId)
            .stream()
            .map(mealPlan -> {
                List<PlannedMeal> meals =
                    plannedMealRepository
                        .findAllByMealPlanIdOrderByMealDateAscMealTypeAsc(
                            mealPlan.getId()
                        );

                return toResponse(mealPlan, meals);
            })
            .toList();
    }

    /**
     * Retourne les repas d'une date donnée.
     * L'écran Aujourd'hui utilisera la date courante.
     */
    public List<PlannedMealResponse> getMealsForDate(
        Long userId,
        LocalDate date
    ) {
        return plannedMealRepository
            .findAllByMealPlanUserIdAndMealDate(
                userId,
                date
            )
            .stream()
            .map(this::toMealResponse)
            .toList();
    }

    private List<Recipe> findOwnedRecipes(
        Long userId,
        List<Long> recipeIds
    ) {
        List<Recipe> recipes = new ArrayList<>();

        for (Long recipeId : recipeIds) {
            Recipe recipe = recipeRepository
                .findByIdAndUserId(recipeId, userId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Recipe not found: " + recipeId
                    )
                );

            recipes.add(recipe);
        }

        return recipes;
    }

    /**
     * Répartit les recettes en rotation.
     *
     * Sans week-end : 5 jours x 2 repas = 10.
     * Avec week-end : 7 jours x 2 repas = 14.
     */
    private List<PlannedMeal> generateMeals(
        MealPlan mealPlan,
        List<Recipe> recipes
    ) {
        List<PlannedMeal> meals = new ArrayList<>();

        int numberOfDays =
            mealPlan.isIncludeWeekend() ? 7 : 5;

        int recipeIndex = 0;

        for (int day = 0; day < numberOfDays; day++) {
            LocalDate mealDate =
                mealPlan.getWeekStartDate().plusDays(day);

            for (String mealType :
                List.of("LUNCH", "DINNER")) {

                Recipe recipe =
                    recipes.get(
                        recipeIndex % recipes.size()
                    );

                meals.add(
                    new PlannedMeal(
                        mealPlan,
                        recipe,
                        mealDate,
                        mealType
                    )
                );

                recipeIndex++;
            }
        }

        return meals;
    }

    /**
     * Calcule le coût estimé de la semaine.
     */
    private BigDecimal calculateEstimatedCost(
        List<PlannedMeal> meals
    ) {
        return meals.stream()
            .map(PlannedMeal::getRecipe)
            .map(Recipe::getEstimatedCost)
            .filter(cost -> cost != null)
            .reduce(
                BigDecimal.ZERO,
                BigDecimal::add
            );
    }

    private void validateMonday(
        LocalDate weekStartDate
    ) {
        if (weekStartDate.getDayOfWeek()
            != DayOfWeek.MONDAY) {

            throw new IllegalArgumentException(
                "Week start date must be a Monday"
            );
        }
    }

    private MealPlanResponse toResponse(
        MealPlan mealPlan,
        List<PlannedMeal> meals
    ) {
        return new MealPlanResponse(
            mealPlan.getId(),
            mealPlan.getWeekStartDate(),
            mealPlan.isIncludeWeekend(),
            mealPlan.getMaxBudget(),
            mealPlan.getEstimatedCost(),
            meals.stream()
                .map(this::toMealResponse)
                .toList()
        );
    }

    private PlannedMealResponse toMealResponse(
        PlannedMeal meal
    ) {
        return new PlannedMealResponse(
            meal.getId(),
            meal.getRecipe().getId(),
            meal.getRecipe().getName(),
            meal.getMealDate(),
            meal.getMealType()
        );
    }
}