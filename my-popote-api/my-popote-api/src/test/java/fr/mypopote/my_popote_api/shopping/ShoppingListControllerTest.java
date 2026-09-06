package fr.mypopote.my_popote_api.shopping;

import fr.mypopote.my_popote_api.security.CurrentUserService;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingListResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du contrôleur des listes de courses.
 *
 * L'identité utilisée pour accéder à une liste
 * doit provenir du JWT authentifié.
 */
@ExtendWith(MockitoExtension.class)
class ShoppingListControllerTest {

    @Mock
    private ShoppingListService shoppingListService;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private ShoppingListController shoppingListController;

    @Test
    void shouldReturnShoppingListForAuthenticatedUser() {
        Long shoppingListId = 1L;
        Long userId = 42L;

        ShoppingList shoppingList = new ShoppingList(null);

        when(currentUserService.getUserId(jwt))
            .thenReturn(userId);

        when(
            shoppingListService.findByIdAndUserId(
                shoppingListId,
                userId
            )
        ).thenReturn(shoppingList);

        ShoppingListResponse response =
            shoppingListController.findById(
                jwt,
                shoppingListId
            );

        assertThat(response).isNotNull();
        assertThat(response.items()).isEmpty();

        verify(shoppingListService)
            .findByIdAndUserId(
                shoppingListId,
                userId
            );
    }
}