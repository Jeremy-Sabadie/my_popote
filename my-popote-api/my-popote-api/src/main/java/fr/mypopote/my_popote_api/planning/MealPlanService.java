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
import java.util.List;
import java.util.Optional;

/**
 * Service métier chargé des plannings hebdomadaires.
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
                request.preferredTagIds()
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
     * Répartit les recettes en rotation.
     *
     * La saison est prise en compte en premier.
     *
     * Les tags choisis par l'utilisateur servent ensuite
     * à privilégier les recettes qui correspondent
     * le mieux à ses préférences de la semaine.
     *
     * Sans week-end : 5 jours x 2 repas = 10.
     * Avec week-end : 7 jours x 2 repas = 14.
     */
    private List<PlannedMeal> generateMeals(
        MealPlan mealPlan,
        List<Recipe> recipes,
        List<Long> preferredTagIds
    ) {
        List<PlannedMeal> meals =
            new ArrayList<>();

        List<Recipe> seasonalRecipes =
            selectRecipesForSeason(
                recipes,
                mealPlan.getWeekStartDate()
            );

        List<Recipe> eligibleRecipes =
            selectRecipesForPreferredTags(
                seasonalRecipes,
                preferredTagIds
            );

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
                    eligibleRecipes.get(
                        recipeIndex
                            % eligibleRecipes.size()
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
     * Sélectionne les recettes compatibles avec
     * la saison correspondant à la semaine.
     *
     * Une recette sans saison est utilisable toute l'année.
     *
     * Si aucune recette n'est compatible,
     * toutes les recettes sont conservées
     * afin de ne jamais bloquer la génération.
     */
    private List<Recipe> selectRecipesForSeason(
        List<Recipe> recipes,
        LocalDate weekStartDate
    ) {
        String season =
            getSeason(
                weekStartDate
            );

        List<Recipe> eligibleRecipes =
            recipes.stream()
                .filter(recipe ->
                    recipe.getSeasons().isEmpty() ||
                    recipe.getSeasons()
                        .stream()
                        .map(RecipeSeason::getSeason)
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
     *
     * Chaque tag correspondant vaut un point.
     *
     * Exemple :
     * - Sèche + Protéiné = score 2
     * - Protéiné uniquement = score 1
     * - Aucun des deux = score 0
     *
     * Seules les recettes ayant le meilleur score
     * sont utilisées.
     *
     * Si aucun tag n'est choisi ou si aucune recette
     * ne correspond aux préférences, la sélection
     * saisonnière est conservée telle quelle.
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

        /*
         * Aucun tag préféré n'est présent
         * dans les recettes disponibles.
         */
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
     *
     * La première version repose volontairement sur
     * les saisons météorologiques par mois complet.
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