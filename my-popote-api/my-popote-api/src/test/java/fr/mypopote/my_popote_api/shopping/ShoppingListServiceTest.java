package fr.mypopote.my_popote_api.shopping;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires du service de gestion des listes de courses.
 *
 * La génération et l'agrégation des ingrédients seront ajoutées
 * progressivement lorsque cette logique métier sera implémentée.
 */
@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @InjectMocks
    private ShoppingListService shoppingListService;

    @Test
    void shouldFindShoppingListById() {
        ShoppingList shoppingList = new ShoppingList(null);

        when(shoppingListRepository.findById(1L))
            .thenReturn(Optional.of(shoppingList));

        ShoppingList result = shoppingListService.findById(1L);

        assertThat(result).isSameAs(shoppingList);
    }
}