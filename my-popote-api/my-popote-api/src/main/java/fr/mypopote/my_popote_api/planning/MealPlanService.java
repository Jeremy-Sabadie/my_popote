package fr.mypopote.my_popote_api.planning;

import fr.mypopote.my_popote_api.planning.dto.GenerateMealPlanRequest;
import fr.mypopote.my_popote_api.planning.dto.MealPlanResponse;
import fr.mypopote.my_popote_api.planning.dto.PlannedMealResponse;
import fr.mypopote.my_popote_api.recipe.Recipe;
import fr.mypopote.my_popote_api.recipe.RecipeRepository;
import fr.mypopote.my_popote_api.recipe.RecipeSeason;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Service métier chargé des plannings hebdomadaires.
 *
 * La génération respecte la saison et les tags choisis.
 * Le budget intervient uniquement si la proposition obtenue
 * avec ces préférences dépasse le plafond demandé.
 */
@Service
@Transactional(readOnly = true)
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final PlannedMealRepository plannedMealRepository;
    private final RecipeRepository recipeRepository;

    public MealPlanService(
        MealPlanRepository mealPlanRepository,
        PlannedMealRepository plannedMealRepository,
        RecipeRepository recipeRepository
    ) {
        this.mealPlanRepository = mealPlanRepository;
        this.plannedMealRepository = plannedMealRepository;
        this.recipeRepository = recipeRepository;
    }

    /**
     * Recherche un planning pour un utilisateur et une semaine.
     */
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
     * Génère une proposition de planning.
     *
     * Si un planning existe déjà pour cette semaine,
     * ses repas sont remplacés par une nouvelle proposition.
     */
    @Transactional
    public MealPlanResponse generate(
        Long userId,
        GenerateMealPlanRequest request
    ) {
        validateMonday(
            request.weekStartDate()
        );

        List<Recipe> recipes =
            loadRecipes(
                userId,
                request.recipeIds()
            );

        if (recipes.isEmpty()) {
            throw new IllegalArgumentException(
                "At least one recipe is required"
            );
        }

        MealPlan mealPlan =
            mealPlanRepository
                .findByUserIdAndWeekStartDate(
                    userId,
                    request.weekStartDate()
                )
                .orElseGet(() ->
                    new MealPlan(
                        recipes.get(0).getUser(),
                        request.weekStartDate(),
                        request.includeWeekend(),
                        request.maxBudget(),
                        BigDecimal.ZERO
                    )
                );

        mealPlan.setIncludeWeekend(
            request.includeWeekend()
        );

        mealPlan.setMaxBudget(
            request.maxBudget()
        );

        MealPlan savedPlan =
            mealPlanRepository.save(
                mealPlan
            );

        /*
         * Supprime l'ancienne proposition.
         */
        plannedMealRepository.deleteAllByMealPlanId(
            savedPlan.getId()
        );

        /*
         * Force les DELETE avant les nouveaux INSERT.
         *
         * Sans ce flush, MariaDB peut encore voir
         * les anciens créneaux et déclencher
         * uk_planned_meal_slot.
         */
        plannedMealRepository.flush();

        List<PlannedMeal> meals =
            generateMeals(
                savedPlan,
                recipes,
                request.preferredTagIds(),
                request.preferredSeason()
            );

        meals =
            plannedMealRepository.saveAll(
                meals
            );

        plannedMealRepository.flush();

        BigDecimal estimatedCost =
            calculateEstimatedCost(
                meals
            );

        savedPlan.setEstimatedCost(
            estimatedCost
        );

        mealPlanRepository.save(
            savedPlan
        );

        return toResponse(
            savedPlan,
            meals
        );
    }

    /**
     * Retourne le planning d'une semaine.
     */
    public MealPlanResponse getWeek(
        Long userId,
        LocalDate weekStartDate
    ) {
        MealPlan mealPlan =
            mealPlanRepository
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

        return toResponse(
            mealPlan,
            meals
        );
    }

    /**
     * Retourne l'historique des semaines générées.
     */
    public List<MealPlanResponse> getHistory(
        Long userId
    ) {
        return mealPlanRepository
            .findAllByUserIdOrderByWeekStartDateDesc(
                userId
            )
            .stream()
            .map(mealPlan -> {
                List<PlannedMeal> meals =
                    plannedMealRepository
                        .findAllByMealPlanIdOrderByMealDateAscMealTypeAsc(
                            mealPlan.getId()
                        );

                return toResponse(
                    mealPlan,
                    meals
                );
            })
            .toList();
    }

    /**
     * Retourne les repas planifiés pour une date précise.
     */
    public List<PlannedMealResponse> getMealsForDate(
        Long userId,
        LocalDate mealDate
    ) {
        return plannedMealRepository
            .findAllByMealPlanUserIdAndMealDate(
                userId,
                mealDate
            )
            .stream()
            .map(this::toMealResponse)
            .toList();
    }

    /**
     * Charge uniquement les recettes appartenant
     * à l'utilisateur connecté.
     */
    private List<Recipe> loadRecipes(
        Long userId,
        List<Long> recipeIds
    ) {
        if (
            recipeIds == null ||
            recipeIds.isEmpty()
        ) {
            throw new IllegalArgumentException(
                "At least one recipe is required"
            );
        }

        List<Recipe> recipes =
            new ArrayList<>();

        for (
            Long recipeId :
            recipeIds.stream()
                .distinct()
                .toList()
        ) {
            Recipe recipe =
                recipeRepository
                    .findByIdAndUserId(
                        recipeId,
                        userId
                    )
                    .orElseThrow(() ->
                        new IllegalArgumentException(
                            "Recipe not found: " + recipeId
                        )
                    );

            recipes.add(
                recipe
            );
        }

        return recipes;
    }

    /**
     * Génère les repas de la semaine.
     */
    private List<PlannedMeal> generateMeals(
        MealPlan mealPlan,
        List<Recipe> recipes,
        List<Long> preferredTagIds,
        String preferredSeason
    ) {
        List<Recipe> seasonalRecipes =
            selectRecipesForSeason(
                recipes,
                mealPlan.getWeekStartDate(),
                preferredSeason
            );

        List<Recipe> preferredRecipes =
            selectRecipesForPreferredTags(
                seasonalRecipes,
                preferredTagIds
            );

        List<PlannedMeal> preferredMeals =
            buildMeals(
                mealPlan,
                preferredRecipes
            );

        /*
         * Aucun budget :
         * le comportement historique est conservé.
         */
        if (mealPlan.getMaxBudget() == null) {
            return preferredMeals;
        }

        BigDecimal preferredCost =
            calculateEstimatedCost(
                preferredMeals
            );

        /*
         * La proposition préférée respecte déjà le budget.
         */
        if (
            preferredCost.compareTo(
                mealPlan.getMaxBudget()
            ) <= 0
        ) {
            return preferredMeals;
        }

        /*
         * Le budget est dépassé.
         *
         * On repart de toutes les recettes compatibles
         * avec la saison pour permettre des alternatives
         * moins coûteuses.
         */
        return buildBudgetAwareMeals(
            mealPlan,
            seasonalRecipes,
            preferredTagIds
        );
    }

    /**
     * Construit une semaine en rotation classique.
     */
    private List<PlannedMeal> buildMeals(
        MealPlan mealPlan,
        List<Recipe> recipes
    ) {
        List<PlannedMeal> meals =
            new ArrayList<>();

        int numberOfDays =
            mealPlan.isIncludeWeekend()
                ? 7
                : 5;

        int recipeIndex = 0;

        for (
            int day = 0;
            day < numberOfDays;
            day++
        ) {
            LocalDate mealDate =
                mealPlan
                    .getWeekStartDate()
                    .plusDays(day);

            for (
                String mealType :
                List.of(
                    "LUNCH",
                    "DINNER"
                )
            ) {
                Recipe recipe =
                    recipes.get(
                        recipeIndex
                            % recipes.size()
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
     * Construit une semaine en tenant compte du budget.
     *
     * Les recettes sont classées par préférence, mais la recherche
     * commence à une position différente à chaque repas.
     *
     * Cela évite que la première recette compatible soit choisie
     * systématiquement pendant toute la semaine.
     */
    private List<PlannedMeal> buildBudgetAwareMeals(
        MealPlan mealPlan,
        List<Recipe> recipes,
        List<Long> preferredTagIds
    ) {
        List<PlannedMeal> meals =
            new ArrayList<>();

        List<Recipe> orderedRecipes =
            orderRecipesByPreference(
                recipes,
                preferredTagIds
            );

        int numberOfDays =
            mealPlan.isIncludeWeekend()
                ? 7
                : 5;

        int totalMeals =
            numberOfDays * 2;

        BigDecimal currentCost =
            BigDecimal.ZERO;

        int recipeIndex = 0;

        for (
            int day = 0;
            day < numberOfDays;
            day++
        ) {
            LocalDate mealDate =
                mealPlan
                    .getWeekStartDate()
                    .plusDays(day);

            for (
                String mealType :
                List.of(
                    "LUNCH",
                    "DINNER"
                )
            ) {
                int remainingMeals =
                    totalMeals
                        - meals.size()
                        - 1;

                Recipe recipe =
                    selectRecipeWithinBudget(
                        orderedRecipes,
                        mealPlan.getMaxBudget(),
                        currentCost,
                        remainingMeals,
                        recipeIndex
                    );

                meals.add(
                    new PlannedMeal(
                        mealPlan,
                        recipe,
                        mealDate,
                        mealType
                    )
                );

                currentCost =
                    currentCost.add(
                        getRecipeCost(
                            recipe
                        )
                    );

                /*
                 * Le prochain repas commencera sa recherche
                 * sur la recette suivante.
                 */
                int selectedIndex =
                    orderedRecipes.indexOf(
                        recipe
                    );

                recipeIndex =
                    (selectedIndex + 1)
                        % orderedRecipes.size();
            }
        }

        return meals;
    }

    /**
     * Classe les recettes du meilleur score de tags
     * vers le moins bon.
     *
     * Le tri Java étant stable, deux recettes ayant
     * le même score conservent leur ordre d'origine.
     */
    private List<Recipe> orderRecipesByPreference(
        List<Recipe> recipes,
        List<Long> preferredTagIds
    ) {
        if (
            preferredTagIds == null ||
            preferredTagIds.isEmpty()
        ) {
            return recipes;
        }

        return recipes.stream()
            .sorted(
                Comparator.comparingInt(
                    (Recipe recipe) ->
                        calculatePreferredTagScore(
                            recipe,
                            preferredTagIds
                        )
                ).reversed()
            )
            .toList();
    }

    /**
     * Choisit une recette compatible avec le budget.
     *
     * La recherche commence à startIndex afin de répartir
     * les recettes sur la semaine au lieu de toujours
     * sélectionner la première recette de la liste.
     */
    private Recipe selectRecipeWithinBudget(
        List<Recipe> orderedRecipes,
        BigDecimal maxBudget,
        BigDecimal currentCost,
        int remainingMeals,
        int startIndex
    ) {
        BigDecimal cheapestCost =
            orderedRecipes.stream()
                .map(this::getRecipeCost)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        for (
            int offset = 0;
            offset < orderedRecipes.size();
            offset++
        ) {
            int index =
                (startIndex + offset)
                    % orderedRecipes.size();

            Recipe recipe =
                orderedRecipes.get(
                    index
                );

            BigDecimal projectedCost =
                currentCost
                    .add(
                        getRecipeCost(
                            recipe
                        )
                    )
                    .add(
                        cheapestCost.multiply(
                            BigDecimal.valueOf(
                                remainingMeals
                            )
                        )
                    );

            if (
                projectedCost.compareTo(
                    maxBudget
                ) <= 0
            ) {
                return recipe;
            }
        }

        /*
         * Même la solution minimale dépasse le budget.
         * On limite donc le dépassement.
         */
        return orderedRecipes.stream()
            .min(
                Comparator.comparing(
                    this::getRecipeCost
                )
            )
            .orElseThrow();
    }

    /**
     * Retourne le coût utilisé par l'algorithme.
     */
    private BigDecimal getRecipeCost(
        Recipe recipe
    ) {
        return recipe.getEstimatedCost() == null
            ? BigDecimal.ZERO
            : recipe.getEstimatedCost();
    }

    /**
     * Sélectionne les recettes compatibles avec
     * la saison choisie, ou celle de la semaine par défaut.
     *
     * Une recette sans saison est utilisable toute l'année.
     *
     * Si aucune recette n'est compatible,
     * toutes les recettes sont conservées.
     */
    private List<Recipe> selectRecipesForSeason(
        List<Recipe> recipes,
        LocalDate weekStartDate,
        String preferredSeason
    ) {
        String season =
            preferredSeason == null ||
            preferredSeason.isBlank()
                ? getSeason(
                    weekStartDate
                )
                : preferredSeason;

        List<Recipe> eligibleRecipes =
            recipes.stream()
                .filter(recipe ->
                    recipe.getSeasons().isEmpty() ||
                    recipe.getSeasons()
                        .stream()
                        .map(
                            RecipeSeason::getSeason
                        )
                        .anyMatch(recipeSeason ->
                            recipeSeason.equalsIgnoreCase(
                                season
                            )
                        )
                )
                .toList();

        if (eligibleRecipes.isEmpty()) {
            return recipes;
        }

        return eligibleRecipes;
    }

    /**
     * Privilégie les recettes correspondant
     * le mieux aux tags choisis par l'utilisateur.
     */
    private List<Recipe> selectRecipesForPreferredTags(
        List<Recipe> recipes,
        List<Long> preferredTagIds
    ) {
        if (
            preferredTagIds == null ||
            preferredTagIds.isEmpty()
        ) {
            return recipes;
        }

        int maxScore =
            recipes.stream()
                .mapToInt(recipe ->
                    calculatePreferredTagScore(
                        recipe,
                        preferredTagIds
                    )
                )
                .max()
                .orElse(0);

        if (maxScore == 0) {
            return recipes;
        }

        return recipes.stream()
            .filter(recipe ->
                calculatePreferredTagScore(
                    recipe,
                    preferredTagIds
                ) == maxScore
            )
            .toList();
    }

    /**
     * Calcule combien de tags préférés
     * sont présents sur une recette.
     */
    private int calculatePreferredTagScore(
        Recipe recipe,
        List<Long> preferredTagIds
    ) {
        return (int) recipe
            .getTags()
            .stream()
            .filter(tag ->
                tag.getId() != null &&
                preferredTagIds.contains(
                    tag.getId()
                )
            )
            .count();
    }

    /**
     * Détermine la saison utilisée pour la génération.
     */
    private String getSeason(
        LocalDate date
    ) {
        Month month =
            date.getMonth();

        return switch (month) {
            case DECEMBER,
                 JANUARY,
                 FEBRUARY ->
                "WINTER";

            case MARCH,
                 APRIL,
                 MAY ->
                "SPRING";

            case JUNE,
                 JULY,
                 AUGUST ->
                "SUMMER";

            case SEPTEMBER,
                 OCTOBER,
                 NOVEMBER ->
                "AUTUMN";
        };
    }

    /**
     * Calcule le coût estimé de la semaine.
     */
    private BigDecimal calculateEstimatedCost(
        List<PlannedMeal> meals
    ) {
        return meals.stream()
            .map(
                PlannedMeal::getRecipe
            )
            .map(
                Recipe::getEstimatedCost
            )
            .filter(
                cost -> cost != null
            )
            .reduce(
                BigDecimal.ZERO,
                BigDecimal::add
            );
    }

    /**
     * Vérifie que la semaine commence bien un lundi.
     */
    private void validateMonday(
        LocalDate weekStartDate
    ) {
        if (weekStartDate == null) {
            throw new IllegalArgumentException(
                "Week start date is required"
            );
        }

        if (
            weekStartDate.getDayOfWeek()
                != DayOfWeek.MONDAY
        ) {
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