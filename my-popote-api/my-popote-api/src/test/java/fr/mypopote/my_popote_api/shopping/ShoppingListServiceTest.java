package fr.mypopote.my_popote_api.shopping;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service de gestion des listes de courses.
 *
 * Une liste doit toujours être recherchée avec
 * l'identifiant de son propriétaire.
 */
@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @InjectMocks
    private ShoppingListService shoppingListService;

    @Test
    void shouldFindShoppingListForAuthenticatedUser() {
        Long shoppingListId = 1L;
        Long userId = 42L;

        ShoppingList shoppingList = new ShoppingList(null);

        when(
            shoppingListRepository.findByIdAndMealPlanUserId(
                shoppingListId,
                userId
            )
        ).thenReturn(Optional.of(shoppingList));

        ShoppingList result =
            shoppingListService.findByIdAndUserId(
                shoppingListId,
                userId
            );

        assertThat(result).isSameAs(shoppingList);

        verify(shoppingListRepository)
            .findByIdAndMealPlanUserId(
                shoppingListId,
                userId
            );
    }
}