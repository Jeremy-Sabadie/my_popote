package fr.mypopote.my_popote_api.shopping;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import fr.mypopote.my_popote_api.security.CurrentUserService;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingItemResponse;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingItemUpdateRequest;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingListResponse;

/**
 * Tests du contrôleur de liste de courses.
 *
 * On vérifie principalement que l'utilisateur utilisé par le contrôleur
 * vient bien du JWT.
 */
class ShoppingListControllerTest {

    private ShoppingListService shoppingListService;
    private CurrentUserService currentUserService;
    private ShoppingListController shoppingListController;
    private Jwt jwt;

    @BeforeEach
    void setUp() {

        shoppingListService =
            mock(ShoppingListService.class);

        currentUserService =
            mock(CurrentUserService.class);

        jwt = mock(Jwt.class);

        shoppingListController =
            new ShoppingListController(
                shoppingListService,
                currentUserService
            );
    }

    @Test
    void shouldGetShoppingListForAuthenticatedUser() {

        Long userId = 1L;
        Long shoppingListId = 10L;

        ShoppingListResponse expected =
            new ShoppingListResponse(
                shoppingListId,
                5L,
                List.of()
            );

        when(currentUserService.getUserId(jwt))
            .thenReturn(userId);

        when(
            shoppingListService.findByIdAndUserId(
                shoppingListId,
                userId
            )
        ).thenReturn(expected);

        ShoppingListResponse response =
            shoppingListController.getShoppingList(
                shoppingListId,
                jwt
            );

        assertThat(response).isEqualTo(expected);

        verify(shoppingListService)
            .findByIdAndUserId(
                shoppingListId,
                userId
            );
    }

    @Test
    void shouldGenerateShoppingListForAuthenticatedUser() {

        Long userId = 1L;
        Long mealPlanId = 5L;

        ShoppingListResponse expected =
            new ShoppingListResponse(
                10L,
                mealPlanId,
                List.of()
            );

        when(currentUserService.getUserId(jwt))
            .thenReturn(userId);

        when(
            shoppingListService.generate(
                mealPlanId,
                userId
            )
        ).thenReturn(expected);

        ShoppingListResponse response =
            shoppingListController.generate(
                mealPlanId,
                jwt
            );

        assertThat(response).isEqualTo(expected);

        verify(shoppingListService)
            .generate(
                mealPlanId,
                userId
            );
    }

    @Test
    void shouldUpdateShoppingItemForAuthenticatedUser() {

        Long userId = 1L;
        Long shoppingItemId = 20L;

        ShoppingItemUpdateRequest request =
            new ShoppingItemUpdateRequest(true);

        ShoppingItemResponse expected =
            new ShoppingItemResponse(
                shoppingItemId,
                30L,
                "Tomate",
                new BigDecimal("2.000"),
                "PIECE",
                true
            );

        when(currentUserService.getUserId(jwt))
            .thenReturn(userId);

        when(
            shoppingListService.updateChecked(
                shoppingItemId,
                userId,
                true
            )
        ).thenReturn(expected);

        ShoppingItemResponse response =
            shoppingListController.updateChecked(
                shoppingItemId,
                request,
                jwt
            );

        assertThat(response).isEqualTo(expected);

        verify(shoppingListService)
            .updateChecked(
                shoppingItemId,
                userId,
                true
            );
    }
}