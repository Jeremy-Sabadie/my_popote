package fr.mypopote.my_popote_api.recipe;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests unitaires de la gestion des ingrédients.
 *
 * Un ingrédient existant doit être réutilisé afin d'éviter
 * de créer plusieurs entrées pour "Tomate", "tomate", etc.
 */
@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private IngredientService ingredientService;

    @Test
    void shouldReturnExistingIngredientIgnoringCase() {
        Ingredient ingredient = new Ingredient("Tomate");

        // La recherche du repository est volontairement insensible à la casse.
        when(ingredientRepository.findByNameIgnoreCase("tomate"))
            .thenReturn(Optional.of(ingredient));

        Ingredient result = ingredientService.findOrCreate("tomate");

        // Le service doit réutiliser l'ingrédient existant.
        assertThat(result).isSameAs(ingredient);
    }
}