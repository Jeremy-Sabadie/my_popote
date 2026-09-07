package fr.mypopote.my_popote_api.shopping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.mypopote.my_popote_api.planning.MealPlan;
import fr.mypopote.my_popote_api.planning.MealPlanRepository;
import fr.mypopote.my_popote_api.planning.PlannedMealRepository;
import fr.mypopote.my_popote_api.recipe.Ingredient;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingItemResponse;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingListResponse;
import fr.mypopote.my_popote_api.user.User;

/**
 * Tests unitaires du service de liste de courses.
 */
@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @Mock
    private ShoppingItemRepository shoppingItemRepository;

    @Mock
    private MealPlanRepository mealPlanRepository;

    @Mock
    private PlannedMealRepository plannedMealRepository;

    private ShoppingListService shoppingListService;

    @BeforeEach
    void setUp() {

        shoppingListService = new ShoppingListService(
            shoppingListRepository,
            shoppingItemRepository,
            mealPlanRepository,
            plannedMealRepository
        );
    }

    /**
     * Vérifie qu'une liste est recherchée avec l'identifiant
     * de l'utilisateur connecté.
     */
    @Test
    void shouldFindShoppingListForAuthenticatedUser() {

        Long userId = 1L;
        Long shoppingListId = 10L;

        User user = new User(
            "jeremy@example.com",
            "hashed-password",
            "Jérémy"
        );

        MealPlan mealPlan = new MealPlan(
            user,
            LocalDate.of(2026, 9, 7),
            false,
            null,
            null
        );

        ShoppingList shoppingList =
            new ShoppingList(mealPlan);

        when(
            shoppingListRepository
                .findByIdAndMealPlanUserId(
                    shoppingListId,
                    userId
                )
        ).thenReturn(Optional.of(shoppingList));

        /*
         * Le service recharge les articles de la liste après
         * avoir vérifié que celle-ci appartient bien au user.
         */
        when(
            shoppingItemRepository
                .findAllByShoppingListIdOrderByIdAsc(
                    shoppingListId
                )
        ).thenReturn(List.of());

        ShoppingListResponse response =
            shoppingListService.findByIdAndUserId(
                shoppingListId,
                userId
            );

        assertThat(response).isNotNull();
        assertThat(response.items()).isEmpty();
        assertThat(response.alreadyOwnedItems()).isEmpty();

        // Le repository doit obligatoirement filtrer par utilisateur.
        verify(shoppingListRepository)
            .findByIdAndMealPlanUserId(
                shoppingListId,
                userId
            );

        verify(shoppingItemRepository)
            .findAllByShoppingListIdOrderByIdAsc(
                shoppingListId
            );
    }

    /**
     * Vérifie que l'état checked d'un article peut être modifié
     * uniquement via une recherche limitée à l'utilisateur.
     */
    @Test
    void shouldUpdateCheckedOnlyForAuthenticatedUser() {

        Long userId = 1L;
        Long shoppingItemId = 20L;

        Ingredient ingredient =
            new Ingredient("Tomate");

        ShoppingItem shoppingItem =
            new ShoppingItem(
                null,
                ingredient,
                new BigDecimal("2.000"),
                "PIECE",
                false
            );

        when(
            shoppingItemRepository
                .findByIdAndShoppingListMealPlanUserId(
                    shoppingItemId,
                    userId
                )
        ).thenReturn(Optional.of(shoppingItem));

        when(shoppingItemRepository.save(shoppingItem))
            .thenReturn(shoppingItem);

        ShoppingItemResponse response =
            shoppingListService.updateChecked(
                shoppingItemId,
                userId,
                true
            );

        assertThat(response.checked()).isTrue();

        // L'article doit lui aussi être récupéré avec contrôle
        // du propriétaire de la liste.
        verify(shoppingItemRepository)
            .findByIdAndShoppingListMealPlanUserId(
                shoppingItemId,
                userId
            );
    }
}