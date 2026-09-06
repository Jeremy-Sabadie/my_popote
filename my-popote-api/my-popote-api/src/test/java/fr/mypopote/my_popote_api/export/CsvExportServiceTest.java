package fr.mypopote.my_popote_api.export;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.mypopote.my_popote_api.recipe.RecipeService;
import fr.mypopote.my_popote_api.recipe.dto.RecipeIngredientResponse;
import fr.mypopote.my_popote_api.recipe.dto.RecipeResponse;
import fr.mypopote.my_popote_api.shopping.ShoppingListService;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingItemResponse;
import fr.mypopote.my_popote_api.shopping.dto.ShoppingListResponse;

/**
 * Tests unitaires du service d'export CSV.
 *
 * On vérifie le contenu produit mais également que les services métier
 * sont toujours appelés avec l'identifiant de l'utilisateur connecté.
 */
@ExtendWith(MockitoExtension.class)
class CsvExportServiceTest {

    @Mock
    private RecipeService recipeService;

    @Mock
    private ShoppingListService shoppingListService;

    private CsvExportService csvExportService;

    @BeforeEach
    void setUp() {

        csvExportService = new CsvExportService(
            recipeService,
            shoppingListService
        );
    }

    /**
     * Vérifie qu'une recette peut être transformée en CSV
     * avec ses ingrédients et ses saisons.
     */
    @Test
    void shouldExportRecipesAsCsv() {

        Long userId = 1L;

        RecipeIngredientResponse ingredient =
            new RecipeIngredientResponse(
                10L,
                "Tomate",
                new BigDecimal("2.000"),
                "PIECE"
            );

        RecipeResponse recipe =
            new RecipeResponse(
                20L,
                "Salade tomate",
                "SALAD",
                2,
                new BigDecimal("4.50"),
                "Couper les tomates",
                List.of(ingredient),
                Set.of("SUMMER")
            );

        when(
            recipeService.findAllByUserId(
                userId,
                null,
                null
            )
        ).thenReturn(List.of(recipe));

        String csv =
            csvExportService.exportRecipes(userId);

        assertThat(csv)
            .contains(
                "Nom;Portions;Coût estimé;Instructions;Ingrédients;Saisons"
            )
            .contains("Salade tomate")
            .contains("Tomate 2.000 PIECE")
            .contains("SUMMER");

        // L'export ne doit récupérer que les recettes
        // appartenant à l'utilisateur connecté.
        verify(recipeService)
            .findAllByUserId(
                userId,
                null,
                null
            );
    }

    /**
     * Vérifie qu'un point-virgule présent dans une donnée utilisateur
     * ne casse pas la structure du fichier CSV.
     */
    @Test
    void shouldEscapeCsvValuesContainingSeparator() {

        Long userId = 1L;

        RecipeResponse recipe =
            new RecipeResponse(
                20L,
                "Poulet; curry",
                "MEAT",
                2,
                new BigDecimal("6.50"),
                "Cuire puis servir",
                List.of(),
                Set.of("ALL_YEAR")
            );

        when(
            recipeService.findAllByUserId(
                userId,
                null,
                null
            )
        ).thenReturn(List.of(recipe));

        String csv =
            csvExportService.exportRecipes(userId);

        // Le point-virgule appartient au nom de la recette :
        // le champ doit donc être entouré de guillemets.
        assertThat(csv)
            .contains("\"Poulet; curry\"");
    }

    /**
     * Vérifie la génération d'un CSV depuis une liste de courses.
     */
    @Test
    void shouldExportShoppingListAsCsv() {

        Long userId = 1L;
        Long shoppingListId = 10L;

        ShoppingItemResponse item =
            new ShoppingItemResponse(
                20L,
                30L,
                "Tomate",
                new BigDecimal("4.000"),
                "PIECE",
                true
            );

        ShoppingListResponse shoppingList =
            new ShoppingListResponse(
                shoppingListId,
                40L,
                List.of(item)
            );

        when(
            shoppingListService.findByIdAndUserId(
                shoppingListId,
                userId
            )
        ).thenReturn(shoppingList);

        String csv =
            csvExportService.exportShoppingList(
                shoppingListId,
                userId
            );

        assertThat(csv)
            .contains(
                "Ingrédient;Quantité;Unité;Acheté"
            )
            .contains(
                "Tomate;4.000;PIECE;Oui"
            );

        // Le contrôle de propriété reste appliqué pendant l'export.
        verify(shoppingListService)
            .findByIdAndUserId(
                shoppingListId,
                userId
            );
    }
}