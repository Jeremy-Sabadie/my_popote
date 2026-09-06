package fr.mypopote.my_popote_api.shopping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fr.mypopote.my_popote_api.planning.MealPlan;
import fr.mypopote.my_popote_api.planning.MealPlanRepository;
import fr.mypopote.my_popote_api.planning.PlannedMeal;
import fr.mypopote.my_popote_api.planning.PlannedMealRepository;
import fr.mypopote.my_popote_api.recipe.Ingredient;
import fr.mypopote.my_popote_api.recipe.RecipeIngredient;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingItemResponse;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingListResponse;

/**
 * Gère la génération et la consultation des listes de courses.
 */
@Service
@Transactional(readOnly = true)
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingItemRepository shoppingItemRepository;
    private final MealPlanRepository mealPlanRepository;
    private final PlannedMealRepository plannedMealRepository;

    public ShoppingListService(
            ShoppingListRepository shoppingListRepository,
            ShoppingItemRepository shoppingItemRepository,
            MealPlanRepository mealPlanRepository,
            PlannedMealRepository plannedMealRepository) {

        this.shoppingListRepository = shoppingListRepository;
        this.shoppingItemRepository = shoppingItemRepository;
        this.mealPlanRepository = mealPlanRepository;
        this.plannedMealRepository = plannedMealRepository;
    }

    /**
     * Génère la liste de courses depuis un planning appartenant
     * à l'utilisateur connecté.
     *
     * Si une liste existe déjà, son contenu est recalculé.
     */
    @Transactional
    public ShoppingListResponse generate(Long mealPlanId, Long userId) {

        // La recherche par userId garantit que le planning appartient
        // bien à l'utilisateur connecté.
        MealPlan mealPlan = mealPlanRepository
            .findByIdAndUserId(mealPlanId, userId)
            .orElseThrow(() ->
                new IllegalArgumentException("Meal plan not found")
            );

        List<PlannedMeal> plannedMeals =
            plannedMealRepository
                .findAllByMealPlanIdOrderByMealDateAscMealTypeAsc(
                    mealPlanId
                );

        // Une liste est unique pour un planning.
        // On réutilise donc la liste existante lors d'une régénération.
        ShoppingList shoppingList = shoppingListRepository
            .findByMealPlanIdAndMealPlanUserId(mealPlanId, userId)
            .orElseGet(() -> new ShoppingList(mealPlan));

        shoppingList = shoppingListRepository.save(shoppingList);

        // On repart du planning actuel afin d'éviter de conserver
        // des articles provenant d'une ancienne version de la semaine.
        shoppingItemRepository.deleteAllByShoppingListId(
            shoppingList.getId()
        );

        /*
         * Clé = ingrédient + unité.
         *
         * Exemple :
         * 200 g de tomate + 300 g de tomate = 500 g.
         *
         * On ne mélange volontairement pas des unités différentes
         * comme "g" et "PIECE".
         */
        Map<IngredientUnitKey, AggregatedIngredient> aggregated =
            new LinkedHashMap<>();

        // Chaque apparition d'une recette dans le planning compte.
        // Une recette prévue deux fois ajoute donc deux fois ses ingrédients.
        for (PlannedMeal plannedMeal : plannedMeals) {

            for (RecipeIngredient recipeIngredient :
                    plannedMeal.getRecipe().getIngredients()) {

                Ingredient ingredient =
                    recipeIngredient.getIngredient();

                String unit = recipeIngredient.getUnit();

                IngredientUnitKey key =
                    new IngredientUnitKey(
                        ingredient.getId(),
                        unit
                    );

                AggregatedIngredient current =
                    aggregated.get(key);

                if (current == null) {

                    aggregated.put(
                        key,
                        new AggregatedIngredient(
                            ingredient,
                            recipeIngredient.getQuantity(),
                            unit
                        )
                    );

                } else {

                    // L'ingrédient existe déjà dans la liste :
                    // on additionne simplement sa quantité.
                    aggregated.put(
                        key,
                        new AggregatedIngredient(
                            ingredient,
                            current.quantity().add(
                                recipeIngredient.getQuantity()
                            ),
                            unit
                        )
                    );
                }
            }
        }

        List<ShoppingItem> items = new ArrayList<>();

        // Transformation des quantités agrégées en entités persistées.
        for (AggregatedIngredient aggregatedIngredient :
                aggregated.values()) {

            items.add(
                new ShoppingItem(
                    shoppingList,
                    aggregatedIngredient.ingredient(),
                    aggregatedIngredient.quantity(),
                    aggregatedIngredient.unit(),
                    false
                )
            );
        }

        shoppingItemRepository.saveAll(items);

        return toResponse(shoppingList, items);
    }

    /**
     * Récupère une liste uniquement si elle appartient
     * à l'utilisateur connecté.
     */
    public ShoppingListResponse findByIdAndUserId(
            Long shoppingListId,
            Long userId) {

        ShoppingList shoppingList = shoppingListRepository
            .findByIdAndMealPlanUserId(
                shoppingListId,
                userId
            )
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Shopping list not found"
                )
            );

        List<ShoppingItem> items =
            shoppingItemRepository
                .findAllByShoppingListIdOrderByIngredientNameAsc(
                    shoppingListId
                );

        return toResponse(shoppingList, items);
    }

    /**
     * Coche ou décoche un article.
     *
     * La recherche passe par l'utilisateur propriétaire de la liste,
     * ce qui empêche de modifier l'article d'un autre utilisateur.
     */
    @Transactional
    public ShoppingItemResponse updateChecked(
            Long shoppingItemId,
            Long userId,
            boolean checked) {

        ShoppingItem shoppingItem = shoppingItemRepository
            .findByIdAndShoppingListMealPlanUserId(
                shoppingItemId,
                userId
            )
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Shopping item not found"
                )
            );

        shoppingItem.setChecked(checked);

        ShoppingItem savedItem =
            shoppingItemRepository.save(shoppingItem);

        return toItemResponse(savedItem);
    }

    /**
     * Transforme une entité ShoppingList en DTO destiné à l'API.
     */
    private ShoppingListResponse toResponse(
            ShoppingList shoppingList,
            List<ShoppingItem> items) {

        List<ShoppingItemResponse> itemResponses =
            items.stream()
                .map(this::toItemResponse)
                .toList();

        return new ShoppingListResponse(
            shoppingList.getId(),
            shoppingList.getMealPlan().getId(),
            itemResponses
        );
    }

    /**
     * Transforme une ligne de courses en DTO.
     */
    private ShoppingItemResponse toItemResponse(
            ShoppingItem shoppingItem) {

        return new ShoppingItemResponse(
            shoppingItem.getId(),
            shoppingItem.getIngredient().getId(),
            shoppingItem.getIngredient().getName(),
            shoppingItem.getQuantity(),
            shoppingItem.getUnit(),
            shoppingItem.isChecked()
        );
    }

    /**
     * Clé technique utilisée pendant l'agrégation.
     */
    private record IngredientUnitKey(
        Long ingredientId,
        String unit
    ) {
    }

    /**
     * Valeur temporaire utilisée avant la création des ShoppingItem.
     */
    private record AggregatedIngredient(
        Ingredient ingredient,
        BigDecimal quantity,
        String unit
    ) {
    }
}