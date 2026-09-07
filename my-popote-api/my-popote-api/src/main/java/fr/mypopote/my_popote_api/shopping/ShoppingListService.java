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
import fr.mypopote.my_popote_api.shopping.dto.ManualShoppingItemRequest;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingItemResponse;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingListResponse;

/**
 * Gère la génération et la personnalisation des listes de courses.
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
     * Génère la liste depuis le planning du user.
     *
     * Les ajouts manuels survivent à une régénération.
     * Les états checked et alreadyOwned des ingrédients
     * toujours présents sont également conservés.
     */
    @Transactional
    public ShoppingListResponse generate(
            Long mealPlanId,
            Long userId) {

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

        ShoppingList shoppingList = shoppingListRepository
            .findByMealPlanIdAndMealPlanUserId(
                mealPlanId,
                userId
            )
            .orElseGet(() -> new ShoppingList(mealPlan));

        shoppingList =
            shoppingListRepository.save(shoppingList);

        /*
         * Mémorise les choix effectués par le user avant
         * de reconstruire les ingrédients issus des recettes.
         */
        Map<IngredientUnitKey, ItemState> previousStates =
            new LinkedHashMap<>();

        List<ShoppingItem> existingGeneratedItems =
            shoppingItemRepository
                .findAllByShoppingListIdAndManualFalse(
                    shoppingList.getId()
                );

        for (ShoppingItem item : existingGeneratedItems) {

            if (item.getIngredient() == null) {
                continue;
            }

            IngredientUnitKey key =
                new IngredientUnitKey(
                    item.getIngredient().getId(),
                    item.getUnit()
                );

            previousStates.put(
                key,
                new ItemState(
                    item.isChecked(),
                    item.isAlreadyOwned()
                )
            );
        }

        /*
         * Les envies manuelles ne sont pas supprimées.
         */
        shoppingItemRepository
            .deleteAllByShoppingListIdAndManualFalse(
                shoppingList.getId()
            );

        Map<IngredientUnitKey, AggregatedIngredient> aggregated =
            new LinkedHashMap<>();

        /*
         * Agrégation des quantités nécessaires aux recettes.
         */
        for (PlannedMeal plannedMeal : plannedMeals) {

            for (RecipeIngredient recipeIngredient :
                    plannedMeal.getRecipe().getIngredients()) {

                Ingredient ingredient =
                    recipeIngredient.getIngredient();

                String unit =
                    recipeIngredient.getUnit();

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

        List<ShoppingItem> generatedItems =
            new ArrayList<>();

        for (Map.Entry<
                IngredientUnitKey,
                AggregatedIngredient> entry :
                aggregated.entrySet()) {

            IngredientUnitKey key =
                entry.getKey();

            AggregatedIngredient value =
                entry.getValue();

            ItemState previousState =
                previousStates.get(key);

            boolean checked =
                previousState != null
                && previousState.checked();

            boolean alreadyOwned =
                previousState != null
                && previousState.alreadyOwned();

            generatedItems.add(
                new ShoppingItem(
                    shoppingList,
                    value.ingredient(),
                    null,
                    value.quantity(),
                    value.unit(),
                    checked,
                    alreadyOwned,
                    false
                )
            );
        }

        shoppingItemRepository.saveAll(generatedItems);

        return reloadResponse(
            shoppingList,
            shoppingList.getId()
        );
    }

    /**
     * Récupère une liste uniquement si elle appartient
     * au user connecté.
     */
    public ShoppingListResponse findByIdAndUserId(
            Long shoppingListId,
            Long userId) {

        ShoppingList shoppingList =
            findOwnedShoppingList(
                shoppingListId,
                userId
            );

        return reloadResponse(
            shoppingList,
            shoppingListId
        );
    }

    /**
     * Coche ou décoche un article.
     */
    @Transactional
    public ShoppingItemResponse updateChecked(
            Long shoppingItemId,
            Long userId,
            boolean checked) {

        ShoppingItem shoppingItem =
            findOwnedShoppingItem(
                shoppingItemId,
                userId
            );

        shoppingItem.setChecked(checked);

        return toItemResponse(
            shoppingItemRepository.save(shoppingItem)
        );
    }

    /**
     * Gère le choix :
     * "C'est bon, j'en ai déjà".
     */
    @Transactional
    public ShoppingItemResponse updateAlreadyOwned(
            Long shoppingItemId,
            Long userId,
            boolean alreadyOwned) {

        ShoppingItem shoppingItem =
            findOwnedShoppingItem(
                shoppingItemId,
                userId
            );

        shoppingItem.setAlreadyOwned(alreadyOwned);

        if (alreadyOwned) {
            shoppingItem.setChecked(false);
        }

        return toItemResponse(
            shoppingItemRepository.save(shoppingItem)
        );
    }

    /**
     * Ajoute une envie manuelle.
     */
    @Transactional
    public ShoppingItemResponse addManualItem(
            Long shoppingListId,
            Long userId,
            ManualShoppingItemRequest request) {

        ShoppingList shoppingList =
            findOwnedShoppingList(
                shoppingListId,
                userId
            );

        ShoppingItem shoppingItem =
            ShoppingItem.manual(
                shoppingList,
                request.name().trim(),
                request.quantity(),
                request.unit().trim().toUpperCase()
            );

        return toItemResponse(
            shoppingItemRepository.save(shoppingItem)
        );
    }

    private ShoppingList findOwnedShoppingList(
            Long shoppingListId,
            Long userId) {

        return shoppingListRepository
            .findByIdAndMealPlanUserId(
                shoppingListId,
                userId
            )
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Shopping list not found"
                )
            );
    }

    private ShoppingItem findOwnedShoppingItem(
            Long shoppingItemId,
            Long userId) {

        return shoppingItemRepository
            .findByIdAndShoppingListMealPlanUserId(
                shoppingItemId,
                userId
            )
            .orElseThrow(() ->
                new IllegalArgumentException(
                    "Shopping item not found"
                )
            );
    }

    private ShoppingListResponse reloadResponse(
            ShoppingList shoppingList,
            Long shoppingListId) {

        List<ShoppingItem> items =
            shoppingItemRepository
                .findAllByShoppingListIdOrderByIdAsc(
                    shoppingListId
                );

        return toResponse(
            shoppingList,
            items
        );
    }

    /**
     * Sépare les achats nécessaires des ingrédients
     * que le user possède déjà.
     */
    private ShoppingListResponse toResponse(
            ShoppingList shoppingList,
            List<ShoppingItem> items) {

        List<ShoppingItemResponse> itemsToBuy =
            items.stream()
                .filter(item -> !item.isAlreadyOwned())
                .map(this::toItemResponse)
                .toList();

        List<ShoppingItemResponse> alreadyOwnedItems =
            items.stream()
                .filter(ShoppingItem::isAlreadyOwned)
                .map(this::toItemResponse)
                .toList();

        return new ShoppingListResponse(
            shoppingList.getId(),
            shoppingList.getMealPlan().getId(),
            itemsToBuy,
            alreadyOwnedItems
        );
    }

    private ShoppingItemResponse toItemResponse(
            ShoppingItem shoppingItem) {

        Long ingredientId =
            shoppingItem.getIngredient() == null
                ? null
                : shoppingItem.getIngredient().getId();

        return new ShoppingItemResponse(
            shoppingItem.getId(),
            ingredientId,
            shoppingItem.getDisplayName(),
            shoppingItem.getQuantity(),
            shoppingItem.getUnit(),
            shoppingItem.isChecked(),
            shoppingItem.isAlreadyOwned(),
            shoppingItem.isManual()
        );
    }

    private record IngredientUnitKey(
        Long ingredientId,
        String unit
    ) {
    }

    private record AggregatedIngredient(
        Ingredient ingredient,
        BigDecimal quantity,
        String unit
    ) {
    }

    /**
     * Choix utilisateur conservés lors d'une régénération.
     */
    private record ItemState(
        boolean checked,
        boolean alreadyOwned
    ) {
    }
}