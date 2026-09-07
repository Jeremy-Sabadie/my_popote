package fr.mypopote.my_popote_api.shopping;

import java.math.BigDecimal;
import java.util.Optional;

import fr.mypopote.my_popote_api.shopping.dto.ManualShoppingItemRequest;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingItemResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Vérifie les personnalisations ajoutées à la liste de courses.
 */
@ExtendWith(MockitoExtension.class)
class ShoppingListCustomizationServiceTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @Mock
    private ShoppingItemRepository shoppingItemRepository;

    @Mock
    private fr.mypopote.my_popote_api.planning.MealPlanRepository
        mealPlanRepository;

    @Mock
    private fr.mypopote.my_popote_api.planning.PlannedMealRepository
        plannedMealRepository;

    @InjectMocks
    private ShoppingListService shoppingListService;

    @Test
    void shouldAddManualShoppingItem() {

        Long shoppingListId = 1L;
        Long userId = 42L;

        ShoppingList shoppingList =
            new ShoppingList(null);

        ManualShoppingItemRequest request =
            new ManualShoppingItemRequest(
                "Chocolat noir",
                BigDecimal.ONE,
                "PIECE"
            );

        when(
            shoppingListRepository
                .findByIdAndMealPlanUserId(
                    shoppingListId,
                    userId
                )
        ).thenReturn(Optional.of(shoppingList));

        when(shoppingItemRepository.save(any(ShoppingItem.class)))
            .thenAnswer(invocation ->
                invocation.getArgument(0)
            );

        ShoppingItemResponse response =
            shoppingListService.addManualItem(
                shoppingListId,
                userId,
                request
            );

        assertThat(response.ingredientName())
            .isEqualTo("Chocolat noir");

        assertThat(response.manual())
            .isTrue();

        assertThat(response.alreadyOwned())
            .isFalse();
    }

    @Test
    void shouldMarkItemAsAlreadyOwned() {

        Long shoppingItemId = 10L;
        Long userId = 42L;

        ShoppingItem item =
            ShoppingItem.manual(
                new ShoppingList(null),
                "Café",
                BigDecimal.ONE,
                "PAQUET"
            );

        when(
            shoppingItemRepository
                .findByIdAndShoppingListMealPlanUserId(
                    shoppingItemId,
                    userId
                )
        ).thenReturn(Optional.of(item));

        when(shoppingItemRepository.save(item))
            .thenReturn(item);

        ShoppingItemResponse response =
            shoppingListService.updateAlreadyOwned(
                shoppingItemId,
                userId,
                true
            );

        assertThat(response.alreadyOwned())
            .isTrue();

        assertThat(response.checked())
            .isFalse();
    }
}