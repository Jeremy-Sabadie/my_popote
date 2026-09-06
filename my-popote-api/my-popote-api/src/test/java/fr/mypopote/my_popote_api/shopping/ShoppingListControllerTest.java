package fr.mypopote.my_popote_api.shopping;

import fr.mypopote.my_popote_api.shopping.dto.ShoppingListResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du contrôleur des listes de courses.
 *
 * La génération et l'agrégation des ingrédients seront testées
 * lorsqu'elles seront introduites dans la logique métier.
 */
@ExtendWith(MockitoExtension.class)
class ShoppingListControllerTest {

    @Mock
    private ShoppingListService shoppingListService;

    @InjectMocks
    private ShoppingListController shoppingListController;

    @Test
    void shouldReturnShoppingListById() {
        ShoppingList shoppingList = new ShoppingList(null);

        when(shoppingListService.findById(1L))
            .thenReturn(shoppingList);

        ShoppingListResponse response =
            shoppingListController.findById(1L);

        assertThat(response).isNotNull();
        assertThat(response.items()).isEmpty();
    }
}